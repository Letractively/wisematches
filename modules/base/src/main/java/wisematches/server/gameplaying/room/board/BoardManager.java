package wisematches.server.gameplaying.room.board;

import wisematches.server.gameplaying.board.GameBoard;
import wisematches.server.gameplaying.board.GameSettings;
import wisematches.server.player.Player;

import java.util.Collection;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface BoardManager<S extends GameSettings, B extends GameBoard<S, ?>> {
	void addBoardStateListener(BoardStateListener l);

	void removeBoardStateListener(BoardStateListener l);

	/**
	 * Creates new game board with specified settings.
	 *
	 * @param gameSettings the settings for new game
	 * @param players	  the list of players.
	 * @return the created game.
	 * @throws BoardCreationException if board can't be created by some reasons.
	 */
	B createBoard(S gameSettings, Collection<Player> players) throws BoardCreationException;

	/**
	 * Returns game by it's id.
	 *
	 * @param gameId the game id
	 * @return the game by specified id or <code>null</code> if game is unknown.
	 * @throws BoardLoadingException if board can't be loaded by some reasons.
	 */
	B openBoard(long gameId) throws BoardLoadingException;

	/**
	 * Updates specified board in boards storage. Save all changes in board to storage.
	 * <p/>
	 * This operation should be used only if {@code GamePlayerHand} is changed. In other cases
	 * {@code wisematches.server.core.room.RoomManager} automatical store board into storage after
	 * any changes.
	 *
	 * @param board the board to be updated.
	 * @throws BoardUpdatingException if board can't be loaded by some reasons.
	 */
	void updateBoard(B board) throws BoardUpdatingException;

	/**
	 * Returns collection of all opened boards at this moment. This methods returns copy of boards
	 * and should not be used in time/memory critical operations.
	 * <p/>
	 * Base scenario for this method: initialization of listeners for already opened boards.
	 *
	 * @return the copy of collection of all opened boards.
	 */
	Collection<B> getOpenedBoards();

	/**
	 * Returns list of all games for specified players that have specified state. If no ony game exist empty
	 * collection will be returned.
	 *
	 * @param player the player who games should be returned.
	 * @return the list of player's games or empty collection.
	 */
	Collection<B> getActiveBoards(Player player);
}