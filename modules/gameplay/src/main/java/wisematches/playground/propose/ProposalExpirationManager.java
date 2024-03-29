package wisematches.playground.propose;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import wisematches.core.Player;
import wisematches.core.expiration.impl.AbstractExpirationManager;
import wisematches.playground.GameSettings;

import java.util.Date;
import java.util.List;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class ProposalExpirationManager<S extends GameSettings> extends AbstractExpirationManager<Long, ProposalExpirationType> implements InitializingBean {
	private long intentionExpirationMillis = 0; // never
	private long challengeExpirationMillis = 7 * 24 * 60 * 60 * 1000; // 7 days

	private GameProposalManager<S> proposalManager;

	private final TheGameProposalListener gameProposalListener = new TheGameProposalListener();

	public ProposalExpirationManager() {
		super(ProposalExpirationType.class, LoggerFactory.getLogger("wisematches.proposal.ExpirationManager"));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		lock.lock();
		try {
			final List<GameProposal<S>> gameProposals = proposalManager.searchEntities(null, ProposalRelation.AVAILABLE, null, null);
			for (GameProposal<S> proposal : gameProposals) {
				scheduleProposalTermination(proposal);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected boolean executeTermination(Long id) {
		return (proposalManager.terminate(id) != null);
	}

	public void setIntentionExpirationMillis(long intentionExpirationMillis) {
		this.intentionExpirationMillis = intentionExpirationMillis;
	}

	public void setChallengeExpirationMillis(long challengeExpirationMillis) {
		this.challengeExpirationMillis = challengeExpirationMillis;
	}

	public void setProposalManager(GameProposalManager<S> proposalManager) {
		lock.lock();
		try {
			if (this.proposalManager != null) {
				this.proposalManager.removeGameProposalListener(gameProposalListener);
			}

			this.proposalManager = proposalManager;

			if (this.proposalManager != null) {
				this.proposalManager.addGameProposalListener(gameProposalListener);
			}
		} finally {
			lock.unlock();
		}
	}

	private void scheduleProposalTermination(GameProposal<? extends GameSettings> proposal) {
		if (proposal.getProposalType() == ProposalType.PRIVATE && challengeExpirationMillis > 0) {
			scheduleTermination(proposal.getId(), new Date(proposal.getCreationDate().getTime() + challengeExpirationMillis));
		} else if (proposal.getProposalType() == ProposalType.PUBLIC && intentionExpirationMillis > 0) {
			scheduleTermination(proposal.getId(), new Date(proposal.getCreationDate().getTime() + intentionExpirationMillis));
		}
	}

	private class TheGameProposalListener implements GameProposalListener {
		private TheGameProposalListener() {
		}

		@Override
		public void gameProposalInitiated(GameProposal<? extends GameSettings> proposal) {
			scheduleProposalTermination(proposal);
		}

		@Override
		public void gameProposalUpdated(GameProposal<? extends GameSettings> proposal, Player player, ProposalDirective directive) {
		}

		@Override
		public void gameProposalFinalized(GameProposal<? extends GameSettings> proposal, Player player, ProposalResolution reason) {
			cancelTermination(proposal.getId());
		}
	}
}
