package wisematches.server.web.servlet.mvc.account;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import wisematches.core.Member;
import wisematches.core.personality.player.account.*;
import wisematches.server.services.notify.NotificationSender;
import wisematches.server.services.notify.NotificationService;
import wisematches.server.web.security.web.captcha.CaptchaService;
import wisematches.server.web.servlet.mvc.WisematchesController;
import wisematches.server.web.servlet.mvc.account.form.RecoveryConfirmationForm;
import wisematches.server.web.servlet.mvc.account.form.RecoveryRequestForm;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/account/recovery")
public class RecoveryController extends WisematchesController {
	private CaptchaService captchaService;
	private AccountManager accountManager;
	private NotificationService notificationService;
	private AccountRecoveryManager recoveryTokenManager;

	private static final String RECOVERING_PLAYER_EMAIL = "RECOVERY_PLAYER_EMAIL";

	private static final Logger log = LoggerFactory.getLogger("wisematches.web.mvc.RecoveryController");

	public RecoveryController() {
	}

	@RequestMapping(value = "request")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String recoveryRequestPage(HttpSession session, Model model, @Valid @ModelAttribute("recovery") RecoveryRequestForm form, BindingResult result) {
		log.info("Recovery password for {}", form);

		if (form.isRecoveryAccount()) {
			try {
				final Account account = accountManager.findByEmail(form.getEmail());
				if (account != null) {
					final RecoveryToken token = recoveryTokenManager.generateToken(account);
					log.info("Recovery token generated: {}", token);

					final Map<String, Object> mailModel = new HashMap<>();
					mailModel.put("principal", account);
					mailModel.put("recoveryToken", token.getToken());

					final Member member = personalityManager.getMember(account.getId());
					notificationService.raiseNotification("account.recovery", member, NotificationSender.ACCOUNTS, mailModel);
					session.setAttribute(RECOVERING_PLAYER_EMAIL, account.getEmail());
					return "redirect:/account/recovery/confirmation";
				} else {
					result.rejectValue("email", "account.recovery.err.unknown");
				}
			} catch (Exception ex) {
				log.error("Recovery password email can't be delivered", ex);
				result.rejectValue("email", "account.recovery.err.system");
			}
		}
		model.addAttribute("resourceTemplate", "/content/account/recovery/request.ftl");
		return "/content/assistance/help";
	}

	@RequestMapping(value = "confirmation")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String recoveredConfirmationAction(NativeWebRequest request, HttpSession session,
											  @Valid @ModelAttribute("recovery") RecoveryConfirmationForm form,
											  BindingResult result, Model model) {
		log.info("Process recovery confirmation: {}", form);

		boolean notificationWasSent = false;
		String email = form.getEmail();
		if (isEmpty(email)) {
			email = (String) session.getAttribute(RECOVERING_PLAYER_EMAIL);
			notificationWasSent = (email != null && !email.isEmpty());
			form.setEmail(email);
		}
		session.removeAttribute(RECOVERING_PLAYER_EMAIL);

		if (isEmpty(email)) {
			return "redirect:/account/recovery/request";
		}

		if (form.isRecoveryAccount()) {
			final Account account = checkRecoveryForm(request, form, result);
			if (!result.hasErrors()) {
				final AccountEditor e = new AccountEditor(account);
				try {
					recoveryTokenManager.clearToken(account); // remove token. Mandatory operation or expired exception will be thrown
					final Account account1 = accountManager.updateAccount(e.createAccount(), form.getPassword());

					final Member member = personalityManager.getMember(account.getId());
					notificationService.raiseNotification("account.updated", member, NotificationSender.ACCOUNTS, member);
					return AccountController.forwardToAuthorization(request, account1, form.isRememberMe(), null);
				} catch (Exception e1) {
					result.rejectValue("email", "account.recovery.err.system");
				}
			}
		}
		model.addAttribute("submittedEmail", email);
		model.addAttribute("notificationWasSent", notificationWasSent);
		model.addAttribute("resourceTemplate", "/content/account/recovery/confirmation.ftl");
		return "/content/assistance/help";
	}

	private Account checkRecoveryForm(NativeWebRequest request, RecoveryConfirmationForm form, BindingResult result) {
		if (isEmpty(form.getEmail())) {
			result.rejectValue("email", "account.register.email.err.blank");
		}

		if (isEmpty(form.getPassword())) {
			result.rejectValue("password", "account.register.pwd.err.blank");
		}

		if (isEmpty(form.getToken())) {
			result.rejectValue("token", "account.recovery.err.token", new Object[]{form.getEmail()}, null);
		}

		if (isEmpty(form.getConfirm())) {
			result.rejectValue("confirm", "account.register.pwd-cfr.err.blank");
		} else if (!form.getPassword().equals(form.getConfirm())) {
			result.rejectValue("confirm", "account.register.pwd-cfr.err.mismatch");
		}

		if (captchaService != null) {
			captchaService.validateCaptcha(request, result);
		}

		Account player = null;
		try {
			player = accountManager.findByEmail(form.getEmail());
			if (player != null) {
				final RecoveryToken token = recoveryTokenManager.getToken(player);
				if (token == null) {
					result.rejectValue("token", "account.recovery.err.expired", new Object[]{form.getEmail()}, null);
				} else if (!token.getToken().equals(form.getToken())) {
					result.rejectValue("token", "account.recovery.err.token", new Object[]{form.getEmail()}, null);
				}
			} else {
				result.rejectValue("email", "account.recovery.err.unknown");
			}
		} catch (Exception ex) {
			result.rejectValue("token", "account.recovery.err.system");
		}
		return player;
	}

	private boolean isEmpty(String email) {
		return email == null || email.isEmpty();
	}

	@Autowired
	public void setCaptchaService(CaptchaService captchaService) {
		this.captchaService = captchaService;
	}

	@Autowired
	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@Autowired
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Autowired
	public void setRecoveryTokenManager(AccountRecoveryManager recoveryTokenManager) {
		this.recoveryTokenManager = recoveryTokenManager;
	}
}
