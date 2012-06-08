package wisematches.playground;

import wisematches.personality.Personality;
import wisematches.playground.search.SearchFilter;
import wisematches.playground.search.SearchManager;

import java.util.Collection;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface BoardManager<S extends GameSettings, B extends GameBoard<S, ?>> extends SearchManager<B, GameState, SearchFilter> {
	void addBoardStateListener(BoardStateListener l);

	void removeBoardStateListener(BoardStateListener l);

	/**
	 * Returns game by it's id.
	 *
	 * @param gameId the game id
	 * @return the game by specified id or <code>null</code> if game is unknown.
	 * @throws BoardLoadingException if board can't be loaded by some reasons.
	 */
	B openBoard(long gameId) throws BoardLoadingException;

	/**
	 * Creates new game board with specified settings.
	 *
	 * @param gameSettings the settings for new game
	 * @param players      the list of players.
	 * @return the created game.
	 * @throws BoardCreationException if board can't be created by some reasons.
	 */
	B createBoard(S gameSettings, Collection<? extends Personality> players) throws BoardCreationException;
}
