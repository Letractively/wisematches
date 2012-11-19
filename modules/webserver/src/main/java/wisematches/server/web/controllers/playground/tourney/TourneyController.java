package wisematches.server.web.controllers.playground.tourney;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wisematches.personality.Language;
import wisematches.personality.Personality;
import wisematches.personality.player.Player;
import wisematches.playground.tourney.regular.*;
import wisematches.playground.tracking.PlayerStatisticManager;
import wisematches.server.web.controllers.ServiceResponse;
import wisematches.server.web.controllers.UnknownEntityException;
import wisematches.server.web.controllers.WisematchesController;
import wisematches.server.web.controllers.playground.tourney.form.EntityIdForm;
import wisematches.server.web.controllers.playground.tourney.form.SubscriptionForm;

import java.util.*;

/**
 * NOTE: this controller and view support only one subscription and limit functionality of
 * {@code TournamentSubscriptionManager}.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Controller
@RequestMapping("/playground/tourney")
public class TourneyController extends WisematchesController {
	private static final Log log = LogFactory.getLog("wisematches.server.web.tourney");
	private RegularTourneyManager tourneyManager;
	private PlayerStatisticManager statisticManager;

	public TourneyController() {
	}

	@RequestMapping("")
	public String tourneysRoot(Model model) {
		return showDashboard(model);
	}

	@RequestMapping("dashboard")
	public String showDashboard(Model model) {
		final Personality personality = getPersonality();


		final List<TourneyGroup> participated = tourneyManager.searchTourneyEntities(personality, new TourneyGroup.Context(personality, EnumSet.of(Tourney.State.ACTIVE)), null, null, null);
		model.addAttribute("participated", participated);

		setupAnnounce(model);

		return "/content/playground/tourney/dashboard";
	}

	@RequestMapping("active")
	public String showActive(Model model) {

		return "/content/playground/tourney/active";
	}

	@RequestMapping("finished")
	public String showFinished(Model model) {

		return "/content/playground/tourney/finished";
	}

	@RequestMapping("subscriptions")
	public String showSubscriptions(@RequestParam(value = "t", required = false) int t,
									@RequestParam(value = "l", required = false) String l,
									Model model) throws UnknownEntityException {
		final Tourney tourney = tourneyManager.getTourneyEntity(new Tourney.Id(t));
		if (tourney == null) {
			throw new UnknownEntityException(t, "tourney");
		}

		final Language language = Language.byCode(l);
		if (language == null) {
			throw new UnknownEntityException(l, "language");
		}
		final TourneySubscription.Context context = new TourneySubscription.Context(t, language);

		final List<TourneySubscription> tourneySubscriptions = tourneyManager.searchTourneyEntities(null, context, null, null, null);
		model.addAttribute("tourney", tourney);
		model.addAttribute("tourneyLanguage", language);
		model.addAttribute("tourneySubscriptions", tourneySubscriptions);

		setupAnnounce(model);

		return "/content/playground/tourney/subscriptions";
	}

	@RequestMapping("view")
	public String showEntityView(Model model, final @ModelAttribute EntityIdForm form) throws UnknownEntityException {
		if (form.isShit()) {
			throw new UnknownEntityException(form.getT(), "tourney");
		}

		final Tourney.Id tourneyId;
		try {
			tourneyId = new Tourney.Id(Integer.valueOf(form.getT()));
		} catch (NumberFormatException ex) {
			throw new UnknownEntityException(form.getT(), "tourney");
		}

		final Tourney tourney = tourneyManager.getTourneyEntity(tourneyId);
		if (tourney == null) {
			throw new UnknownEntityException(form.getT(), "tourney");
		}
		model.addAttribute("tourney", tourney); // add tourney object

		final Personality personality = getPersonality();
		if (form.isTourney()) {
			final TourneyRound.Context ctx = new TourneyRound.Context(tourneyId, null);
			final Map<TourneyDivision, List<TourneyRound>> divisionsTree = new HashMap<TourneyDivision, List<TourneyRound>>();
			final List<TourneyRound> rounds = tourneyManager.searchTourneyEntities(personality, ctx, null, null, null);

			for (TourneyRound round : rounds) {
				final TourneyDivision division = round.getDivision();

				List<TourneyRound> tourneyRounds = divisionsTree.get(division);
				if (tourneyRounds == null) {
					tourneyRounds = new ArrayList<TourneyRound>();
					divisionsTree.put(division, tourneyRounds);
				}
				tourneyRounds.add(round);
			}
			model.addAttribute("divisionsTree", divisionsTree);

			return "/content/playground/tourney/divisions";
		}
		return null;
	}

	@ResponseBody
	@RequestMapping("changeSubscription.ajax")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ServiceResponse changeSubscriptionAjax(@RequestParam("t") int tourneyNumber, @RequestBody SubscriptionForm form, Locale locale) {
		final Tourney tourney = tourneyManager.getTourneyEntity(new Tourney.Id(tourneyNumber));
		if (tourney == null) {
			return ServiceResponse.failure(gameMessageSource.getMessage("tourney.subscription.err.unknown", locale));
		}

		Language language = null;
		if (form.getLanguage() != null) {
			language = Language.byCode(form.getLanguage());
			if (language == null) {
				return ServiceResponse.failure(gameMessageSource.getMessage("tourney.subscription.err.language", locale));
			}
		}

		TourneySection section = null;
		try {
			final String sectionName = form.getSection();
			if (sectionName != null) {
				section = TourneySection.valueOf(sectionName.toUpperCase());
			}
		} catch (IllegalArgumentException ex) {
			return ServiceResponse.failure(gameMessageSource.getMessage("tourney.subscription.err.section", locale));
		}

		final Player principal = getPrincipal();
		try {
			final TourneySubscription subscription = tourneyManager.getSubscription(tourney, principal);
			if (subscription != null) {
				tourneyManager.unsubscribe(tourney, principal, subscription.getLanguage(), subscription.getSection());
			}
			if (section != null && language != null) { // subscribe
				tourneyManager.subscribe(tourney, principal, language, section);
			}
		} catch (TourneySubscriptionException ex) {
			log.error("Subscription can't be changed: " + form, ex);
			return ServiceResponse.failure(gameMessageSource.getMessage("tourney.subscription.err.internal", locale));
		}

		final TourneySubscriptions subscriptions = tourneyManager.getSubscriptions(tourney);
		final Map<String, Map<String, Integer>> res = new HashMap<String, Map<String, Integer>>();
		for (Language l : Language.values()) {
			Map<String, Integer> stringIntegerMap = new HashMap<String, Integer>();
			res.put(l.name(), stringIntegerMap);
			for (TourneySection s : TourneySection.values()) {
				stringIntegerMap.put(s.name(), subscriptions.getPlayers(l, s));
			}
		}
		return ServiceResponse.success("", "subscriptions", res);
	}

	private void setupAnnounce(Model model) {
		final Personality personality = getPersonality();

		final List<Tourney> announces = tourneyManager.searchTourneyEntities(personality, new Tourney.Context(EnumSet.of(Tourney.State.SCHEDULED)), null, null, null);
		Tourney announce = null;
		if (announces.size() > 1) {
			log.warn("More than one scheduled tourney. Shouldn't be possible: " + announces.size());
		} else if (announces.size() == 1) {
			announce = announces.get(0);
		}
		if (announce != null) {
			model.addAttribute("announce", announce);
			model.addAttribute("sections", TourneySection.values());
			model.addAttribute("languages", Language.values());
			model.addAttribute("statistics", statisticManager.getPlayerStatistic(personality));

			model.addAttribute("subscription", tourneyManager.getSubscription(announce, personality));
			model.addAttribute("subscriptions", tourneyManager.getSubscriptions(announce));
		}
	}

	@Autowired
	public void setTourneyManager(RegularTourneyManager tourneyManager) {
		this.tourneyManager = tourneyManager;
	}

	@Autowired
	public void setStatisticManager(PlayerStatisticManager statisticManager) {
		this.statisticManager = statisticManager;
	}

	@Override
	public String getHeaderTitle() {
		return "title.tourney";
	}
}
