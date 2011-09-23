package wisematches.playground;

import wisematches.personality.Personality;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains information about game - game settings, all moves and so on.
 * <p/>
 * {@code AbstractGameBoard} is database entity but has {@code @MappedSuperclass} annotation and subclasses
 * shoud specify {@code Entity} annotation.
 * <p/>
 * This implementation persists the following fields: boardId, players, passesCount,
 * lastMoveTile, gamesState and gameSettings. This implementation does not persist moves and any listeners.
 * Each subclass must do it by itself.
 *
 * @param <S> the type of game settings.
 * @param <P> the type of player hands.
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@MappedSuperclass
public abstract class AbstractGameBoard<S extends GameSettings, P extends GamePlayerHand> implements GameBoard<S, P> {
	/**
	 * The board id. This is primary key for each board.
	 * <p/>
	 * Field is accessible by field in Hibernate.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "boardId")
	private long boardId;

	/**
	 * Collections of all done moves.
	 * <p/>
	 * This is transient field for Hibernate.
	 */
	@Transient
	protected final List<GameMove> moves = new ArrayList<GameMove>(0);

	@Column(name = "startedDate")
	private Date startedDate;

	@Column(name = "finishedDate")
	private Date finishedDate;

	@OrderColumn(name = "playerIndex")
	@ElementCollection(fetch = FetchType.EAGER)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	@CollectionTable(name = "scribble_player", joinColumns = @JoinColumn(name = "boardId"))
	private List<P> playerHands;

	@Column(name = "playersCount")
	private byte playersCount;

	@Column(name = "movesCount")
	private int movesCount = 0;

	@Column(name = "currentPlayerIndex")
	private byte currentPlayerIndex = -1;

	/**
	 * Count of passed turns. If passed turns count grate three game is draw.
	 */
	@Column(name = "passesCount")
	private int passesCount;

	/**
	 * Time when last move was done.
	 */
	@Column(name = "lastMoveTime")
	private Date lastMoveTime;

	/**
	 * The state of this game.
	 */
	@Column(name = "resolution")
	private GameResolution gameResolution = null;

	@Column(name = "rated")
	private boolean rated = true;

	/**
	 * Settings of this game
	 */
	@Embedded
	private S gameSettings;

	@Transient
	protected final Lock lock = new ReentrantLock();

	@Transient
	private final Collection<GameBoardListener> boardListeners = new CopyOnWriteArrayList<GameBoardListener>();

	private static final int MAX_PASSED_TURNS = 2;
	private static final Random FIRST_PLAYER_RANDOM = new Random();

	/**
	 * This is Hibernate constructor that is required for serialization. Visibility of this constructor MUST BE
	 * changed to package visible in superclass.
	 */
	@Deprecated
	protected AbstractGameBoard() {
	}

	/**
	 * Creates new board with specified settings. Game created with this constructor is rated.
	 *
	 * @param gameSettings the game settings.
	 * @param players	  the collection of all players.
	 * @throws NullPointerException if setting is {@code null}
	 */
	@SuppressWarnings("unchecked")
	protected AbstractGameBoard(S gameSettings, Collection<? extends Personality> players) {
		if (gameSettings == null) {
			throw new IllegalArgumentException("Settings can't be null");
		}
		if (players == null) {
			throw new IllegalArgumentException("Players can't be null");
		}
		if (players.size() < 2) {
			throw new IllegalArgumentException("Game can't have less than 2 players");
		}

		this.gameSettings = gameSettings;
		this.rated = gameSettings.isRatedGame();

		startedDate = lastMoveTime = new Date();

		final List<P> hands = new ArrayList<P>(players.size());
		for (Personality player : players) {
			if (player == null) {
				throw new IllegalArgumentException("Players list can't contain null players");
			}
			hands.add(createPlayerHand(player));
		}
		playerHands = hands;
		playersCount = (byte) hands.size();
		currentPlayerIndex = selectFirstPlayer(gameSettings, hands);
	}

	@Override
	public long getBoardId() {
		return boardId;
	}

	@Override
	public Date getStartedTime() {
		return startedDate;
	}

	@Override
	public Date getLastMoveTime() {
		return lastMoveTime;
	}

	@Override
	public Date getFinishedTime() {
		return finishedDate;
	}

