package wisematches.server.core.rating;

/**
 * This listener is used in {@code RatingsManager} to notify clients about changes in top players.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public interface TopPlayersListener {
    /**
     * Indicates that top players list was updated.
     *
     * @see RatingsManager#getTopRatedPlayers()
     */
    void topRatingsUpdated();
}