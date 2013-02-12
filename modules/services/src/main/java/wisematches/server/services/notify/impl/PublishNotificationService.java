package wisematches.server.services.notify.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wisematches.core.Player;
import wisematches.core.task.TransactionalExecutor;
import wisematches.server.services.notify.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class PublishNotificationService implements NotificationService {
	private TransactionalExecutor taskExecutor;
	private NotificationManager notificationManager;
	private NotificationConverter notificationConverter;

	private final Collection<NotificationPublisher> publishers = new ArrayList<>();

	private static final Log log = LogFactory.getLog("wisematches.server.notice.service");

	public PublishNotificationService() {
	}

	@Override
	public Notification raiseNotification(String code, Player target, NotificationSender sender, Object context) throws NotificationException {
		final NotificationDescriptor descriptor = notificationManager.getDescriptor(code);
		if (descriptor == null) {
			return null;
		}
		final NotificationScope notificationScope = notificationManager.getNotificationScope(code, target);
		final NotificationScope personalScope = notificationScope != null ? notificationScope : NotificationScope.EXTERNAL;

		final Notification notification = notificationConverter.createNotification(code, target, sender, context);

		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (final NotificationPublisher publisher : publishers) {
					final NotificationScope publisherScope = publisher.getNotificationScope();
					if ((publisherScope.isInternal() && personalScope.isInternal()) ||
							(publisherScope.isExternal() && personalScope.isExternal())) {
						try {
							publisher.publishNotification(notification);
						} catch (NotificationException ex) {
							log.error("Notification can't be processed: code=" + notification.getCode() + ",publisher=" + publisher.getName(), ex);
						}
					}
				}
			}
		});
		return notification;
	}

	public void setTaskExecutor(TransactionalExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setNotificationManager(NotificationManager notificationManager) {
		this.notificationManager = notificationManager;
	}

	public void setNotificationConverter(NotificationConverter notificationConverter) {
		this.notificationConverter = notificationConverter;
	}

	public void setNotificationPublishers(Collection<NotificationPublisher> publishers) {
		this.publishers.clear();

		if (publishers != null) {
			this.publishers.addAll(publishers);
		}
	}
}