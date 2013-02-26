package wisematches.server.web.servlet.mvc.playground.scribble.board;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import wisematches.core.Player;
import wisematches.playground.BoardLoadingException;
import wisematches.playground.dictionary.WordAttribute;
import wisematches.playground.scribble.*;
import wisematches.playground.scribble.bank.LetterDescription;
import wisematches.playground.scribble.bank.LettersDistribution;
import wisematches.playground.scribble.settings.BoardSettings;
import wisematches.playground.scribble.settings.BoardSettingsManager;
import wisematches.server.web.servlet.mvc.DeprecatedResponse;
import wisematches.server.web.servlet.mvc.UnknownEntityException;
import wisematches.server.web.servlet.mvc.WisematchesController;
import wisematches.server.web.servlet.mvc.playground.scribble.game.form.ScribbleTileForm;
import wisematches.server.web.servlet.mvc.playground.scribble.game.form.ScribbleWordForm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/playground/scribble/board")
@Deprecated
public class ScribbleBoardController extends WisematchesController {
	private ScribblePlayManager boardManager;
	private BoardSettingsManager boardSettingsManager;

	private static final Log log = LogFactory.getLog("wisematches.server.web.playboard");
	public static final BoardSettings BOARD_SETTINGS = new BoardSettings(false, false, true, true, true, "tiles-set-classic");

	public ScribbleBoardController() {
	}

	@RequestMapping("")
	public String showPlayboard(@RequestParam("b") long gameId,
								@RequestParam(value = "t", required = false) String tiles,
								Model model) throws UnknownEntityException {
		try {
			final Player player = getPlayer();
			final ScribbleBoard board = boardManager.openBoard(gameId);
			if (board == null) { // unknown board
				throw new UnknownEntityException(gameId, "board");
			}

			model.addAttribute("board", board);

			// Issue 206: Share tiles
			if (tiles != null && !tiles.isEmpty()) {
				final char[] ts = tiles.toCharArray();
				if (ts.length > 0 && ts.length < 8) {
					boolean valid = true;
					final Tile[] t = new Tile[ts.length];
					final LettersDistribution lettersDistribution = board.getDistribution();
					for (int i = 0; i < t.length && valid; i++) {
						final LetterDescription description = lettersDistribution.getLetterDescription(Character.toLowerCase(ts[i]));
						if (description != null) {
							t[i] = new Tile(0, description.getLetter(), description.getCost());
						} else {
							valid = false;
						}
					}

					if (valid) {
						model.addAttribute("tiles", t);
					}
				}
			}

			model.addAttribute("wordAttributes", WordAttribute.values());
			model.addAttribute("dictionaryLanguage", board.getSettings().getLanguage());

			if (player == null) {
				model.addAttribute("viewMode", Boolean.TRUE);
				model.addAttribute("boardSettings", BOARD_SETTINGS);
			} else {
				model.addAttribute("boardSettings", boardSettingsManager.getScribbleSettings(player));
				model.addAttribute("viewMode", !board.isActive() || board.getPlayerHand(player) == null);
			}
			return "/content/playground/scribble/playboard";
		} catch (BoardLoadingException ex) {
			log.error("Board " + gameId + " can't be loaded", ex);
			throw new UnknownEntityException(gameId, "board");
		}
	}

	@ResponseBody
	@RequestMapping("make")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DeprecatedResponse makeTurnAjax(@RequestParam("b") final long gameId,
										   @RequestBody final ScribbleWordForm word, final Locale locale) {
		if (log.isDebugEnabled()) {
			log.debug("Process player's move: " + gameId + ", word: " + word);
		}
		return ScribbleObjectsConverter.processSafeAction(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				final Player player = getPlayer();

				final ScribbleBoard board = boardManager.openBoard(gameId);
				final MakeTurn gameMove = board.makeTurn(player, word.createWord());
				return ScribbleObjectsConverter.convertGameMove(player, board, gameMove, messageSource, locale);
			}
		}, messageSource, locale);
	}

	@ResponseBody
	@RequestMapping("pass")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DeprecatedResponse passTurnAjax(@RequestParam("b") final long gameId, final Locale locale) {
		if (log.isDebugEnabled()) {
			log.debug("Process player's pass: " + gameId);
		}
		return ScribbleObjectsConverter.processSafeAction(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				final Player player = getPlayer();
				final ScribbleBoard board = boardManager.openBoard(gameId);
				final PassTurn gameMove = board.passTurn(player);
				return ScribbleObjectsConverter.convertGameMove(player, board, gameMove, messageSource, locale);
			}
		}, messageSource, locale);
	}

	@ResponseBody
	@RequestMapping("exchange")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DeprecatedResponse exchangeTilesAjax(@RequestParam("b") final long gameId,
												@RequestBody final ScribbleTileForm[] tiles, final Locale locale) {
		if (log.isDebugEnabled()) {
			log.debug("Process player's exchange: " + gameId + ", tiles: " + Arrays.toString(tiles));
		}
		return ScribbleObjectsConverter.processSafeAction(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				int[] t = new int[tiles.length];
				for (int i = 0; i < tiles.length; i++) {
					t[i] = tiles[i].getNumber();
				}

				final Player player = getPlayer();
				final ScribbleBoard board = boardManager.openBoard(gameId);
				final ExchangeMove gameMove = board.exchangeTiles(player, t);
				return ScribbleObjectsConverter.convertGameMove(player, board, gameMove, messageSource, locale);
			}
		}, messageSource, locale);
	}

	@ResponseBody
	@RequestMapping("resign")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DeprecatedResponse resignGameAjax(@RequestParam("b") final long gameId, final Locale locale) {
		if (log.isDebugEnabled()) {
			log.debug("Process player's resign: " + gameId);
		}
		return ScribbleObjectsConverter.processSafeAction(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				final ScribbleBoard board = boardManager.openBoard(gameId);
				board.resign(getPlayer());

				final Map<String, Object> res = new HashMap<>();
				res.put("state", ScribbleObjectsConverter.convertGameState(board, messageSource, locale));
				if (!board.isActive()) {
					res.put("players", board.getPlayers());
				}
				return res;
			}
		}, messageSource, locale);
	}
/*

	private Map<String, Object> processGameMove(final long gameId, final PlayerMove move, final Locale locale) throws Exception {
		final Personality currentPlayer = getMember();
		if (move.getPlayerId() != currentPlayer.getId()) {
			throw new UnsuitablePlayerException("make turn", currentPlayer);
		}

		final ScribbleBoard board = boardManager.openBoard(gameId);
		final GameMove gameMove = board.makeMove(move);
		return ScribbleObjectsConverter.convertGameMove(locale, currentPlayer, board, gameMove);
	}
*/

	@Autowired
	public void setBoardManager(ScribblePlayManager scribbleBoardManager) {
		this.boardManager = scribbleBoardManager;
	}

	@Autowired
	public void setBoardSettingsManager(BoardSettingsManager boardSettingsManager) {
		this.boardSettingsManager = boardSettingsManager;
	}
}
