package wisematches.server.web.services.notify;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public enum NotificationMover {
	/**
	 * This is abstract e-mail notification.
	 */
	UNDEFINED("noreply"),

	/**
	 * From address is bugs reporter.
	 */
	SUPPORT("support"),

	/**
	 * Mail was sent from game state notifications team
	 */
	GAME("game-noreply"),

	/**
	 * Mail was sent from accounts support team.
	 */
	ACCOUNTS("account-noreply");

	private final String userInfo;

	NotificationMover(String userInfo) {
		this.userInfo = userInfo;
	}

	/**
	 * Returns user info for this sender
	 *
	 * @return the sender' user info
	 */
	public String getUserInfo() {
		return userInfo;
	}
}
