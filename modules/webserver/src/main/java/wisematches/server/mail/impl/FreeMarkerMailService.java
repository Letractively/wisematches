package wisematches.server.mail.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import wisematches.personality.Language;
import wisematches.personality.account.Account;
import wisematches.personality.player.PlayerManager;
import wisematches.server.mail.*;
import wisematches.server.web.i18n.GameMessageSource;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class FreeMarkerMailService implements MailService {
	private String serverHostName = "wisematches.net";
	private JavaMailSender mailSender;
	private PlayerManager playerManager;
	private GameMessageSource gameMessageSource;
	private MessageSource messageSource;
	private Configuration freeMarkerConfig;

	private String supportSenderAddress = "support@wm.net";
	private String supportRecipientAddress = "support@wm.net";

	private final Map<SenderKey, InternetAddress> addressesCache = new HashMap<SenderKey, InternetAddress>();

	private static final Pattern TITLE = Pattern.compile("\\s*<title>(.*)</title>\\s*");

	protected static final Log log = LogFactory.getLog("wisematches.server.mail");

	public FreeMarkerMailService() {
	}

	@Override
	public void sendMail(SenderName from, Account to, String msgCode, Map<String, ?> model) {
		try {
			sendWarrantyMail(from, to, msgCode, model);
		} catch (MailException ex) {
			log.fatal("Mail can't be send: from=" + from + ", player=" + to + ", msgCode=" + msgCode + ", model=" + model, ex);
		}
	}

	@Override
	public void sendWarrantyMail(SenderName from, Account to, String msgCode, Map<String, ?> model) throws MailException {
		sendMail(preparePlayerMessage(from, to, msgCode, model));
	}

	@Override
	public void sendSupportRequest(String subject, String msgCode, Map<String, ?> model) throws MailException {
		sendMail(prepareSupportMessage(subject, msgCode, model));
	}

	protected void sendMail(MimeMessagePreparator preparator) throws MailException {
		try {
			log.info("Sending email message " + preparator);

			mailSender.send(preparator);
		} catch (MailPreparationException ex) {
			throw new MailTemplateException("", ex);
		} catch (MailParseException ex) {
			throw new MailTemplateException("", ex);
		} catch (MailSendException ex) {
			throw new MailTransportException("", ex);
		} catch (MailAuthenticationException ex) {
			throw new MailTransportException("", ex);
		} catch (Exception ex) {
			throw new MailException("", ex);
		}
	}

	protected MimeMessagePreparator prepareSupportMessage(final String subject, final String msgCode, final Map<String, ?> model) {
		return new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				try {
					final Language language = Language.EN;
					final Template template = getTemplate(msgCode, language);

					final Map<String, Object> variables = new HashMap<String, Object>();
					if (model != null) {
						variables.putAll(model);
					}

					final String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
					final InternetAddress to = new InternetAddress(supportRecipientAddress);
					final InternetAddress from = new InternetAddress(supportSenderAddress);

					final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
					message.setFrom(from);
					message.setTo(to);
					message.setSubject(subject);
					message.setText(text, true);
				} catch (Exception ex) {
					throw new MailTemplateException("Mail template can't be created", ex);
				}
			}


			@Override
			public String toString() {
				return "Support EMail message with subject: " + subject + ",msgCode: " + msgCode;
			}
		};
	}

	protected MimeMessagePreparator preparePlayerMessage(final SenderName senderName, final Account player, final String msgCode, final Map<String, ?> model) {
		return new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				try {
					final Language language = player.getLanguage();
					final Template template = getTemplate(msgCode, language);

					final Map<String, Object> variables = new HashMap<String, Object>();
					variables.put("principal", player);
					variables.put("locale", language.locale());
					variables.put("playerManager", playerManager);
					variables.put("gameMessageSource", gameMessageSource);
					variables.put("messageCode", msgCode);
					variables.put("serverHostName", serverHostName);
					variables.put("senderName", senderName.getDefaultName());
					if (model != null) {
						variables.putAll(model);
					}

					final String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
					final Matcher matcher = TITLE.matcher(text);
					if (!matcher.find()) {
						throw new MailTemplateException("Template does not have subject line at a top");
					}

					final InternetAddress to = new InternetAddress(player.getEmail(), player.getNickname(), "UTF-8");
					final InternetAddress from = getInternetAddress(senderName, language);

					final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, "UTF-8");
					message.setFrom(from);
					message.setTo(to);
					message.setSubject(matcher.group(1));
					message.setText(text, true);
				} catch (MailTemplateException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new MailTemplateException("Mail template can't be created", ex);
				}
			}

			@Override
			public String toString() {
				return "Player EMail message from: " + senderName + ",to: " + player + ",msgCode:" + msgCode;
			}
		};
	}

	protected Template getTemplate(String msgCode, Language language) throws IOException {
		return freeMarkerConfig.getTemplate(msgCode + ".ftl", language.locale(), "UTF-8");
	}

	protected InternetAddress getInternetAddress(SenderName senderName, Language language) {
		return addressesCache.get(new SenderKey(language, senderName));
	}

	private void validateAddressesCache() {
		addressesCache.clear();

		if (messageSource == null || serverHostName == null) {
			return;
		}

		for (SenderName senderName : SenderName.values()) {
			for (Language language : Language.values()) {
				try {
					final String address = messageSource.getMessage("mail.address." + senderName.getDefaultName(),
							null, senderName.getDefaultName() + "@" + serverHostName, language.locale());

					final String personal = messageSource.getMessage("mail.personal." + senderName.getDefaultName(),
							null, senderName.name(), language.locale());

					addressesCache.put(
							new SenderKey(language, senderName),
							new InternetAddress(address, personal, "UTF-8"));
				} catch (UnsupportedEncodingException ex) {
					log.fatal("JAVA SYSTEM ERROR - NOT UTF8!", ex);
				}
			}
		}
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setFreeMarkerConfig(Configuration freeMarkerConfig) {
		this.freeMarkerConfig = freeMarkerConfig;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
		validateAddressesCache();
	}

	public void setSupportSenderAddress(String supportSenderAddress) {
		this.supportSenderAddress = supportSenderAddress;
	}

	public void setSupportRecipientAddress(String supportRecipientAddress) {
		this.supportRecipientAddress = supportRecipientAddress;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
		validateAddressesCache();
	}

	public void setPlayerManager(PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	public void setGameMessageSource(GameMessageSource gameMessageSource) {
		this.gameMessageSource = gameMessageSource;
	}

	private static final class SenderKey {
		private final Language language;
		private final SenderName senderNameAccount;

		private SenderKey(Language language, SenderName senderNameAccount) {
			this.language = language;
			this.senderNameAccount = senderNameAccount;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SenderKey senderKey = (SenderKey) o;
			return language == senderKey.language && senderNameAccount == senderKey.senderNameAccount;
		}

		@Override
		public int hashCode() {
			int result = language.hashCode();
			result = 31 * result + senderNameAccount.hashCode();
			return result;
		}
	}
}
