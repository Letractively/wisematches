package wisematches.server.web.controllers.gameplaying;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import wisematches.server.gameplaying.room.BoardLoadingException;
import wisematches.server.gameplaying.room.RoomManager;
import wisematches.server.gameplaying.scribble.board.ScribbleBoard;
import wisematches.server.gameplaying.scribble.board.ScribbleSettings;

import java.util.Locale;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/game")
public class PlayboardController {
	private RoomManager<ScribbleBoard, ScribbleSettings> scribbleRoomManager;

	@RequestMapping("/playboard")
	public String showPlayboard(@RequestParam("gameId") long gameId, Model model, Locale locale) {
		try {
			model.addAttribute("board", scribbleRoomManager.openBoard(gameId));
		} catch (BoardLoadingException ex) {
			ex.printStackTrace();
		}
		model.addAttribute("pageName", "playboard");
		return "/content/game/layout";
	}

	@Autowired
	@Qualifier("scribbleRoomManager")
	public void setScribbleRoomManager(RoomManager<ScribbleBoard, ScribbleSettings> scribbleRoomManager) {
		this.scribbleRoomManager = scribbleRoomManager;
	}
}