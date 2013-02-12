package wisematches.server.web.servlet.mvc.playground.scribble.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import wisematches.core.Personality;
import wisematches.core.PersonalityManager;
import wisematches.core.Player;
import wisematches.playground.propose.GameProposalManager;
import wisematches.playground.propose.ProposalRelation;
import wisematches.playground.scribble.ScribbleContext;
import wisematches.playground.scribble.ScribblePlayManager;
import wisematches.playground.scribble.ScribbleSearchManager;
import wisematches.playground.scribble.ScribbleSettings;
import wisematches.playground.tracking.StatisticManager;
import wisematches.server.services.state.PlayerStateManager;
import wisematches.server.web.i18n.GameMessageSource;
import wisematches.server.web.servlet.mvc.WisematchesController;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/playground/scribble")
public class AbstractGameController extends WisematchesController {
	protected GameMessageSource messageSource;
	protected PersonalityManager personalityManager;
	protected ScribblePlayManager playManager;
	protected ScribbleSearchManager searchManager;
	protected PlayerStateManager playerStateManager;
	protected StatisticManager statisticManager;
	protected GameProposalManager<ScribbleSettings> proposalManager;

	protected static final ScribbleContext ACTIVE_GAMES_CTX = new ScribbleContext(true);
	protected static final ScribbleContext FINISHED_GAMES_CTX = new ScribbleContext(true);

	public AbstractGameController() {
	}

	protected int getActiveGamesCount(Personality principal) {
		int activeGames;
		if (principal instanceof Player) {
			activeGames = statisticManager.getStatistic((Player) principal).getActiveGames();
		} else {
			activeGames = searchManager.getTotalCount(principal, ACTIVE_GAMES_CTX);
		}
		return activeGames + proposalManager.getTotalCount(principal, ProposalRelation.INVOLVED);
	}

	protected int getFinishedGamesCount(Personality principal) {
		if (principal instanceof Player) {
			return statisticManager.getStatistic((Player) principal).getFinishedGames();
		} else {
			return searchManager.getTotalCount(principal, FINISHED_GAMES_CTX);
		}
	}

	@Autowired
	public void setPersonalityManager(PersonalityManager personalityManager) {
		this.personalityManager = personalityManager;
	}

	@Autowired
	public void setMessageSource(GameMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Autowired
	public void setPlayManager(ScribblePlayManager playManager) {
		this.playManager = playManager;
	}

	@Autowired
	public void setSearchManager(ScribbleSearchManager searchManager) {
		this.searchManager = searchManager;
	}

	@Autowired
	public void setPlayerStateManager(PlayerStateManager playerStateManager) {
		this.playerStateManager = playerStateManager;
	}

	@Autowired
	public void setStatisticManager(StatisticManager statisticManager) {
		this.statisticManager = statisticManager;
	}

	@Autowired
	public void setProposalManager(GameProposalManager<ScribbleSettings> proposalManager) {
		this.proposalManager = proposalManager;
	}
}