package wisematches.playground;

import wisematches.core.Personality;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@SuppressWarnings("unchecked")
class MockGameBoard extends AbstractGameBoard<GameSettings, AbstractPlayerHand, GameMove> {
	private boolean allowNextMove = true;
	private boolean gameFinished = false;
	private boolean moveFinished;
	private short[] finishScore;
	private Date lastMoveTime = new Date();
	private MockGameMoveScore scoreCalculation = new MockGameMoveScore();

	MockGameBoard(GameSettings settings, Collection<Personality> players) {
		super(settings, players, null);
	}

	@Override
	protected AbstractPlayerHand createPlayerHand(Personality player) {
		return new AbstractPlayerHand(player);
	}

	protected boolean checkGameFinished() {
		return gameFinished;
	}

	@Override
	protected short[] processGameFinished() {
		short[] a = finishScore;
		finishScore = null;
		return a;
	}

	@Override
	protected void processMoveDone(GameMove move) {
	}

	public boolean isAllowNextMove() {
		return allowNextMove;
	}

	public void setAllowNextMove(boolean allowNextMove) {
		this.allowNextMove = allowNextMove;
	}

	@Override
	public boolean isGameFinished() {
		return gameFinished;
	}

	public void setGameFinished(boolean gameFinished) {
		if (!gameFinished) {
//			PlayersIterator<GamePlayerHand> iterator;
			final Field field;
			try {
				field = AbstractBoardDescription.class.getDeclaredField("currentPlayerIndex");
				field.setAccessible(true);
				field.setByte(this, (byte) 0);
//				iterator = (PlayersIterator<GamePlayerHand>) field.get(this);
//				iterator.setPlayerTurn(iterator.getPlayers().get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.gameFinished = gameFinished;
	}

	public int getPoints() {
		return scoreCalculation.getPoints();
	}

	public void setPoints(short points) {
		scoreCalculation.setPoints(points);
	}

	public boolean isMoveFinished() {
		return moveFinished;
	}

	public void setMoveFinished(boolean moveFinished) {
		this.moveFinished = moveFinished;
	}

	public short[] getFinishScore() {
		return finishScore;
	}

	public void setFinishScore(short[] finishScore) {
		this.finishScore = finishScore;
	}

	public void setLastMoveTime(Date date) {
		lastMoveTime = date;
	}

	@Override
	public Date getLastMoveTime() {
		return lastMoveTime;
	}

	GameMove makeMove(Personality player) throws GameMoveException {
		return finalizePlayerMove(new MockMove(player), scoreCalculation);
	}
}
