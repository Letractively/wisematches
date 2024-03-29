package wisematches.playground.propose;

/**
 * Proposal type shows how the proposal should be tracked. Depends on the type a proposal can have differ behaviour.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public enum ProposalType {
	/**
	 * Public proposal when any player that meets some {@code PlayerCriterion} can join the proposal.
	 */
	PUBLIC,

	/**
	 * Private proposal and only pre-defined player can join the proposal.
	 */
	PRIVATE;

	public boolean isPublic() {
		return this == PUBLIC;
	}

	public boolean isPrivate() {
		return this == PRIVATE;
	}
}
