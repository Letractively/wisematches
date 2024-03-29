package wisematches.playground.propose.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import wisematches.core.Personality;
import wisematches.core.PersonalityManager;
import wisematches.core.Player;
import wisematches.core.search.Orders;
import wisematches.core.search.Range;
import wisematches.playground.GameSettings;
import wisematches.playground.propose.*;
import wisematches.playground.tracking.StatisticsManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractProposalManager<S extends GameSettings> implements GameProposalManager<S>, InitializingBean {
	private StatisticsManager playerStatisticManager;
	private PersonalityManager personalityManager;
	protected TransactionTemplate transactionTemplate;

	private final Lock lock = new ReentrantLock();
	private final AtomicLong proposalIds = new AtomicLong();

	private final Map<Long, AbstractGameProposal<S>> proposals = new ConcurrentHashMap<>();
	private final Collection<GameProposalListener> proposalListeners = new CopyOnWriteArraySet<>();

	protected AbstractProposalManager() {
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void afterPropertiesSet() throws Exception {
		lock.lock();
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					long max = 0;
					final Collection<AbstractGameProposal<S>> gameProposals = loadGameProposals();
					for (AbstractGameProposal<S> proposal : gameProposals) {
						max = Math.max(max, proposal.getId());
						proposals.put(proposal.getId(), proposal);

						proposal.validatePlayers(personalityManager);
					}
					proposalIds.set(max + 1);
				}
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addGameProposalListener(GameProposalListener l) {
		proposalListeners.add(l);
	}

	@Override
	public void removeGameProposalListener(GameProposalListener l) {
		if (l != null) {
			proposalListeners.remove(l);
		}
	}

	@Override
	public PrivateProposal<S> initiate(S settings, String commentary, Player initiator, Collection<Player> opponents) {
		final long id = proposalIds.incrementAndGet();
		return registerProposal(new DefaultPrivateProposal<>(id, commentary, settings, initiator, opponents));
	}

	@Override
	public PublicProposal<S> initiate(S settings, Player initiator, int opponentsCount, Criterion... criteria) {
		final long id = proposalIds.incrementAndGet();
		return registerProposal(new DefaultPublicProposal<>(id, settings, initiator, opponentsCount, Arrays.asList(criteria)));
	}

	@Override
	public GameProposal<S> accept(long proposalId, Player player) throws CriterionViolationException {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = proposals.get(proposalId);
			if (proposal == null) {
				return null;
			}

			Collection<CriterionViolation> violations = validate(proposalId, player);
			if (violations != null) {
				throw new CriterionViolationException(violations);
			}

			proposal.attach(player);
			if (proposal.isReady()) {
				proposals.remove(proposalId);
				removeGameProposal(proposal);
				fireGameProposalFinalized(proposal, null, ProposalResolution.READY);
			} else {
				storeGameProposal(proposal);
				fireGameProposalUpdated(proposal, player, ProposalDirective.ACCEPTED);
			}
			return proposal;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GameProposal<S> reject(long proposalId, Player player) throws CriterionViolationException {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = proposals.get(proposalId);
			if (proposal == null || !proposal.getPlayers().contains(player)) {
				return null;
			}

			if (proposal.getInitiator().equals(player)) {
				proposals.remove(proposalId);
				removeGameProposal(proposal);
				fireGameProposalFinalized(proposal, player, ProposalResolution.REPUDIATED);
			} else {
				if (proposal.getProposalType() == ProposalType.PRIVATE) {
					proposals.remove(proposalId);
					removeGameProposal(proposal);
					fireGameProposalFinalized(proposal, player, ProposalResolution.REJECTED);
				} else {
					proposal.detach(player);
					storeGameProposal(proposal);
					fireGameProposalUpdated(proposal, player, ProposalDirective.REJECTED);
				}
			}
			return proposal;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GameProposal<S> terminate(long proposalId) {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = proposals.get(proposalId);
			if (proposal == null) {
				return null;
			}
			proposals.remove(proposalId);
			removeGameProposal(proposal);

			final List<Player> players = new ArrayList<>(proposal.getPlayers());
			players.removeAll(proposal.getJoinedPlayers());

			Player terminated = null;
			if (players.size() != 0) {
				terminated = players.get(0);
			}
			fireGameProposalFinalized(proposal, terminated, ProposalResolution.TERMINATED);

			return proposal;
		} finally {
			lock.unlock();
		}
	}


	@Override
	public GameProposal<S> getProposal(long proposalId) {
		lock.lock();
		try {
			return proposals.get(proposalId);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Collection<CriterionViolation> validate(long proposalId, Player player) {
		final AbstractGameProposal<S> proposal = proposals.get(proposalId);
		if (proposal == null) {
			return null;
		}

		if (proposal.isPlayerJoined(player)) {
			return Collections.singleton(new CriterionViolation("player.joined", player.getId()));
		}

		if (proposal.getProposalType() == ProposalType.PRIVATE) {
			if (!proposal.validatePlayer(player)) {
				return Collections.singleton(new CriterionViolation("player.unexpected", player.getId()));
			}
			return null;
		}
		if (proposal instanceof PublicProposal) {
			return ((PublicProposal) proposal).checkViolations(player, playerStatisticManager.getStatistic(player));
		}
		return null;
	}


	@Override
	public <Ctx extends ProposalRelation> int getTotalCount(Personality person, Ctx context) {
		lock.lock();
		try {
			int res = 0;
			if (context == ProposalRelation.AVAILABLE) {
				res = proposals.size();
			} else if (context == ProposalRelation.INVOLVED) {
				for (AbstractGameProposal<S> proposal : proposals.values()) {
					if (person instanceof Player && proposal.containsPlayer((Player) person)) {
						res++;
					}
				}
			} else {
				throw new IllegalArgumentException("Proposal type is not supported: " + context);
			}
			return res;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <Ctx extends ProposalRelation> List<GameProposal<S>> searchEntities(Personality person, Ctx context, Orders orders, Range range) {
		lock.lock();
		try {
			if (context == ProposalRelation.AVAILABLE) {
				return new ArrayList<GameProposal<S>>(proposals.values());
			} else if (context == ProposalRelation.INVOLVED) {
				final List<GameProposal<S>> res = new ArrayList<>();
				for (AbstractGameProposal<S> proposal : proposals.values()) {
					if (person instanceof Player && proposal.containsPlayer((Player) person)) {
						res.add(proposal);
					}
				}
				return res;
			} else {
				throw new IllegalArgumentException("Proposal type is not supported: " + context);
			}
		} finally {
			lock.unlock();
		}
	}


	private <T extends AbstractGameProposal<S>> T registerProposal(T proposal) {
		lock.lock();
		try {
			proposals.put(proposal.getId(), proposal);
			storeGameProposal(proposal);
			fireGameProposalInitiated(proposal);
			return proposal;
		} finally {
			lock.unlock();
		}
	}

	protected abstract Collection<AbstractGameProposal<S>> loadGameProposals();

	protected abstract void storeGameProposal(AbstractGameProposal<S> proposal);

	protected abstract void removeGameProposal(AbstractGameProposal<S> proposal);

	public void setPersonalityManager(PersonalityManager personalityManager) {
		this.personalityManager = personalityManager;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public void setPlayerStatisticManager(StatisticsManager playerStatisticManager) {
		this.playerStatisticManager = playerStatisticManager;
	}

	protected void fireGameProposalInitiated(GameProposal<S> proposal) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalInitiated(proposal);
		}
	}

	protected void fireGameProposalUpdated(GameProposal<S> proposal, Player player, ProposalDirective directive) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalUpdated(proposal, player, directive);
		}
	}

	protected void fireGameProposalFinalized(GameProposal<S> proposal, Player player, ProposalResolution reason) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalFinalized(proposal, player, reason);
		}
	}
}