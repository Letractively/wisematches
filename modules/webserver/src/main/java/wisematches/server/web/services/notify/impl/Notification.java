package wisematches.server.web.services.notify.impl;

import wisematches.personality.account.Account;
import wisematches.server.web.services.notify.NotificationSender;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class Notification {
	private final String code;
	private final String subject;
	private final String message;
	private final Account account;
	private final NotificationSender sender;

	public Notification(String code, String subject, String message, Account account, NotificationSender sender) {
		this.code = code;
		this.subject = subject;
		this.message = message;
		this.account = account;
		this.sender = sender;
	}

	public String getCode() {
		return code;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public Account getAccount() {
		return account;
	}

	public NotificationSender getSender() {
		return sender;
	}
}
