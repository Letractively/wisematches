package wisematches.server.web.controllers.gameplaying;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wisematches.server.gameplaying.board.BoardManagementException;
import wisematches.server.gameplaying.propose.GameProposal;
import wisematches.server.gameplaying.propose.GameProposalManager;
import wisematches.server.gameplaying.propose.ViolatedRestrictionException;
import wisematches.server.gameplaying.room.RoomManager;
import wisematches.server.gameplaying.scribble.board.ScribbleBoard;
import wisematches.server.gameplaying.scribble.board.ScribbleSettings;
import wisematches.server.personality.Personality;
import wisematches.server.personality.account.Language;
import wisematches.server.personality.player.Player;
import wisematches.server.personality.player.PlayerManager;
import wisematches.server.personality.player.computer.ComputerPlayer;
import wisematches.server.personality.player.computer.robot.RobotPlayer;
import wisematches.server.web.controllers.AbstractPlayerController;
import wisematches.server.web.controllers.gameplaying.form.CreateScribbleForm;
import wisematches.server.web.services.ads.AdvertisementManager;

import javax.validation.Valid;
import java.util.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/game")
public class DashboardController extends AbstractPlayerController {
	private PlayerManager playerManager;
	private AdvertisementManager advertisementManager;

	private RoomManager<ScribbleSettings, ScribbleBoard> scribbleRoomManager;

	private static final Log log = LogFactory.getLog("wisematches.server.web.dashboard");

	public DashboardController() {
	}

