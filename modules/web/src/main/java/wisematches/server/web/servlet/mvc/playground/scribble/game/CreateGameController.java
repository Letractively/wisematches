package wisematches.server.web.servlet.mvc.playground.scribble.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wisematches.core.Personality;
import wisematches.core.RobotType;
import wisematches.playground.BoardLoadingException;
import wisematches.playground.dictionary.DictionaryManager;
import wisematches.playground.restriction.RestrictionManager;
import wisematches.playground.scribble.ScribbleBoard;
import wisematches.server.services.relations.PlayerSearchArea;
import wisematches.server.services.relations.ScribblePlayerSearchManager;
import wisematches.server.web.servlet.mvc.ServiceResponse;
import wisematches.server.web.servlet.mvc.playground.scribble.game.form.CreateScribbleForm;
import wisematches.server.web.servlet.mvc.playground.scribble.game.form.CreateScribbleTab;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/playground/scribble")
public class CreateGameController extends AbstractGameController {
	private DictionaryManager dictionaryManager;
	private RestrictionManager restrictionManager;
	private ScribblePlayerSearchManager playerSearchManager;

	private static final String[] SEARCH_COLUMNS = new String[]{"nickname", "ratingG", "ratingA", "language", "averageMoveTime", "lastMoveTime"};
	private static final List<PlayerSearchArea> SEARCH_AREAS = Arrays.asList(PlayerSearchArea.values());

	private static final Log log = LogFactory.getLog("wisematches.server.web.game.create");

	public CreateGameController() {
	}

	@RequestMapping("create")
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String createGamePage(@RequestParam(value = "t", required = false) String type,
								 @RequestParam(value = "p", required = false) String parameter,
								 @Valid @ModelAttribute("create") CreateScribbleForm form,
								 Model model, Locale locale) {
		if (form.getBoardLanguage() == null) {
			form.setBoardLanguage(locale.getLanguage());
		}

		if (!form.isRotten()) {
			if ("robot".equalsIgnoreCase(type)) {
				initRobotForm(form, parameter);
			} else if ("wait".equalsIgnoreCase(type)) {
				initWaitOpponentForm(form, parameter);
			} else if ("challenge".equalsIgnoreCase(type)) {
				initChallengeForm(form, parameter, locale);
			} else if ("board".equalsIgnoreCase(type)) {
				initBoardCloneForm(form, parameter, locale);
			} else {
				initDefaultForm(form);
			}
		}

		final Personality personality = getPlayer();
		model.addAttribute("robots", playManager.getSupportedRobots());
		model.addAttribute("restriction", restrictionManager.validateRestriction(personality, "games.active", getActiveGamesCount(personality)));
		model.addAttribute("maxOpponents", restrictionManager.getRestrictionThreshold("scribble.opponents", personality));

		model.addAttribute("searchArea", PlayerSearchArea.FRIENDS);
		model.addAttribute("searchAreas", SEARCH_AREAS);
		model.addAttribute("searchColumns", SEARCH_COLUMNS);
		model.addAttribute("searchEntityDescriptor", playerSearchManager.getEntityDescriptor());

		if (personality.getType().isVisitor()) {
			form.setCreateTab(CreateScribbleTab.ROBOT);
			model.addAttribute("playRobotsOnly", true);
		} else {
			model.addAttribute("playRobotsOnly", false);
		}
		return "/content/playground/scribble/create";
	}

	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@RequestMapping(value = "create.ajax", method = RequestMethod.POST)
	public ServiceResponse createGameAction(@RequestBody CreateScribbleForm form, Locale locale) {
/*		if (log.isInfoEnabled()) {
			log.info("Create new game: " + form);
		}

		final Personality principal = getMember();
		if (form.getTitle().length() > 150) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.title.err.max", locale));
		}
		if (form.getDaysPerMove() < 2) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.time.err.min", locale));
		} else if (form.getDaysPerMove() > 14) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.time.err.max", locale));
		}

		if (form.getCreateTab() == null) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponent.err.blank", locale));
		}

		if (form.getChallengeMessage().length() > 254) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponent.challenge.err", locale));
		}

		final Language language = Language.byCode(form.getBoardLanguage());
		if (language == null) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.language.err.blank", locale));
		}

		final Dictionary dictionary = dictionaryManager.getDictionary(language);
		if (dictionary == null) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.dictionary.err.unknown", locale));
		}

		long[] opponents;
		final ScribbleSettings s = new ScribbleSettings(form.getTitle(), language, form.getDaysPerMove());
		if (form.getCreateTab() == CreateScribbleTab.ROBOT) {
			opponents = new long[]{RobotPlayer.valueOf(form.getRobotType()).getId()};
		} else if (form.getCreateTab() == CreateScribbleTab.WAIT) {
			opponents = new long[form.getOpponentsCount()];
		} else if (form.getCreateTab() == CreateScribbleTab.CHALLENGE) {
			opponents = form.getOpponents();
		} else {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponent.err.incorrect", locale));
		}

		boolean robot = false;
		final List<Personality> players = new ArrayList<>();
		if (opponents == null || opponents.length == 0) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.min", locale));
		} else if (opponents.length > 3) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.max", locale));
		} else {
			for (long opponent : opponents) {
				final ProprietaryPlayer computerPlayer = RobotPlayer.getComputerPlayer(opponent);
				if (computerPlayer instanceof RobotPlayer) {
					robot = true;
					if (opponents.length > 1) {
						return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.robot", locale));
					} else {
						players.add(computerPlayer);
					}
				} else if (ProprietaryPlayer.isComputerPlayer(opponent)) {
					return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.unknown", locale, opponent));
				} else if (opponent != 0) {
					Personality p = personalityManager.getMember(opponent);
					if (p == null) {
						return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.unknown", locale, opponent));
					} else {
						players.add(p);
					}
				}
			}
		}

		final Set<Personality> check = new HashSet<>(players);
		check.add(principal);
		if (check.size() < players.size() + 1) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.duplicate", locale));
		}

		final int activeGamesCount = getActiveGamesCount(principal);
		if (restrictionManager.validateRestriction(principal, "games.active", activeGamesCount) != null) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.forbidden", locale, activeGamesCount));
		}

		final Restriction restriction = restrictionManager.validateRestriction(principal, "scribble.opponents", opponents.length - 1);
		if (restriction != null) {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.count", locale, restriction.getThreshold()));
		}

		ScribbleBoard board = null;
		if (players.size() == opponents.length) { // challenge or robot
			if (robot) { // robot
				players.add(principal);
				try {
					board = playManager.createBoard(s, players);
				} catch (BoardCreationException ex) {
					log.error("New board can't be created: " + s + ", " + players, ex);
					return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.unknown", locale));
				}
			} else { // challenge
				proposalManager.initiate(s, principal, players, form.getChallengeMessage());
			}
		} else if (players.size() == 0) { // waiting
			final List<PlayerCriterion> res = new ArrayList<>();
			if (form.getMinRating() > 0) {
				res.add(PlayerRestrictions.rating("player.rating.min", form.getMinRating(), ComparableOperator.GE));
			}
			if (form.getMaxRating() > 0) {
				res.add(PlayerRestrictions.rating("player.rating.max", form.getMaxRating(), ComparableOperator.LE));
			}
			if (form.getTimeouts() > 0) {
				res.add(PlayerRestrictions.timeouts("game.timeouts", form.getTimeouts(), ComparableOperator.LE));
			}
			if (form.getCompleted() > 0) {
				res.add(PlayerRestrictions.completed("game.completed", form.getCompleted(), ComparableOperator.GE));
			}
			proposalManager.initiate(s, principal, opponents.length, res.toArray(new PlayerCriterion[res.size()]));
		} else {
			return ServiceResponse.failure(messageSource.getMessage("game.create.opponents.err.mixed", locale));
		}

		if (board != null) {
			return ServiceResponse.success(null, "board", board.getBoardId());
		}
		return ServiceResponse.success();*/
		return ServiceResponse.failure();
	}