	public void addGameBoardListener(GameBoardListener listener) {
		if (!boardListeners.contains(listener)) {
			boardListeners.add(listener);
		}
	}

	public void removeGameBoardListener(GameBoardListener listener) {
		boardListeners.remove(listener);
	}

	protected void firePlayerMoved(GameMove move) {
		for (GameBoardListener boardListener : boardListeners) {
			boardListener.gameMoveDone(this, move);
		}
	}

	protected void fireGameFinished() {
		for (GameBoardListener boardListener : boardListeners) {
			boardListener.gameFinished(this, gameResolution, getWonPlayers());
		}
	}
	/* ========== End Listeners and Fires ================ */

	@Override
	public GameMove makeMove(PlayerMove move) throws GameMoveException {
		lock.lock();
		try {
			checkState();

			final P player = getPlayerHand(move.getPlayerId());
			if (player == null) {
				throw new UnsuitablePlayerException("make turn", 0);
			}

			if (player != getPlayerTurn()) {
				throw new UnsuitablePlayerException("make turn", getPlayerTurn().getPlayerId(), player.getPlayerId());
			}

			checkMove(move);

			final short points = calculateMovePoints(move);
			player.increasePoints(points);
			lastMoveTime = new Date();


			if (move instanceof PassTurnMove) {
				passesCount++;
			} else {
				passesCount = 0;
			}

			final GameMove gameMove = new GameMove(move, points, moves.size(), lastMoveTime);
			moves.add(gameMove);
			movesCount = moves.size();

			processMoveFinished(player, gameMove);

			boolean finished = checkGameFinished();
			boolean passed = isGameStalemate();
			if (finished || passed) {
				finalizeGame(finished ? GameResolution.FINISHED : GameResolution.STALEMATE);
				firePlayerMoved(gameMove);
				fireGameFinished();
			} else {
				currentPlayerIndex = getNextPlayerIndex();
				firePlayerMoved(gameMove);
			}
			return gameMove;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<P> getWonPlayers() {
		lock.lock();
		try {
			if (isGameActive()) {
				return null;
			}
			final List<P> won = new ArrayList<P>(playerHands.size());

			int points = Integer.MIN_VALUE;
			for (P player : playerHands) {
				if (player.getPoints() == points) {
					won.add(player);
				} else if (player.getPoints() > points) {
					won.clear();
					won.add(player);
					points = player.getPoints();
				}
			}
			if (won.size() == playerHands.size()) {
				return Collections.emptyList();
			}
			return won;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public P getPlayerTurn() {
		lock.lock();
		try {
			return getPlayerByCode(currentPlayerIndex);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public P getNextPlayerTurn() {
		lock.lock();
		try {
			return getPlayerByCode(getNextPlayerIndex());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<GameMove> getGameMoves() {
		lock.lock();
		try {
			return Collections.unmodifiableList(moves);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int getGameMovesCount() {
		lock.lock();
		try {
			return movesCount;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public P getPlayerHand(long playerId) {
		lock.lock();
		try {
			for (P playerHand : playerHands) {
				if (playerHand.getPlayerId() == playerId) {
					return playerHand;
				}
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<P> getPlayersHands() {
		lock.lock();
		try {
			return Collections.unmodifiableList(playerHands);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public S getGameSettings() {
		return gameSettings;
	}

	@Override
	public boolean isRatedGame() {
		return rated;
	}

	@Override
	public boolean isGameActive() {
		lock.lock();
		try {
			return gameResolution == null && !isGameExpired();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GameResolution getGameResolution() {
		lock.lock();
		try {
			return gameResolution;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void terminate() throws GameMoveException {
		lock.lock();
		try {
			try {
				checkState();
			} catch (GameExpiredException ex) { //terminate if expired
				final P p = getPlayerTurn();
				if (p != null) {
					closeImpl(p, true);
				}
			} catch (GameFinishedException ex) {
				; // nothing to do
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void resign(P player) throws GameMoveException {
		lock.lock();
		try {
			closeImpl(player, false);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns number of passed turns.
	 *
	 * @return the number of passed turns.
	 */
	public int getPassesCount() {
		lock.lock();
		try {
			return passesCount;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks that game is expired.
	 *
	 * @return the game is expired.
	 */
	protected boolean isGameExpired() {
		return System.currentTimeMillis() - getLastMoveTime().getTime() > gameSettings.getDaysPerMove() * 86400000;
	}

	/**
	 * Checks that game was passed.
	 *
	 * @return <code>true</code> if game was passed; <code>otherwise</code>
	 */
	protected boolean isGameStalemate() {
		return passesCount / playerHands.size() >= MAX_PASSED_TURNS;
	}

	/**
	 * Checks that board is ready for game and player can make a move.
	 *
	 * @throws GameStateException if game state is not <code>ACTIVE</code>.
	 */
	protected void checkState() throws GameStateException {
		if (gameResolution != null) {
			throw new GameFinishedException(gameResolution);
		}

		if (isGameExpired()) {
			throw new GameExpiredException();
		}
	}

	/**
	 * Selects first player for the game.
	 * <p/>
	 * Implementation of this method selects random player from all players in specified list.
	 *
	 * @param gameSettings the game settings.
	 * @param players	  the list of all players to select first.
	 * @return the player who should be first.
	 */
	protected byte selectFirstPlayer(S gameSettings, List<P> players) {
		return (byte) FIRST_PLAYER_RANDOM.nextInt(players.size());
	}

	protected final P getPlayerByCode(byte code) {
		if (code == -1) {
			return null;
		}
		return playerHands.get(code);
	}

	protected final byte getPlayerCode(P player) {
		return (byte) playerHands.indexOf(player);
	}


	private byte getNextPlayerIndex() {
		if (currentPlayerIndex == -1) {
			return -1;
		} else if (currentPlayerIndex + 1 == playerHands.size()) {
			return 0;
		} else {
			return (byte) (currentPlayerIndex + 1);
		}
	}

	private void finalizeGame(GameResolution resolution) {
		finishedDate = new Date();
		gameResolution = resolution;

		if (resolution == GameResolution.STALEMATE) {
			currentPlayerIndex = -1;
		}

		final short[] ints = processGameFinished();
		for (int i = 0; i < ints.length; i++) {
			playerHands.get(i).increasePoints(ints[i]);
		}
	}

	private void closeImpl(P player, boolean byTimeout) throws GameMoveException {
		if (gameResolution != null) {
			return;
		}
		final long playerId = player.getPlayerId();
		final P hand = getPlayerHand(playerId);
		if (hand != player) {
			throw new UnsuitablePlayerException("Player does not belong to this game and can't resign it", playerId);
		}

		hand.increasePoints((short) -hand.getPoints()); // Clear player points...

		if (rated && moves.size() + passesCount < playerHands.size() * 2) {
			rated = false;
		}

		finalizeGame(byTimeout ? GameResolution.TIMEOUT : GameResolution.RESIGNED);

		//According to requirements if game was interrupted when terminator should be set as a current player.
		currentPlayerIndex = getPlayerCode(hand);
		fireGameFinished();
	}

	/*================== Checks game state =================*/

	/**
	 * Creates a player hand for specified player.
	 *
	 * @param player the player for who hand must be crated.
	 * @return the player's hand.
	 */
	protected abstract P createPlayerHand(Personality player);

	/**
	 * Checks specified move. If move isn't correct or can't be maden appropriate exception will be thrown.
	 *
	 * @param move the move for checking.
	 * @throws IncorrectMoveException if move is incorrect.
	 */
	protected abstract void checkMove(PlayerMove move) throws IncorrectMoveException;

	/**
	 * Calculate and returns move points.
	 *
	 * @param move the move to be calculated.
	 * @return the move points.
	 */
	protected abstract short calculateMovePoints(PlayerMove move);

	/**
	 * Indicates that move was accepted. When this method is called move already added to game moves list.
	 *
	 * @param player   the player who made move
	 * @param gameMove the game move
	 */
	protected abstract void processMoveFinished(P player, GameMove gameMove);

	/**
	 * Checks that no one valid move is allowed.
	 *
	 * @return <code>true</code> if no any allowed moves; <code>false</code> - otherwise.
	 */
	protected abstract boolean checkGameFinished();

	/**
	 * Indicates that game was finished with specified status.
	 *
	 * @return the points for players in order of <code>#getPlayerHands</code>.
	 * @see #getPlayersHands()
	 */
	protected abstract short[] processGameFinished();
}