	@RequestMapping("create")
	public String createGamePage(@ModelAttribute("create") CreateScribbleForm form, Model model, Locale locale) {
		if (form.getBoardLanguage() == null) {
			form.setBoardLanguage(locale.getLanguage());
		}
		model.addAttribute("robotPlayers", RobotPlayer.getRobotPlayers());
		model.addAttribute("advertisementBlock", advertisementManager.getAdvertisementBlock("dashboard", Language.byLocale(locale)));
		return "/content/game/dashboard/create";
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public String createGameAction(@Valid @ModelAttribute("create") CreateScribbleForm form,
								   BindingResult result, Model model, Locale locale) throws BoardManagementException {
		if (log.isInfoEnabled()) {
			log.info("Create new game: " + form);
		}

		final Player player = getPlayer();

		if (form.getDaysPerMove() < 2) {
			result.rejectValue("daysPerMove", "game.create.time.err.min");
		} else if (form.getDaysPerMove() > 14) {
			result.rejectValue("daysPerMove", "game.create.time.err.max");
		}

		int opponents = 0;
		final List<Personality> players = new ArrayList<Personality>();
		if (checkOpponent("opponent1", form.getOpponent1(), true, players, result)) {
			opponents++;
		}
		if (checkOpponent("opponent2", form.getOpponent2(), false, players, result)) {
			opponents++;
		}
		if (checkOpponent("opponent3", form.getOpponent3(), false, players, result)) {
			opponents++;
		}

		if (opponents == 0) {
			result.rejectValue("opponent1", "game.create.opponents.err.nofirst");
		}

		if (!result.hasErrors()) {
			final ScribbleSettings s = new ScribbleSettings(form.getTitle(), form.getBoardLanguage(), form.getDaysPerMove());
			if (players.size() == opponents) {
				players.add(0, player); // also add current personality as a first one
				final ScribbleBoard board = scribbleRoomManager.getBoardManager().createBoard(s, players);
				return "redirect:/game/playboard.html?b=" + board.getBoardId();
			} else {
				final GameProposalManager<ScribbleSettings> proposalManager = scribbleRoomManager.getProposalManager();
				proposalManager.initiateGameProposal(s, opponents + 1, null, Arrays.asList(player));
				return "redirect:/game/dashboard.html";
			}
		}
		return createGamePage(form, model, locale);
	}

	@RequestMapping(value = "gameboard", params = "p")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String joinGameAction(@RequestParam("p") String id, @ModelAttribute("join") String join, Errors errors, Model model, Locale locale) throws BoardManagementException {
		if (log.isInfoEnabled()) {
			log.info("Join to the game: " + id);
		}
		try {
			final GameProposalManager<ScribbleSettings> proposalManager = scribbleRoomManager.getProposalManager();
			final GameProposal<ScribbleSettings> proposal = proposalManager.attachPlayer(Long.valueOf(id), getPlayer());
			if (proposal == null) {
				errors.reject("game.error.restriction.ready.description", null);
			} else if (proposal.isReady()) {
				final ScribbleBoard board = scribbleRoomManager.getBoardManager().createBoard(proposal.getGameSettings(), proposal.getPlayers());
				return "redirect:/game/playboard.html?b=" + board.getBoardId();
			}
		} catch (ViolatedRestrictionException e) {
			errors.reject("game.error.restriction." + e.getCode() + ".description", new Object[]{e.getActualValue(), e.getExpectedValue()}, null);
		}
		return showWaitingGames(join, model, locale);
	}

	@RequestMapping("gameboard")
	public String showWaitingGames(@ModelAttribute("join") String join, Model model, Locale locale) {
		final Player player = getPlayer();
		if (log.isDebugEnabled()) {
			log.debug("Loading waiting games for personality: " + player);
		}

		final List<GameProposal<ScribbleSettings>> proposals = new ArrayList<GameProposal<ScribbleSettings>>(scribbleRoomManager.getProposalManager().getActiveProposals());
		if (log.isDebugEnabled()) {
			log.debug("Found " + proposals.size() + " proposals for personality: " + player);
		}
		model.addAttribute("activeProposals", proposals);
		model.addAttribute("advertisementBlock", advertisementManager.getAdvertisementBlock("gameboard", Language.byLocale(locale)));
		return "/content/game/dashboard/join";
	}

	@RequestMapping("dashboard")
	public String showActiveGames(Model model, Locale locale) {
		final Personality personality = getPersonality();
		if (log.isDebugEnabled()) {
			log.debug("Loading active games for personality: " + personality);
		}
		final Collection<ScribbleBoard> activeBoards = scribbleRoomManager.getBoardManager().getActiveBoards(personality);
		if (log.isDebugEnabled()) {
			log.debug("Found " + activeBoards.size() + " active games for personality: " + personality);
		}
		final Collection<GameProposal<ScribbleSettings>> proposals = scribbleRoomManager.getProposalManager().getPlayerProposals(personality);
		if (log.isDebugEnabled()) {
			log.debug("Found " + proposals.size() + " proposals for personality: " + personality);
		}
		model.addAttribute("activeBoards", activeBoards);
		model.addAttribute("activeProposals", proposals);
		model.addAttribute("advertisementBlock", advertisementManager.getAdvertisementBlock("dashboard", Language.byLocale(locale)));
		return "/content/game/dashboard/view";
	}

	private boolean checkOpponent(String field, String opponent, boolean cpAllowed, List<Personality> players, BindingResult result) {
		final String id = opponent.trim();
		if (!"no".equalsIgnoreCase(id)) {
			if (!id.isEmpty()) {
				try {
					final Long playerId = Long.valueOf(id);
					final Player player1 = playerManager.getPlayer(playerId);
					if (player1 == null) {
						result.rejectValue(field, "game.create.opponents.err.unknown");
					} else if (!cpAllowed && player1 instanceof ComputerPlayer) {
						result.rejectValue(field, "game.create.opponents.err.manyrobots");
					} else {
						players.add(player1);
					}
				} catch (NumberFormatException ex) {
					result.rejectValue(field, "game.create.opponents.err.badid");
				}
			}
			return true;
		}
		return false;
	}

	@Autowired
	public void setPlayerManager(PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	@Autowired
	public void setAdvertisementManager(AdvertisementManager advertisementManager) {
		this.advertisementManager = advertisementManager;
	}

	@Autowired
	public void setScribbleRoomManager(RoomManager<ScribbleSettings, ScribbleBoard> scribbleRoomManager) {
		this.scribbleRoomManager = scribbleRoomManager;
	}
}
