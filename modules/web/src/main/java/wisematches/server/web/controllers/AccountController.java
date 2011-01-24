/*
 * Copyright (c) 2011, WiseMatches (by Sergey Klimenko).
 */

package wisematches.server.web.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import wisematches.server.player.*;
import wisematches.server.security.PlayerSecurityService;
import wisematches.server.web.forms.AccountLoginForm;
import wisematches.server.web.forms.AccountRegistrationForm;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Set;

/**
 * TODO: java docs
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping(value = "/account")
public class AccountController extends AbstractInfoController {
	private AccountManager accountManager;
	private PlayerSecurityService playerSecurityService;

	private int defaultRating = 1200;
	private Membership defaultMembership = Membership.BASIC;

	private static final Log log = LogFactory.getLog("wisematches.server.web.accoint");

	public AccountController() {
		super("classpath:/i18n/server/account/");
	}

	/**
	 * This is basic login page that shows small information about the site and login form.
	 *
	 * @param form   the empty login form. Required for FTL page.
	 * @param model  the original model
	 * @param locale the locale to get a info page
	 * @return the appropriate FTL layout page.
	 */
	@RequestMapping("login")
	public String loginPage(@ModelAttribute("login") AccountLoginForm form, Model model, Locale locale) {
		if (!processInfoPage("login", model, locale)) {
			return null;
		}
		model.addAttribute("accountBodyPageName", "login");
		return "/content/account/layout";
	}

	@RequestMapping("loginAuth")
	public String loginAction(@Valid @ModelAttribute("login") AccountLoginForm form,
							  BindingResult result,
							  HttpSession session, Model model, Locale locale) {
		if (log.isDebugEnabled()) {
			log.debug("Try authenticate user: " + form);
		}
		if (!result.hasErrors()) {
			final AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			if (log.isInfoEnabled()) {
				log.info("User can't be authenticated: " + ex);
			}

			if (ex != null) {
				if (ex instanceof BadCredentialsException) {
					result.rejectValue("j_password", "account.login.err.mismatch");
				} else if (ex instanceof RememberMeAuthenticationException) {
					//noinspection SpringMVCViewInspection
					return "redirect:/account/loginExpired.html";
				} else {
					result.rejectValue("j_password", "account.login.err.system");
				}

			}
		}
		if (!result.hasErrors()) {
			//noinspection SpringMVCViewInspection
			return "forward:/j_spring_security_check";
		}
		return loginPage(form, model, locale);
	}

	/**
	 * This is fake method. Implementation is provided by Spring Security.
	 *
	 * @return always null
	 */
	@RequestMapping("authMember")
	public String authMemberAction() {
		return null;
	}

	@RequestMapping("logout")
	public String logoutMemberAction() {
		return null;
	}

	/**
	 * This is basic form form. Just forward it to appropriate FTL page.
	 *
	 * @param model the associated model where {@code accountBodyPageName} parameter will be stored.
	 * @param form  the form form.
	 * @return the FTL full page name without extension
	 */
	@RequestMapping(value = "create", method = RequestMethod.GET)
	public String createAccountPage(Model model,
									@ModelAttribute("registration")
									AccountRegistrationForm form) {
		model.addAttribute("accountBodyPageName", "create");
		return "/content/account/layout";
	}

	/**
	 * This is action processor for new account. Get model from HTTP POST request and creates new account, if possible.	 *
	 *
	 * @param model  the all model
	 * @param form   the form request form
	 * @param result the errors result
	 * @param status the session status. Will be cleared in case of success
	 * @return the create account page in case of error of forward to {@code authMember} page in case of success.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public String createAccountAction(Model model,
									  @Valid @ModelAttribute("registration") AccountRegistrationForm form,
									  BindingResult result, SessionStatus status) {
		if (log.isInfoEnabled()) {
			log.info("Create new account request: " + form);
		}

		// Validate before next steps
		validateRegistrationForm(form, result);

		// Create account if no errors
		if (!result.hasErrors()) {
			try {
				createAccount(form);
			} catch (DuplicateAccountException ex) {
				final Set<String> fieldNames = ex.getFieldNames();
				if (fieldNames.contains("email")) {
					result.rejectValue("email", "account.register.form.email.err.busy");
				}
				if (fieldNames.contains("nickname")) {
					result.rejectValue("nickname", "account.register.form.username.err.incorrect");
				}
			} catch (InadmissibleUsernameException ex) {
				result.rejectValue("username", "account.register.form.username.err.incorrect");
			} catch (Exception ex) {
				log.error("Account can't be created", ex);
				result.reject("wisematches.error.internal");
			}
		}

		if (result.hasErrors()) {
			if (log.isInfoEnabled()) {
				log.info("Account form is not correct: " + result.toString());
			}
			return createAccountPage(model, form);
		} else {
			if (log.isInfoEnabled()) {
				log.info("Account has been created.");
			}

			status.setComplete();

			try {
				final StringBuilder b = new StringBuilder();
				b.append("j_username=").append(URLEncoder.encode(form.getEmail(), "UTF-8"));
				b.append("&");
				b.append("j_password=").append(URLEncoder.encode(form.getPassword(), "UTF-8"));
				if (form.isRememberMe()) {
					b.append("&").append("_remember_me=true");
				}
				//noinspection SpringMVCViewInspection
				return "forward:/j_spring_security_check";
			} catch (UnsupportedEncodingException ex) {
				log.error("Very strange exception that mustn't be here", ex);
				//noinspection SpringMVCViewInspection
				return "redirect:/account/login.html";
			}
		}
	}

	/**
	 * JSON request for email and username validation.
	 *
	 * @param email	the email to to checked.
	 * @param nickname the nickname to be checked
	 * @param result   the bind result that will be filled in case of any errors.
	 * @return the service response that also contains information about errors.
	 */
	@ResponseBody
	@RequestMapping(value = "checkAvailability")
	private ServiceResponse getAvailabilityStatus(@RequestParam("email") String email,
												  @RequestParam("nickname") String nickname,
												  BindingResult result) {
		if (log.isDebugEnabled()) {
			log.debug("Check account validation for: " + email + " (\"" + nickname + "\")");
		}

		final AccountAvailability a = accountManager.checkAccountAvailable(nickname, email);
		if (a.isAvailable()) {
			return ServiceResponse.success();
		} else {
			if (!a.isEmailAvailable()) {
				result.rejectValue("email", "account.register.email.err.busy");
			}
			if (!a.isUsernameAvailable()) {
				result.rejectValue("nickname", "account.register.nickname.err.busy");
			}
			if (!a.isUsernameProhibited()) {
				result.rejectValue("nickname", "account.register.nickname.err.incorrect");
			}
			return ServiceResponse.convert(result);
		}
	}


	// ==========================
	// Private implementation
	// ==========================

	/**
	 * Checks that specified form is valid. Otherwise fills specified result object.
	 *
	 * @param form   the form to be checked
	 * @param result the binding result to be filled in case of any error.
	 */
	private void validateRegistrationForm(AccountRegistrationForm form, BindingResult result) {
		if (!form.getPassword().equals(form.getConfirm())) {
			result.rejectValue("confirm", "account.register.pwd-cfr.err.mismatch");
		}
		getAvailabilityStatus(form.getEmail(), form.getNickname(), result);
	}

	/**
	 * Creates account based on specified form and returns created user.
	 *
	 * @param registration the new account form
	 * @return the create player
	 * @throws DuplicateAccountException	 if account with the same email or nickname already exist
	 * @throws InadmissibleUsernameException if nickname can't be used.
	 */
	private Player createAccount(AccountRegistrationForm registration) throws DuplicateAccountException, InadmissibleUsernameException {
		final PlayerEditor editor = new PlayerEditor();
		editor.setEmail(registration.getEmail());
		editor.setNickname(registration.getNickname());
		editor.setPassword(registration.getPassword());
		editor.setMembership(defaultMembership);
		editor.setRating(defaultRating);
		editor.setLanguage(Language.byCode(registration.getLanguage()));

		if (playerSecurityService != null) {
			editor.setPassword(playerSecurityService.encodePlayerPassword(editor.createPlayer(), registration.getPassword()));
		}
		return accountManager.createPlayer(editor.createPlayer());
	}


	// ==========================
	// Public Bean methods
	// ==========================

	public void setDefaultRating(int defaultRating) {
		this.defaultRating = defaultRating;
	}

	public void setDefaultMembership(String defaultMembership) {
		this.defaultMembership = Membership.valueOf(defaultMembership.toUpperCase());
	}

	@Autowired
	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@Autowired
	public void setPlayerSecurityService(PlayerSecurityService playerSecurityService) {
		this.playerSecurityService = playerSecurityService;
	}

}
