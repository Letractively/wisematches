package wisematches.server.services.notify;

import wisematches.core.Member;

/**
 * {@code NotificationDistributor} is main notification distribution interface. It prepares
 * notification, check is it should be processed and so on and sends to all known publishers.
 * <p/>
 * The {@code NotificationDistributor} is always asynchronous. There is no warranty that notification was sent
 * after method invoking.
 * <p/>
 * There is no way to track notification errors. If you need to be sure that notification/email was sent please
 * use appropriate {@code NotificationPublisher}.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface NotificationService {
	Notification raiseNotification(String code, Member target, NotificationSender sender, Object context) throws NotificationException;
}