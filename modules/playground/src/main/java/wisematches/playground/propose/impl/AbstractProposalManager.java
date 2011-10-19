package wisematches.playground.propose.impl;

import org.springframework.beans.factory.InitializingBean;
import wisematches.personality.Personality;
import wisematches.personality.player.Player;
import wisematches.playground.GameSettings;
import wisematches.playground.propose.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public abstract class AbstractProposalManager<S extends GameSettings> implements GameProposalManager<S>, InitializingBean {
	private final Lock lock = new ReentrantLock();

	private final AtomicLong proposalIds = new AtomicLong();
	private final Map<Long, GameProposal<S>> proposals = new ConcurrentHashMap<Long, GameProposal<S>>();

	private final Collection<GameProposalListener> proposalListeners = new CopyOnWriteArraySet<GameProposalListener>();

	protected AbstractProposalManager() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		long max = 0;
		final Collection<GameProposal<S>> gameProposals = loadGameProposals();
		for (GameProposal<S> proposal : gameProposals) {
			max = Math.max(max, proposal.getId());
			proposals.put(proposal.getId(), proposal);
		}
		proposalIds.set(max + 1);
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
	public GameProposal<S> initiateWaitingProposal(S settings, Player initiator, int playersCount, GameRestriction restriction) {
		return registerProposal(new DefaultWaitingGameProposal<S>(proposalIds.incrementAndGet(), settings, playersCount, initiator, restriction));
	}

	@Override
	public GameProposal<S> initiateChallengeProposal(S settings, String comment, Player initiator, Collection<Player> opponents) {
		return registerProposal(new DefaultChallengeGameProposal<S>(proposalIds.incrementAndGet(), settings, comment, initiator, opponents));
	}

	private GameProposal<S> registerProposal(GameProposal<S> proposal) {
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

	@Override
	public GameProposal<S> attachPlayer(long proposalId, Player player) throws ViolatedRestrictionException {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = (AbstractGameProposal<S>) proposals.get(proposalId);
			if (proposal == null) {
				return null;
			}
			proposal.attachPlayer(player);
			if (proposal.isReady()) {
				proposals.remove(proposalId);
				removeGameProposal(proposal);
				fireGameProposalClosed(proposal);
			} else {
				storeGameProposal(proposal);
				fireGameProposalUpdated(proposal);
			}
			return proposal;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GameProposal<S> detachPlayer(long proposalId, Player player) throws ViolatedRestrictionException {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = (AbstractGameProposal<S>) proposals.get(proposalId);
			if (proposal == null) {
				return null;
			}
			proposal.detachPlayer(player);
			if (proposal.getPlayers().isEmpty()) {
				proposals.remove(proposalId);
				removeGameProposal(proposal);
				fireGameProposalClosed(proposal);
			} else {
				storeGameProposal(proposal);
				fireGameProposalUpdated(proposal);
			}
			return proposal;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GameProposal<S> cancel(long proposalId, Player player) throws ViolatedRestrictionException {
		lock.lock();
		try {
			final AbstractGameProposal<S> proposal = (AbstractGameProposal<S>) proposals.get(proposalId);
			if (proposal == null) {
				return null;
			}
			if (proposal instanceof ChallengeGameProposal<?>) {
				final ChallengeGameProposal cgp = (ChallengeGameProposal) proposal;
				if (!cgp.getWaitingPlayers().contains(player) && !cgp.getInitiator().equals(player)) {
					throw new ViolatedRestrictionException("player.unsuitable");
				}
			} else {
				if (!proposal.getInitiator().equals(player)) {
					throw new ViolatedRestrictionException("player.unsuitable");
				}
			}

			proposals.remove(proposalId);
			removeGameProposal(proposal);
			fireGameProposalClosed(proposal);
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
	public Collection<GameProposal<S>> getActiveProposals() {
		lock.lock();
		try {
			return Collections.unmodifiableCollection(proposals.values());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Collection<GameProposal<S>> getPlayerProposals(Personality player) {
		lock.lock();
		try {
			final Collection<GameProposal<S>> res = new ArrayList<GameProposal<S>>();
			for (GameProposal<S> scribbleProposal : getActiveProposals()) {
				if (scribbleProposal.containsPlayer(player)) {
					res.add(scribbleProposal);
				}
			}
			return res;
		} finally {
			lock.unlock();
		}
	}

	protected abstract Collection<GameProposal<S>> loadGameProposals();

	protected abstract void storeGameProposal(GameProposal<S> proposal);

	protected abstract void removeGameProposal(GameProposal<S> proposal);

	protected void fireGameProposalInitiated(GameProposal proposal) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalInitiated(proposal);
		}
	}

	protected void fireGameProposalUpdated(GameProposal proposal) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalUpdated(proposal);
		}
	}

	protected void fireGameProposalClosed(GameProposal proposal) {
		for (GameProposalListener proposalListener : proposalListeners) {
			proposalListener.gameProposalClosed(proposal);
		}
	}
}
