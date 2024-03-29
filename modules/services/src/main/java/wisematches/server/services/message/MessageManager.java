package wisematches.server.services.message;

import wisematches.core.Player;
import wisematches.core.task.CleaningDayListener;

import java.util.Collection;

/**
 * {@code MessageManager} provides ability to send/receive short messages between personalities.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface MessageManager extends CleaningDayListener {
	void addMessageListener(MessageListener l);

	void removeMessageListener(MessageListener l);

	/**
	 * Sent a message to specified player
	 *
	 * @param sender    the sender of the message
	 * @param recipient the player who should received the message
	 * @param body      the message's body
	 * @throws NullPointerException     if sender or recipient is null
	 * @throws IllegalArgumentException if body is null or empty
	 */
	void sendMessage(Player sender, Player recipient, String body);

	/**
	 * Sent a message to specified player
	 *
	 * @param sender    the sender of the message
	 * @param recipient the player who should received the message
	 * @param body      the message's body
	 * @param quite     indicates is notification should be quite or note.
	 * @throws NullPointerException     if sender or recipient is null
	 * @throws IllegalArgumentException if body is null or empty
	 */
	void sendMessage(Player sender, Player recipient, String body, boolean quite);

	/**
	 * Reply to specified message.
	 *
	 * @param sender  the sender of the message
	 * @param message the original message
	 * @param body    the message's body
	 * @throws NullPointerException     if sender, recipient or message is null
	 * @throws IllegalArgumentException if body is null or empty
	 */
	void replyMessage(Player sender, Message message, String body);

	/**
	 * Reply to specified message.
	 *
	 * @param sender  the sender of the message
	 * @param message the original message
	 * @param body    the message's body
	 * @param quite   indicates is notification should be quite or note.
	 * @throws NullPointerException     if sender, recipient or message is null
	 * @throws IllegalArgumentException if body is null or empty
	 */
	void replyMessage(Player sender, Message message, String body, boolean quite);

	/**
	 * Sent notification to specified player with specified body
	 *
	 * @param recipient the player who should received the message
	 * @param body      the message's body
	 * @throws NullPointerException     if recipient is null
	 * @throws IllegalArgumentException if recipient is unknown or {@code body} is empty.
	 */
	void sendNotification(Player recipient, String body);

	/**
	 * Sent notification to specified player with specified body
	 *
	 * @param recipient the player who should received the message
	 * @param body      the message's body
	 * @param quite     indicates is notification should be quite or note.
	 * @throws NullPointerException     if recipient is null
	 * @throws IllegalArgumentException if recipient is unknown or {@code body} is empty.
	 */
	void sendNotification(Player recipient, String body, boolean quite);

	/**
	 * Returns a message with specified id.
	 *
	 * @param id the message id
	 * @return the message or {@code null} if message unknown
	 */
	Message getMessage(long id);

	/**
	 * Returns new messages count.
	 *
	 * @param personality the person who's messages count should be received.
	 * @return the number of messages received from last time
	 */
	int getNewMessagesCount(Player personality);

	/**
	 * Returns number of messages sent today.
	 *
	 * @param personality the person who's count should be returned.
	 * @param direction   the message direction.
	 * @return the number of messages sent today.
	 */
	int getTodayMessagesCount(Player personality, MessageDirection direction);

	/**
	 * Returns collection of all message for specified person.
	 *
	 * @param person    the person who's message should be returned.
	 * @param direction the message direction.
	 * @return the collection of all message.
	 */
	Collection<Message> getMessages(Player person, MessageDirection direction);

	/**
	 * Removes a message with specified id
	 *
	 * @param person    a person who's message should be removed
	 * @param messageId the message id
	 * @param direction the message direction.
	 */
	void removeMessage(Player person, long messageId, MessageDirection direction);
}
