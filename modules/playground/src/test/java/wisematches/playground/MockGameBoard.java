package wisematches.playground;

import wisematches.core.Personality;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@SuppressWarnings("unchecked")
class MockGameBoard extends AbstractGameBoard<GameSettings, GamePlayerHand> {
	private boolean allowNextMove = true;
	private boolean gameFinished = false;
	private boolean gamePassed = false;
	private boolean moveFinished;
	private short[] finishScore;
	private Date lastMoveTime = new Date();
	private MockGameMoveScore scoreCalculation = new MockGameMoveScore();

	MockGameBoard(GameSettings settings, Collection<Personality> players) {
		super(settings, players, null);
	}

	protected GamePlayerHand createPlayerHand(Personality player) {
		return new GamePlayerHand(player.getId());
	}

	protected boolean checkGameFinished() {
		return gameFinished;
	}

	protected void checkMove(PlayerMove move) throws IncorrectMoveException {
		if (!allowNextMove) {
			throw new IncorrectMoveException("Move not allowed");
		}
	}

	@Override
	protected GameMoveScore calculateMoveScores(PlayerMove move) {
		return scoreCalculation;
	}

	@Override
	protected void processMoveFinished(GamePlayerHand player, GameMove gameMove) {
		moveFinished = true;
	}

	protected short[] processGameFinished() {
		short[] a = finishScore;
		finishScore = null;
		return a;
	}

	protected boolean isGameStalemate() {
		return gamePassed || super.isGameStalemate();
	}

	public boolean isAllowNextMove() {
		return allowNextMove;
	}

	public void setAllowNextMove(boolean allowNextMove) {
		this.allowNextMove = allowNextMove;
	}

	public boolean isGameFinished() {
		return gameFinished;
	}

	public void setGameFinished(boolean gameFinished) {
		if (!gameFinished) {
//			PlayersIterator<GamePlayerHand> iterator;
			final Field field;
			try {
				field = getClass().getSuperclass().getDeclaredField("currentPlayerIndex");
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

	public boolean isGamePassed() {
		return gamePassed;
	}

	public void setGamePassed(boolean gamePassed) {
		this.gamePassed = gamePassed;
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

	@Override
	public GameRatingChange getRatingChange(GamePlayerHand player) {
		return null;
	}

	@Override
	public List<GameRatingChange> getRatingChanges() {
		return null;
	}
}
