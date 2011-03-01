package wisematches.server.gameplaying.scribble.robot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is integration test and demonstrates how two robots can play a game...
 * <p/>
 * This test uses real configuration and store game into database. It check that all steps works correct.
 * <p/>
 * TODO: move this test to integration test
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/config/database-config.xml",
		"classpath:/config/server-base-config.xml",
		"classpath:/config/game-scribble-config.xml",
		"classpath:/config/test-game-modules-config.xml"})
public class SmallRobotsGameTest {
	@Test
	public void commented() {
		throw new UnsupportedOperationException("Test has been commented");
	}
/*	@Autowired
	private RoomsManager roomsManager;

	@Autowired
	private RobotBrainManager robotBrainManager;

	private final Lock gameFinishedLock = new ReentrantLock();
	private final Condition gameFinishedCondition = gameFinishedLock.newCondition();

	private static final Log log = LogFactory.getLog("wisematches.scribble.robot.test");

	@Test
	public void test_makeSmallGame() throws BoardCreationException, InterruptedException {
		long currentTime = System.currentTimeMillis();

		assertNotNull("No room manager", roomsManager);
		assertNotNull("No robots brain manager", robotBrainManager);

		final RobotPlayer r1 = RobotPlayer.DULL;
		final RobotPlayer r2 = RobotPlayer.TRAINEE;
		final RobotPlayer r3 = RobotPlayer.EXPERT;

		final ScribbleBoardManager roomManager = (ScribbleBoardManager) roomsManager.getRoomManager(ScribbleBoardManager.ROOM);

		final ScribbleBoard board = roomManager.createBoard(
				new ScribbleSettings("This is robots game", "en", 3, false, true),
				Arrays.<Player>asList(r1, r2, r3));
		board.addGameBoardListener(new GameBoardListener() {
			public void gameFinished(GameBoard board, GamePlayerHand wonPlayer) {
				notifyGameFinished();
			}

			@Override
			public void playerMoved(GameMoveEvent event) {

			}

			public void gameDrew(GameBoard board) {
				notifyGameFinished();
			}

			public void gameInterrupted(GameBoard board, GamePlayerHand interrupterPlayer, boolean byTimeout) {
				notifyGameFinished();
			}
		});
		assertEquals("Game is not in progress state", GameState.ACTIVE, board.getGameState());

		gameFinishedLock.lock();
		if (board.getGameState() == GameState.ACTIVE) {
			gameFinishedCondition.await();
		}
		gameFinishedLock.unlock();

		log.info("Game was finished at " + (System.currentTimeMillis() - currentTime) + "ms");

		assertTrue("Board is not saved", board.getBoardId() > 0);
		assertEquals("Board is not finished", GameState.FINISHED, board.getGameState());
		assertTrue("Board has no one move", board.getGameMoves().size() > 0);
		assertNull("Board has a player who has a turn", board.getPlayerTurn());

		final int dullPoints = board.getPlayerHand(r1.getId()).getPoints();
		final int stagerPoints = board.getPlayerHand(r2.getId()).getPoints();
		final int expertPoints = board.getPlayerHand(r3.getId()).getPoints();

		if (log.isDebugEnabled()) {
			log.debug("Moves count: " + board.getGameMoves().size());
			log.debug("Players points: DULL - " + dullPoints +
					", TRAINEE - " + stagerPoints +
					", EXPERT - " + expertPoints);
			log.debug("Tiles in hands:");
			log.debug("     DULL - " + Arrays.toString(board.getPlayerHand(r1.getId()).getTiles()));
			log.debug("     TRAINEE - " + Arrays.toString(board.getPlayerHand(r2.getId()).getTiles()));
			log.debug("     EXPERT - " + Arrays.toString(board.getPlayerHand(r3.getId()).getTiles()));
		}

		assertTrue("Dull won a stager???", dullPoints < stagerPoints);
		assertTrue("Stager won a expert???", stagerPoints < expertPoints);
		assertEquals("EXPERT didn't win???", r3.getId(), board.getWonPlayer().getPlayerId());
	}

	private void notifyGameFinished() {
		gameFinishedLock.lock();
		gameFinishedCondition.signalAll();
		gameFinishedLock.unlock();
	}*/
}