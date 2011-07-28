package wisematches.playground.scribble.stats.statistician;

import org.junit.Before;
import org.junit.Test;
import wisematches.personality.Personality;
import wisematches.playground.GameMove;
import wisematches.playground.PlayerMove;
import wisematches.playground.scribble.*;
import wisematches.playground.scribble.tracking.ScribbleStatisticsEditor;
import wisematches.playground.scribble.tracking.ScribbleStatisticsTrapper;

import java.util.Arrays;
import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class ScribbleStatisticsTrapperTest {
	private ScribbleStatisticsTrapper movesStatistician;

	public ScribbleStatisticsTrapperTest() {
	}

	@Before
	public void setUp() {
		movesStatistician = new ScribbleStatisticsTrapper();
	}

	@Test
	public void testUpdateMovesStatistic() throws Exception {
		final long moveTime = System.currentTimeMillis();

		final ScribbleStatisticsEditor editor = new ScribbleStatisticsEditor(Personality.person(13L));

		final PlayerMove move1 = new MakeWordMove(13L, new Word(new Position(1, 2), Direction.HORIZONTAL, new Tile(1, 'A', 3), new Tile(1, 'B', 4)));
		final PlayerMove move2 = new MakeWordMove(13L, new Word(new Position(1, 2), Direction.HORIZONTAL, new Tile(1, 'A', 3), new Tile(1, 'B', 4), new Tile(1, 'C', 1), new Tile(1, 'D', 1)));
		final PlayerMove move3 = new ExchangeTilesMove(13, new int[0]);

		final ScribbleBoard gb = createMock(ScribbleBoard.class);
		expect(gb.getGameMoves())
				.andReturn(Arrays.asList(new GameMove(move1, 20, 1, new Date(moveTime - 1000)), null))
				.andReturn(Arrays.asList(new GameMove(move1, 10, 2, new Date(moveTime)), null))
				.andReturn(Arrays.asList(new GameMove(move1, 6, 3, new Date(moveTime + 3000)), null));
		replay(gb);

		movesStatistician.trapGameMoveDone(gb, new GameMove(move1, 20, 1, new Date(moveTime)), editor);
		assertEquals(20, editor.getAveragePoints());
		assertEquals(1000, editor.getAverageMoveTime());
		assertEquals(2, editor.getAverageWordLength());
		assertEquals(0, editor.getExchangesCount());
		assertEquals("AB", editor.getLastLongestWord().getText());
		assertEquals(moveTime, editor.getLastMoveTime().getTime());
		assertEquals("AB", editor.getLastValuableWord().getText());
		assertEquals(20, editor.getHighestPoints());
		assertEquals(20, editor.getLowestPoints());
		assertEquals(0, editor.getPassesCount());
		assertEquals(1, editor.getTurnsCount());
		assertEquals(1, editor.getWordsCount());

		movesStatistician.trapGameMoveDone(gb, new GameMove(move2, 10, 1, new Date(moveTime + 3000)), editor);
		assertEquals(15, editor.getAveragePoints());
		assertEquals((1000 + 3000) / 2, editor.getAverageMoveTime());
		assertEquals(3, editor.getAverageWordLength());
		assertEquals(0, editor.getExchangesCount());
		assertEquals("ABCD", editor.getLastLongestWord().getText());
		assertEquals(moveTime + 3000, editor.getLastMoveTime().getTime());
		assertEquals("AB", editor.getLastValuableWord().getText());
		assertEquals(20, editor.getHighestPoints());
		assertEquals(10, editor.getLowestPoints());
		assertEquals(0, editor.getPassesCount());
		assertEquals(2, editor.getTurnsCount());
		assertEquals(2, editor.getWordsCount());

		movesStatistician.trapGameMoveDone(gb, new GameMove(move3, 6, 1, new Date(moveTime + 8000)), editor);
		assertEquals(12, editor.getAveragePoints());
		assertEquals((1000 + 3000 + 5000) / 3, editor.getAverageMoveTime());
		assertEquals(3, editor.getAverageWordLength());
		assertEquals(1, editor.getExchangesCount());
		assertEquals("ABCD", editor.getLastLongestWord().getText());
		assertEquals(moveTime + 8000, editor.getLastMoveTime().getTime());
		assertEquals("AB", editor.getLastValuableWord().getText());
		assertEquals(20, editor.getHighestPoints());
		assertEquals(6, editor.getLowestPoints());
		assertEquals(0, editor.getPassesCount());
		assertEquals(3, editor.getTurnsCount());
		assertEquals(2, editor.getWordsCount());

		verify(gb);
	}
}
