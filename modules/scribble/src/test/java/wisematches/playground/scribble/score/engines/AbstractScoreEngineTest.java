package wisematches.playground.scribble.score.engines;

import org.junit.Before;
import org.junit.Test;
import wisematches.playground.scribble.*;
import wisematches.playground.scribble.score.ScoreBonus;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class AbstractScoreEngineTest {
	private Tile[] tiles1;
	private Tile[] tiles2;
	private Tile[] tiles3;

	private TilesPlacement tp;
	private AbstractScoreEngine scoreEngine;

	@Before
	public void init() {
		scoreEngine = new AbstractScoreEngine(new ScoreBonus[]{
				new ScoreBonus(0, 0, ScoreBonus.Type.L2),
				new ScoreBonus(3, 0, ScoreBonus.Type.W2)
		}, 10);

		tiles1 = new Tile[]{
				new Tile(1, 'a', 1),
				new Tile(2, 'b', 2),
				new Tile(3, 'c', 3),
		};

		tiles2 = new Tile[]{
				new Tile(1, 'a', 1),
				new Tile(2, 'b', 2),
				new Tile(3, 'c', 3),
				new Tile(4, 'd', 4),
		};

		tiles3 = new Tile[]{
				new Tile(1, 'a', 1),
				new Tile(2, 'b', 2),
				new Tile(3, 'c', 3),
				new Tile(4, 'd', 0),
				new Tile(5, 'd', 0),
				new Tile(6, 'd', 0),
				new Tile(7, 'd', 0),
		};

		tp = createStrictMock(TilesPlacement.class);
	}

	@Test
	public void simpleCalculation() {
		expect(tp.isBoardTile(1)).andReturn(true);
		expect(tp.isBoardTile(2)).andReturn(false);
		expect(tp.isBoardTile(3)).andReturn(true);
		replay(tp);
		ScribbleMoveScore res = scoreEngine.calculateWordScore(tp, new Word(new Position(7, 7), Direction.VERTICAL, tiles1));
		assertEquals(6, res.getPoints());
		assertEquals(3, res.getBonuses().length);
		assertEquals("1 + 2 + 3", res.getFormula());
		verify(tp);
	}

	@Test
	public void allHandBonus() {
		expect(tp.isBoardTile(anyInt())).andReturn(false).times(7);
		replay(tp);
		ScribbleMoveScore res = scoreEngine.calculateWordScore(tp, new Word(new Position(7, 7), Direction.VERTICAL, tiles3));
		assertEquals(16, res.getPoints());
		assertEquals(7, res.getBonuses().length);
		assertEquals("(1 + 2 + 3 + 0 + 0 + 0 + 0) + 10", res.getFormula());
		verify(tp);
	}

	@Test
	public void oneBonusCell() {
		expect(tp.isBoardTile(1)).andReturn(false);
		expect(tp.isBoardTile(2)).andReturn(false);
		expect(tp.isBoardTile(3)).andReturn(true);
		replay(tp);
		ScribbleMoveScore res = scoreEngine.calculateWordScore(tp, new Word(new Position(0, 0), Direction.VERTICAL, tiles1));
		assertEquals(7, res.getPoints());
		assertEquals(3, res.getBonuses().length);
		assertSame(ScoreBonus.Type.L2, res.getBonuses()[0]);
		assertEquals("1*2 + 2 + 3", res.getFormula());
		verify(tp);
	}

	@Test
	public void twoBonusCells() {
		expect(tp.isBoardTile(1)).andReturn(false);
		expect(tp.isBoardTile(2)).andReturn(false);
		expect(tp.isBoardTile(3)).andReturn(true);
		expect(tp.isBoardTile(4)).andReturn(false);
		replay(tp);
		ScribbleMoveScore res = scoreEngine.calculateWordScore(tp, new Word(new Position(0, 0), Direction.VERTICAL, tiles2));
		assertEquals(22, res.getPoints());
		assertEquals(4, res.getBonuses().length);
		assertSame(ScoreBonus.Type.L2, res.getBonuses()[0]);
		assertSame(ScoreBonus.Type.W2, res.getBonuses()[3]);
		assertEquals("(1*2 + 2 + 3 + 4)*2", res.getFormula());
		verify(tp);
	}

	@Test
	public void twoBonusCellsIfOneBusy() {
		expect(tp.isBoardTile(1)).andReturn(false);
		expect(tp.isBoardTile(2)).andReturn(false);
		expect(tp.isBoardTile(3)).andReturn(true);
		expect(tp.isBoardTile(4)).andReturn(true);
		replay(tp);
		ScribbleMoveScore res = scoreEngine.calculateWordScore(tp, new Word(new Position(0, 0), Direction.VERTICAL, tiles2));
		assertEquals(11, res.getPoints());
		assertEquals(4, res.getBonuses().length);
		assertSame(ScoreBonus.Type.L2, res.getBonuses()[0]);
		assertNull(res.getBonuses()[1]);
		assertNull(res.getBonuses()[2]);
		assertNull(res.getBonuses()[3]);
		assertEquals("1*2 + 2 + 3 + 4", res.getFormula());
		verify(tp);
	}
}
