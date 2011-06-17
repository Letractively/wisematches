package wisematches.server.web.controllers.personality.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wisematches.personality.Language;
import wisematches.personality.account.*;
import wisematches.personality.player.Player;
import wisematches.server.security.AccountSecurityService;
import wisematches.server.web.controllers.AbstractPlayerController;
import wisematches.server.web.controllers.UnknownEntityException;
import wisematches.server.web.controllers.personality.settings.form.SettingsForm;
import wisematches.server.web.controllers.personality.settings.form.TimeZoneInfo;

import javax.validation.Valid;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/account/modify")
public class SettingsController extends AbstractPlayerController {
    private AccountManager accountManager;
    private AccountSecurityService accountSecurityService;

    private static final Log log = LogFactory.getLog("wisematches.server.web.settings");

    public SettingsController() {
    }

    @RequestMapping(value = "")
    public String modifyAccountPage(Model model, @ModelAttribute("settings") SettingsForm form) {
        final Player principal = getPrincipal();
        if (principal.getTimeZone() != null) {
            form.setTimezone(principal.getTimeZone().getID());
        }
        form.setLanguage(principal.getLanguage().name().toLowerCase());
        form.setEmail(principal.getEmail());
        model.addAttribute("timeZones", TimeZoneInfo.getTimeZones());
        return "/content/personality/settings/template";
    }

    @RequestMapping(value = "done")
    public String modifyAccountDone(Model model, @ModelAttribute("settings") SettingsForm form) {
        model.addAttribute("saved", Boolean.TRUE);
        return modifyAccountPage(model, form);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String modifyAccountAction(@Valid @ModelAttribute("settings") SettingsForm form,
                                      BindingResult errors, Model model) throws UnknownEntityException {
        final Account account = accountManager.getAccount(getPersonality().getId());
        if (account == null) {
            throw new UnknownEntityException(null, "account");
        }

        Language language = account.getLanguage();
        if (form.getLanguage() != null) {
            try {
                language = Language.valueOf(form.getLanguage().toUpperCase());
            } catch (IllegalArgumentException ex) {
                errors.rejectValue("language", "account.register.language.err.unknown");
            }
        }

        TimeZone timeZone = account.getTimeZone();
        if (form.getTimezone() != null) {
            timeZone = TimeZone.getTimeZone(form.getTimezone());
            if (timeZone == null) {
                errors.rejectValue("timezone", "account.register.timezone.err.unknown");
            }
        }

        if (form.isChangeEmail() && form.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "account.register.email.err.blank");
        }

        if (form.isChangePassword()) {
            if (form.getPassword().trim().isEmpty()) {
                errors.rejectValue("password", "account.register.pwd.err.blank");
            }
            if (form.getConfirm().trim().isEmpty()) {
                errors.rejectValue("confirm", "account.register.pwd-cfr.err.blank");
            }

            if (!form.getPassword().equals(form.getConfirm())) {
                errors.rejectValue("confirm", "account.register.pwd-cfr.err.mismatch");
            }
        }

        if (!errors.hasErrors()) {
            final AccountEditor editor = new AccountEditor(account);
            if (language != editor.getLanguage()) {
                editor.setLanguage(language);
            }
            if (!editor.getTimeZone().equals(timeZone)) {
                editor.setTimeZone(timeZone);
            }

            if (form.isChangeEmail() && !editor.getEmail().equals(form.getEmail())) {
                editor.setEmail(form.getEmail());
            }

            if (form.isChangePassword()) {
                if (accountSecurityService != null) {
                    editor.setPassword(accountSecurityService.encodePlayerPassword(editor.createAccount(), form.getPassword()));
                } else {
                    editor.setPassword(form.getPassword());
                }
            }

            try {
                final Account a = accountManager.updateAccount(editor.createAccount());
                if (accountSecurityService != null) {
                    accountSecurityService.authenticatePlayer(a, form.isChangePassword() ? form.getPassword() : null);
                }
                return "redirect:/account/modify/done";
            } catch (UnknownAccountException e) {
                throw new UnknownEntityException(null, "account");
            } catch (DuplicateAccountException ex) {
                final Set<String> fieldNames = ex.getFieldNames();
                if (fieldNames.contains("email")) {
                    errors.rejectValue("email", "account.register.form.email.err.busy");
                }
                if (fieldNames.contains("nickname")) {
                    errors.rejectValue("nickname", "account.register.form.username.err.incorrect");
                }
            } catch (InadmissibleUsernameException ex) {
                errors.rejectValue("username", "account.register.form.username.err.incorrect");
            } catch (Exception ex) {
                log.error("Account can't be created", ex);
                errors.reject("wisematches.error.internal");
            }
        }
        return modifyAccountPage(model, form);
    }

    @ModelAttribute("headerTitle")
    public String getHeaderTitle() {
        return "title.settings";
    }

    @Autowired
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Autowired
    public void setAccountSecurityService(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }
}