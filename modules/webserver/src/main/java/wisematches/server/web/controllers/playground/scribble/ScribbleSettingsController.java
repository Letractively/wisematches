package wisematches.server.web.controllers.playground.scribble;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import wisematches.personality.player.Player;
import wisematches.playground.scribble.settings.BoardSettings;
import wisematches.playground.scribble.settings.BoardSettingsManager;
import wisematches.server.web.controllers.ServiceResponse;
import wisematches.server.web.controllers.WisematchesController;
import wisematches.server.web.controllers.playground.scribble.form.BoardSettingsForm;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@RequestMapping("/playground/scribble/settings")
@Controller
public class ScribbleSettingsController extends WisematchesController {
	private BoardSettingsManager boardSettingsManager;

	private static final Log log = LogFactory.getLog("wisematches.server.web.playboard");

	public ScribbleSettingsController() {
	}

	@RequestMapping("load")
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String loadBoardSettings(final Model model, @ModelAttribute("settings") final BoardSettingsForm form) {
		final Player principal = getPrincipal();

		final BoardSettings settings = boardSettingsManager.getScribbleSettings(principal);
		form.setTilesClass(settings.getTilesClass());
		form.setCheckWords(settings.isCheckWords());
		form.setCleanMemory(settings.isCleanMemory());

		model.addAttribute("plain", Boolean.TRUE);

		return "/content/playground/scribble/settings";
	}


	@ResponseBody
	@RequestMapping("save")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ServiceResponse saveBoardSettings(final Model model, @ModelAttribute("settings") final BoardSettingsForm form) {
		boardSettingsManager.setScribbleSettings(getPersonality(), new BoardSettings(form.isCleanMemory(), form.isCheckWords(), form.getTilesClass()));
		return ServiceResponse.success(null, "settings", form);
	}

	@Autowired
	public void setBoardSettingsManager(BoardSettingsManager boardSettingsManager) {
		this.boardSettingsManager = boardSettingsManager;
	}
}