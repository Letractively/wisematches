package wisematches.playground.propose.impl;

import wisematches.playground.GameSettings;
import wisematches.playground.propose.GameProposal;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class MemoryProposalManager<S extends GameSettings> extends AbstractProposalManager<S> {
	public MemoryProposalManager() {
	}

	@Override
	protected Collection<GameProposal<S>> loadGameProposals() {
		return Collections.emptyList();
	}

	@Override
	protected void storeGameProposal(GameProposal<S> sGameProposal) {
	}

	@Override
	protected void removeGameProposal(GameProposal<S> sGameProposal) {
	}
}