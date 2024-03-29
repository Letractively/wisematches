package wisematches.playground.scribble.robot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wisematches.core.Personality;
import wisematches.core.RobotType;
import wisematches.playground.GameMove;
import wisematches.playground.GameMoveException;
import wisematches.playground.dictionary.Dictionary;
import wisematches.playground.dictionary.WordEntry;
import wisematches.playground.scribble.*;
import wisematches.playground.scribble.score.ScoreEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of robot brain for scribble.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public final class ScribbleRobotBrain {
	private static final Logger log = LoggerFactory.getLogger("wisematches.scribble.RobotBrain");

	public ScribbleRobotBrain() {
	}

	public void putInAction(ScribbleBoard board, RobotType type) {
		if (!board.isActive()) {
			return;
		}

		final long boardId = board.getBoardId();
		final long currentTime = System.currentTimeMillis();

		if (log.isDebugEnabled()) {
			log.debug("Start braining activity for board: " + boardId + " of type " + type +
					" at " + currentTime);
		}

		final Personality personality = board.getPlayerTurn();
		final ScribblePlayerHand hand = board.getPlayerHand(personality);

		final List<Word> words = searchAvailableMoves(board, hand.getTiles(), type);
		if (words == null) {
			log.info("Dictionary is not iterable. Turn passed.");
			try {
				board.passTurn(personality);
			} catch (GameMoveException e) {
				log.error("Turn can't be passed", e);
			}
			return;
		}
		log.trace("Found {} variants of words", words.size());

		final Word word = selectResultWord(words, type, board.getScoreEngine(), board);
		log.debug("Robot selected word: {}", word);

		try {
			if (word != null) {
				try {
					final GameMove move = board.makeTurn(personality, word);
					log.debug("Robot made a word and took {} points", move.getPoints());
				} catch (GameMoveException ex) {
					log.error("Move can't be done", ex);
					board.passTurn(personality);
				}
			} else {
				int bankRemained = Math.min(7, board.getBankRemained());
				if (bankRemained == 0) {
					log.debug("No available word. Turn passed.");
					board.passTurn(personality);
				} else {
					int[] tiles = selectTilesForExchange(hand, bankRemained);
					log.debug("No available word. Exchange tiles: {}", tiles);
					board.exchangeTiles(personality, tiles);
				}
			}
		} catch (GameMoveException ex) {
			log.error("Move can't be passed", ex);
		}

		if (log.isDebugEnabled()) {
			log.debug("Brain activity for board " + boardId + " is finished at " +
					System.currentTimeMillis() + ". Robot has been thought " +
					(System.currentTimeMillis() - currentTime) + "ms");
		}
	}

	public List<Word> getAvailableMoves(ScribbleBoard board, Tile[] tiles) {
		return searchAvailableMoves(board, tiles, RobotType.EXPERT); // expert knows about all moves
	}

	List<Word> searchAvailableMoves(ScribbleBoard board, Tile[] tiles, RobotType type) {
		log.trace("Hand robot tiles: {}", (Object) tiles);
		return analyzeValidWords(board.getDictionary(), board, tiles, type);
	}

	List<Word> analyzeValidWords(final Dictionary dictionary, final TilesPlacement placement, Tile[] tiles, final RobotType type) {
		final List<WorkTile> wordTiles = new ArrayList<>();
		for (int row = 0; row < ScribbleBoard.CELLS_NUMBER; row++) {
			for (int column = 0; column < ScribbleBoard.CELLS_NUMBER; column++) {
				final Tile boardTile = placement.getBoardTile(row, column);
				if (boardTile != null) {
					wordTiles.add(new WorkTile(boardTile, new Position(row, column)));
				}
			}
		}

		for (Tile tile : tiles) {
			wordTiles.add(new WorkTile(tile, null));
		}

		final List<Word> words = new ArrayList<>();
		boolean lastWasIncorrect = false;
		final AnalyzingTree analyzingTree = new AnalyzingTree(placement, wordTiles);
		for (WordEntry entry : dictionary.getWordEntries()) {
			final String word = entry.getWord();
			if (!isLegalWord(word, type)) {
				continue;
			}

			final String cw = analyzingTree.getCurrentWord();
			if (cw.length() != 0 && lastWasIncorrect) {
				if (word.startsWith(cw)) {
					continue;
				}
			}

			while (!word.startsWith(analyzingTree.getCurrentWord())) {
				analyzingTree.rollback();
				lastWasIncorrect = false;
			}

			final char[] chars = word.toCharArray();
			for (int i = analyzingTree.getCurrentLevel(); i < chars.length; i++) {
				char ch = chars[i];
				if (!analyzingTree.offerNextChar(ch)) {
					lastWasIncorrect = true;
					break;
				}
			}

			if (!lastWasIncorrect) {
				words.addAll(analyzingTree.getAcceptableWords());
			}
		}
		return words;
	}

	/**
	 * Checks that work is legal for specified robot type.
	 * <p/>
	 * Word is corrert in the following cases:
	 * <ol>
	 * <li>Robot is {@literal DULL} and length of work less or equals 6.
	 * <li>Robot is {@literal STAGER} and length of work less or equals 8.
	 * <li>Robot is {@literal EXPERT}.
	 * <ol>
	 * <p/>
	 * In any other cases word is incorrect and should be passed.
	 *
	 * @param word the word to be checked
	 * @param type the robot type
	 * @return {@code true} if word is legal; {@code false} - otherwise.
	 */
	boolean isLegalWord(String word, RobotType type) {
		switch (type) {
			case DULL:
				return (word.length() <= 6);
			case TRAINEE:
				return (word.length() <= 8);
			case EXPERT:
				return true;
		}
		return false;
	}

	Word selectResultWord(List<Word> words, RobotType robotType,
						  ScoreEngine scoreEngine, TilesPlacement tilesPlacement) {
		if (words.size() == 0) {
			return null;
		}
		Word word = null;
		switch (robotType) {
			case DULL:
				word = searchDullWord(words);
				break;
			case TRAINEE:
				word = searchFineWord(words);
				break;
			case EXPERT:
				word = searchExpertWord(words, scoreEngine, tilesPlacement);
				break;
			default:
		}
		if (word == null) {
			return null;
		}
		return word;
	}

	int[] selectTilesForExchange(ScribblePlayerHand hand, int max) {
		int n = 0;
		final int[] list = new int[max];
		for (Tile tile : hand.getTiles()) {
			if (!tile.isWildcard()) {
				list[n++] = tile.getNumber();
			}
		}
		return n != max ? Arrays.copyOf(list, n) : list;
	}

	private Word searchDullWord(List<Word> words) {
		int index = (int) Math.round(Math.random() * (double) (words.size() - 1));
		return words.get(index);
	}

	private Word searchFineWord(List<Word> words) {
		Word result = null;
		int maxLength = 0;

		for (Word word : words) {
			if (word.length() > maxLength) {
				result = word;
				maxLength = word.getTiles().length;
			}
		}
		return result;
	}

	private Word searchExpertWord(List<Word> words, ScoreEngine scoreEngine, TilesPlacement tilesPlacement) {
		Word result = null;

		int maxPoints = 0;
		for (Word word : words) {
			final ScribbleMoveScore calculation = scoreEngine.calculateWordScore(tilesPlacement, word);
			final int points = calculation.getPoints();
			if (points > maxPoints) {
				result = word;
				maxPoints = points;
			}
		}
		return result;
	}
}