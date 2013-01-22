package wisematches.playground.tracking;

import wisematches.core.personality.Player;

import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface StatisticManager {
	void addStatisticListener(StatisticsListener l);

	void removeStatisticListener(StatisticsListener l);

	/**
	 * Returns current player's rating.
	 *
	 * @param player the player who's rating must be returned.
	 * @return the player's rating.
	 * @throws IllegalArgumentException if specified player out of this manager.
	 */
	short getRating(Player player);

	/**
	 * Returns statistic for specified player.
	 *
	 * @param player the player id.
	 * @return the player statistic.
	 */
	Statistics getStatistic(Player player);

	/**
	 * Returns rating curve container that contains information about all rating changes for specified player
	 * for specified dates with specified resolution.
	 *
	 * @param player     the player who's curve should be returned.
	 * @param resolution the curve resolution. Indicates how many days must be grouped for one point. It's not possible
	 *                   to get curve with resolution less that one day at this moment.
	 * @param startDate  startDate date. If null all range will be used.
	 * @param endDate    endDate date. If null today will be used
	 * @return the rating curve.
	 * @throws IllegalArgumentException if resolution if zero or negative.
	 * @throws NullPointerException     if {@code player} is null
	 */
	RatingCurve getRatingCurve(Player player, int resolution, Date startDate, Date endDate);
}
