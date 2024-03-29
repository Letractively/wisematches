package wisematches.playground.tourney.regular;

import wisematches.core.Language;
import wisematches.playground.tourney.TourneyEntity;

import java.util.EnumSet;

/**
 * A tourney round belong to a tourney and
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface TourneyRound extends RegularTourneyEntity<TourneyRound, TourneyRound.Id, TourneyRound.Context> {
	/**
	 * Returns number of the round.
	 *
	 * @return the number of the round.
	 */
	int getRound();

	/**
	 * Returns total number of groups in this round.
	 *
	 * @return the total number of groups in this round.
	 */
	int getGroupsCount();

	/**
	 * Returns nusmber of total games in the round.
	 *
	 * @return the number of total games in the round.
	 */
	int getTotalGamesCount();

	/**
	 * Returns number of finished games.
	 *
	 * @return the number of finished games.
	 */
	int getFinishedGamesCount();

	/**
	 * Returns division for this round
	 *
	 * @return the division for this round
	 */
	TourneyDivision getDivision();

	/**
	 * Indicates that this round is final.
	 *
	 * @return {@code true} if this round is final; {@code false} - otherwise.
	 */
	boolean isFinal();

	public final class Id extends TourneyEntity.Id<TourneyRound, Id> {
		private TourneyDivision.Id divisionId;
		private int round;

		private Id() {
		}

		public Id(int tourney, Language language, TourneySection section, int round) {
			this(new TourneyDivision.Id(tourney, language, section), round);
		}

		public Id(TourneyDivision.Id divisionId, int round) {
			this.divisionId = divisionId;
			this.round = round;
		}

		public int getRound() {
			return round;
		}

		public TourneyDivision.Id getDivisionId() {
			return divisionId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Id id = (Id) o;
			return round == id.round && divisionId.equals(id.divisionId);
		}

		@Override
		public int hashCode() {
			int result = divisionId.hashCode();
			result = 31 * result + round;
			return result;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Id");
			sb.append("{divisionId=").append(divisionId);
			sb.append(", round=").append(round);
			sb.append('}');
			return sb.toString();
		}
	}

	public final class Context extends TourneyEntity.Context<TourneyRound, Context> {
		private Tourney.Id tourneyId;
		private TourneyDivision.Id divisionId;

		public Context(EnumSet<State> states) {
			super(states);
		}

		public Context(TourneyDivision.Id divisionId, EnumSet<State> states) {
			super(states);
			this.divisionId = divisionId;
		}

		public Context(Tourney.Id tourneyId, EnumSet<State> states) {
			super(states);
			this.tourneyId = tourneyId;
		}

		public Tourney.Id getTourneyId() {
			return tourneyId;
		}

		public TourneyDivision.Id getDivisionId() {
			return divisionId;
		}
	}
}
