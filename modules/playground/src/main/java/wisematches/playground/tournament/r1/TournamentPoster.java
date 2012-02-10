package wisematches.playground.tournament.r1;

import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface TournamentPoster {
	/**
	 * Returns number of next tournament.
	 *
	 * @return the number of next tournament.
	 */
	int getNumber();

	/**
	 * Returns date for next scheduled tournament.
	 *
	 * @return the date for next scheduled tournament.
	 */
	Date getScheduledDate();
}