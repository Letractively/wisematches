package wisematches.playground.robot.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import wisematches.personality.player.computer.robot.RobotPlayer;
import wisematches.personality.player.computer.robot.RobotType;
import wisematches.playground.*;
import wisematches.playground.robot.RobotBrain;
import wisematches.playground.robot.RobotBrainManager;
import wisematches.playground.room.Room;
import wisematches.playground.room.RoomManager;
import wisematches.playground.room.RoomsManager;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * This manager listen all games and when turn is transfered to robot it start process for perfome that move.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class RobotActivityCenter {
	private Executor movesExecutor;

	private RoomsManager roomsManager;
	private RobotBrainManager robotBrainManager;
	private TransactionTemplate transactionTemplate;

	private static final Log log = LogFactory.getLog("wisematches.server.robot.activity");

	public RobotActivityCenter() {
	}

	private void initializeGames() {
		final Collection<RoomManager> roomManagerCollection = roomsManager.getRoomManagers();

		final Collection<RobotPlayer> robotPlayers = robotBrainManager.getRobotPlayers();
		for (RoomManager roomManager : roomManagerCollection) {
			final Room roomType = roomManager.getRoomType();
			final BoardManager boardManager = roomManager.getBoardManager();
			boardManager.addBoardStateListener(new TheBoardStateListener(roomType));

			for (RobotPlayer player : robotPlayers) {
				@SuppressWarnings("unchecked")
				final Collection<GameBoard> activeBoards = boardManager.getActiveBoards(player);
				for (GameBoard activeBoard : activeBoards) {
					processRobotMove(roomType, activeBoard);
				}
			}
		}
	}

	private void afterPropertiesSet() {
		if (this.robotBrainManager != null && this.roomsManager != null && this.movesExecutor != null && this.transactionTemplate != null) {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					initializeGames();
				}
			});
		}
	}

	/**
	 * Checks that robot's has a turn on specified board and make a turn.
	 *
	 * @param room	  the room of specified game board.
	 * @param gameBoard the bame board to check and make a turn.
	 * @return {@code true} if move was maden; {@code false} - otherwise.
	 */
	private boolean processRobotMove(Room room, GameBoard gameBoard) {
		final GamePlayerHand hand = gameBoard.getPlayerTurn();
		if (hand != null) {
			final RobotPlayer robot = RobotPlayer.getComputerPlayer(hand.getPlayerId(), RobotPlayer.class);
			if (robot != null) {
				log.info("Initialize robot activity: " + room + ", " + robot);
				movesExecutor.execute(new MakeTurnTask(room, gameBoard));
				return true;
			}
		}
		return false;
	}


	public void setRoomsManager(RoomsManager roomsManager) {
		this.roomsManager = roomsManager;
		afterPropertiesSet();
	}

	public void setRobotBrainManager(RobotBrainManager robotBrainManager) {
		this.robotBrainManager = robotBrainManager;
		afterPropertiesSet();
	}

	public void setMovesExecutor(Executor movesExecutor) {
		this.movesExecutor = movesExecutor;
		afterPropertiesSet();
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		afterPropertiesSet();
	}


	final class MakeTurnTask implements Runnable {
		private final Room roomType;
		private final GameBoard gameBoard;

		MakeTurnTask(Room roomType, GameBoard gameBoard) {
			this.roomType = roomType;
			this.gameBoard = gameBoard;
		}

		public void run() {
			final GamePlayerHand hand = gameBoard.getPlayerTurn();
			if (hand != null) {
				final RobotPlayer robot = RobotPlayer.getComputerPlayer(hand.getPlayerId(), RobotPlayer.class);
				if (robot != null) {
					final RobotType robotType = robot.getRobotType();
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus status) {
							@SuppressWarnings("unchecked")
							final RobotBrain<GameBoard> brain = robotBrainManager.getRobotBrain(roomType, robotType);
							brain.putInAction(gameBoard, robotType);
						}
					});
				}
			}
		}
	}

	private final class TheBoardStateListener implements BoardStateListener {
		private Room room;

		private TheBoardStateListener(Room room) {
			this.room = room;
		}

		@Override
		public void gameStarted(GameBoard board) {
			processRobotMove(room, board);
		}

		@Override
		public void gameMoveDone(GameBoard board, GameMove move) {
			processRobotMove(room, board);
		}

		@Override
		public <S extends GameSettings, P extends GamePlayerHand> void gameFinished(GameBoard<S, P> board, GameResolution resolution, Collection<P> wonPlayers) {
		}
	}
}