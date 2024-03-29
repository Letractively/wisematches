package wisematches.playground;

/**
 * This exception is thrown by <code>BoardManager</code> if new game board can't be created
 * by some reason.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class BoardCreationException extends BoardException {
	public BoardCreationException(String message) {
		super(message);
	}

	public BoardCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
