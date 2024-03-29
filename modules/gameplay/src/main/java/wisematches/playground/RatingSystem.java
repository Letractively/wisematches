package wisematches.playground;

/**
 * Rating System is implementation of rating system that allows calculates rating of players
 * when game was finished.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public interface RatingSystem {
	/**
	 * Returns rating system name.
	 *
	 * @return the rating system name.
	 */
	String getName();

	/**
	 * Calculates rating of specified players. This method returns array of new ratings for each
	 * player in specified {@code players} array.
	 *
	 * @param ratings the current ratings
	 * @param points  the players points for finished game.
	 * @return the array of new ratings for each specified player.
	 * @throws NullPointerException     if {@code players } or {@code points} arrays are {@code null}
	 *                                  or any of {@code players} element is {@code null}
	 * @throws IllegalArgumentException if length of {@code points} array is not equals with
	 *                                  {@code players} array length or any of {@code points} element is less than zero.
	 */
	short[] calculateRatings(short[] ratings, short[] points);
}
