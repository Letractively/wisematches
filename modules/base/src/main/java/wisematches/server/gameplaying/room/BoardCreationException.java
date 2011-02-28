package wisematches.server.gameplaying.room;

/**
 * This exception is thrown by <code>RoomManager</code> if new game board can't be created
 * by some reasone.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class BoardCreationException extends Exception {
	/**
	 * {@inheritDoc}
	 */
	public BoardCreationException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public BoardCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
