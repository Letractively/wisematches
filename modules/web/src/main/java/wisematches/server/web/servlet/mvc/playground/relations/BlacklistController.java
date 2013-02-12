package wisematches.server.web.servlet.mvc.playground.relations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wisematches.core.PersonalityManager;
import wisematches.core.Player;
import wisematches.server.services.relations.blacklist.BlacklistManager;
import wisematches.server.web.i18n.GameMessageSource;
import wisematches.server.web.servlet.ServiceResponse;
import wisematches.server.web.servlet.mvc.WisematchesController;
import wisematches.server.web.servlet.mvc.playground.relations.form.BlacklistRecordForm;

import java.util.List;
import java.util.Locale;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/playground/blacklist")
public class BlacklistController extends WisematchesController {
	private BlacklistManager blacklistManager;
	private PersonalityManager playerManager;
	private GameMessageSource gameMessageSource;

	public BlacklistController() {
	}

	@RequestMapping("view")
	public String viewBlacklist(Model model) {
		model.addAttribute("blacklist", blacklistManager.getBlacklist(getPrincipal()));
		return "/content/playground/players/blacklist/view";
	}

	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ServiceResponse addToBlacklist(@RequestBody BlacklistRecordForm form, Locale locale) {
		final Player player = playerManager.getPlayer(form.getPerson());
		if (player == null) {
			return ServiceResponse.failure(gameMessageSource.getMessage("blacklist.err.unknown", locale));
		}
		blacklistManager.addPlayer(getPrincipal(), player, form.getComment());
		return ServiceResponse.SUCCESS;
	}

	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@RequestMapping(value = "remove", method = RequestMethod.POST)
	public ServiceResponse removeFromBlacklist(@RequestParam(value = "persons[]") List<Long> removeList) {
		final Player player = getPrincipal();
		for (Long id : removeList) {
			final Player player1 = playerManager.getPlayer(id);
			blacklistManager.removePlayer(player, player1);
		}
		return ServiceResponse.SUCCESS;
	}

	@Autowired
	public void setPersonalityManager(PersonalityManager playerManager) {
		this.playerManager = playerManager;
	}

	@Autowired
	public void setBlacklistManager(BlacklistManager blacklistManager) {
		this.blacklistManager = blacklistManager;
	}

	@Autowired
	public void setGameMessageSource(GameMessageSource gameMessageSource) {
		this.gameMessageSource = gameMessageSource;
	}
}