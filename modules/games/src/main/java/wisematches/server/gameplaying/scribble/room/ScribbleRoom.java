package wisematches.server.gameplaying.scribble.room;

import wisematches.server.gameplaying.room.Room;
import wisematches.server.gameplaying.scribble.board.ScribbleBoard;
import wisematches.server.gameplaying.scribble.board.ScribbleSettings;
import wisematches.server.gameplaying.scribble.room.proposal.ScribbleProposal;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public final class ScribbleRoom implements Room<ScribbleProposal, ScribbleSettings, ScribbleBoard> {
	public static final ScribbleRoom name = new ScribbleRoom();

	private ScribbleRoom() {
	}
}