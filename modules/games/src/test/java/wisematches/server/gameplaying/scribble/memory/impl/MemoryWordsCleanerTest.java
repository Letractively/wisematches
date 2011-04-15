package wisematches.server.gameplaying.scribble.memory.impl;

import org.easymock.Capture;
import org.junit.Test;
import wisematches.server.gameplaying.board.GameResolution;
import wisematches.server.gameplaying.room.board.BoardManager;
import wisematches.server.gameplaying.room.board.BoardStateListener;
import wisematches.server.gameplaying.scribble.board.ScribbleBoard;
import wisematches.server.gameplaying.scribble.memory.MemoryWordManager;
import wisematches.server.gameplaying.scribble.room.ScribbleRoomManager;

import static org.easymock.EasyMock.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class MemoryWordsCleanerTest {
	public MemoryWordsCleanerTest() {
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test() {
		final ScribbleBoard board = createMock(ScribbleBoard.class);

		final MemoryWordManager wordManager = createMock(MemoryWordManager.class);
		wordManager.clearMemoryWords(board);
		replay(wordManager);

		final Capture<BoardStateListener> capture = new Capture<BoardStateListener>();

		final BoardManager manager = createMock(BoardManager.class);
		manager.addBoardStateListener(capture(capture));
		replay(manager);

		final ScribbleRoomManager roomManager = new ScribbleRoomManager();
		roomManager.setBoardManager(manager);

		MemoryWordsCleaner cleaner = new MemoryWordsCleaner();
		cleaner.setMemoryWordManager(wordManager);
		cleaner.setScribbleRoomManager(roomManager);

		capture.getValue().gameFinished(board, GameResolution.FINISHED, null);

		verify(wordManager);
	}
}