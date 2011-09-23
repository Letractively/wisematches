package wisematches.server.web.services.notify.impl.transform;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.context.MessageSource;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import wisematches.personality.account.Account;
import wisematches.server.web.services.notify.NotificationMover;
import wisematches.server.web.services.notify.NotificationPublisher;
import wisematches.server.web.services.notify.impl.Notification;
import wisematches.server.web.services.notify.impl.NotificationTransformer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class FreeMarkerNotificationTransformer implements NotificationTransformer {
	protected MessageSource messageSource;
	protected Configuration freeMarkerConfig;

	public FreeMarkerNotificationTransformer() {
	}

	@Override
	public Notification createNotification(String code, Account account, NotificationMover mover, NotificationPublisher publisher, Map<String, Object> model) throws Exception {
		final Locale locale = account.getLanguage().locale();

		final String subject = messageSource.getMessage("notify.subject." + code, null, locale);

		final Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("code", code);
		variables.put("locale", locale);
		variables.put("sender", mover);
		variables.put("subject", subject);
		variables.put("principal", account);
		variables.put("publisher", publisher.getPublisherName());
		if (model != null) {
			variables.putAll(model);
		}

		final Template template = freeMarkerConfig.getTemplate("layout.ftl", locale, "UTF-8");
		final String message = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
		return new Notification(code, subject, message, account, mover);
	}

	public void setFreeMarkerConfig(Configuration freeMarkerConfig) {
		this.freeMarkerConfig = freeMarkerConfig;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public Configuration getFreeMarkerConfig() {
		return freeMarkerConfig;
	}
}