package wisematches.core.personality.player.account;

/**
 * Indicates that account that should be updated or removed is unknown.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class UnknownAccountException extends AccountException {
	public UnknownAccountException(Account account) {
		super("UnknownAccountException: " + account, account);
	}

	public UnknownAccountException(Throwable cause, Account player) {
		super("UnknownAccountException: " + player, cause, player);
	}
}