	private void initRobotForm(CreateScribbleForm form, String parameter) {
		form.setCreateTab(CreateScribbleTab.ROBOT);

		RobotType type = RobotType.TRAINEE;
		if (parameter != null) {
			try {
				type = RobotType.valueOf(parameter.toUpperCase());
			} catch (IllegalArgumentException ignore) {
			}
		}
		form.setRobotType(type);
	}

	private void initWaitOpponentForm(CreateScribbleForm form, String parameter) {
		form.setCreateTab(CreateScribbleTab.WAIT);
		form.setOpponentsCount(1);
		if (parameter != null) {
			try {
				form.setOpponentsCount(Integer.valueOf(parameter));
			} catch (NumberFormatException ignore) {
			}
		}
	}

	private void initChallengeForm(CreateScribbleForm form, String parameter, Locale locale) {
		form.setTitle(messageSource.getMessage("game.challenge.player.label", getPlayer().getNickname(), locale));
		form.setChallengeMessage("");
		form.setCreateTab(CreateScribbleTab.CHALLENGE);
		final List<Long> ids = new ArrayList<>();
		if (parameter != null) {
			final String[] split = parameter.split("\\|");
			for (String id : split) {
				try {
					Personality player = personalityManager.getMember(Long.valueOf(id));
					if (player != null) {
						ids.add(player.getId());
					}
				} catch (NumberFormatException ignore) {
				}
			}

			if (ids.size() != 0) {
				final long[] res = new long[ids.size()];
				for (int i = 0, idsSize = ids.size(); i < idsSize; i++) {
					res[i] = ids.get(i);
				}
				form.setOpponents(res);
			}
		}
	}

	private void initBoardCloneForm(CreateScribbleForm form, String parameter, Locale locale) {
		try {
			final ScribbleBoard board = playManager.openBoard(Long.valueOf(parameter));
			if (board != null) {
				form.setTitle(messageSource.getMessage("game.challenge.replay.label", board.getBoardId(), locale));
				form.setChallengeMessage(messageSource.getMessage("game.challenge.replay.description", messageSource.getPersonalityNick(getPlayer(), locale), locale));
				form.setDaysPerMove(board.getSettings().getDaysPerMove());
				form.setBoardLanguage(board.getSettings().getLanguage().getCode());

				int index = 0;
				final List<Personality> playersHands = board.getPlayers();
				final long[] players = new long[playersHands.size() - 1];
				for (Personality playersHand : playersHands) {
					if (playersHand.equals(getPlayer())) {
						continue;
					}
					players[index++] = playersHand.getId();
				}
				form.setOpponents(players);
				form.setCreateTab(CreateScribbleTab.CHALLENGE);
			}
		} catch (BoardLoadingException ignore) { // do nothing
		}
	}

	private void initDefaultForm(CreateScribbleForm form) {
		form.setCreateTab(CreateScribbleTab.ROBOT);
		form.setOpponentsCount(1);
	}

	@Autowired
	public void setDictionaryManager(DictionaryManager dictionaryManager) {
		this.dictionaryManager = dictionaryManager;
	}

	@Autowired
	public void setRestrictionManager(RestrictionManager restrictionManager) {
		this.restrictionManager = restrictionManager;
	}

	@Autowired
	public void setPlayerSearchManager(ScribblePlayerSearchManager playerSearchManager) {
		this.playerSearchManager = playerSearchManager;
	}
}
