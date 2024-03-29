package wisematches.server.web.servlet.mvc.playground.tourney;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import wisematches.core.Language;
import wisematches.core.Personality;
import wisematches.core.Player;
import wisematches.core.search.Range;
import wisematches.playground.restriction.Restriction;
import wisematches.playground.restriction.RestrictionManager;
import wisematches.playground.tourney.TourneyEntity;
import wisematches.playground.tourney.regular.*;
import wisematches.playground.tracking.Statistics;
import wisematches.playground.tracking.StatisticsManager;
import wisematches.server.web.servlet.mvc.UnknownEntityException;
import wisematches.server.web.servlet.mvc.WisematchesController;
import wisematches.server.web.servlet.mvc.playground.tourney.form.EntityIdForm;
import wisematches.server.web.servlet.mvc.playground.tourney.form.SubscriptionForm;
import wisematches.server.web.servlet.sdo.ServiceResponse;

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
	private StatisticsManager statisticManager;
	private RegularTourneyManager tourneyManager;
	private RestrictionManager restrictionManager;

	private static final Logger log = LoggerFactory.getLogger("wisematches.web.mvc.TourneyController");

	private static final TourneyRound.Context ACTIVE_ROUND_CONTEXT = new TourneyRound.Context(EnumSet.of(TourneyEntity.State.ACTIVE));
	private static final TourneyGroup.Context ACTIVE_GROUP_CONTEXT = new TourneyGroup.Context(EnumSet.of(Tourney.State.ACTIVE));
	private static final TourneyDivision.Context ACTIVE_DIVISION_CONTEXT = new TourneyDivision.Context(EnumSet.of(TourneyEntity.State.ACTIVE));

	private static final Tourney.Context ACTIVE_TOURNEY_CONTEXT = new Tourney.Context(EnumSet.of(Tourney.State.ACTIVE));
	private static final Tourney.Context SCHEDULED_TOURNEY_CONTEXT = new Tourney.Context(EnumSet.of(Tourney.State.SCHEDULED));

	private static final TourneyDivision.Context FINISHED_DIVISION_CONTEXT = new TourneyDivision.Context(EnumSet.of(TourneyEntity.State.FINISHED));

	public TourneyController() {
	}

	@RequestMapping(value = {"", "dashboard"})
	public String showDashboardPage(Model model) {
		final Personality personality = getPrincipal();

		final List<TourneyGroup> participated = tourneyManager.searchTourneyEntities(personality, ACTIVE_GROUP_CONTEXT, null, null);
		model.addAttribute("groupsIterator", TourneyEntryIterator.groups(participated));

		setupAnnounce(model);

		return "/content/playground/tourney/dashboard";
	}

	@RequestMapping("active")
	public String showActiveTourneysPage(Model model) {
		List<TourneyDivision> divisions = tourneyManager.searchTourneyEntities(null, ACTIVE_DIVISION_CONTEXT, null, null);
		model.addAttribute("divisionsIterator", TourneyEntryIterator.divisions(divisions));

		setupAnnounce(model);

		return "/content/playground/tourney/active";
	}

	@RequestMapping("finished")
	public String showFinishedTourneysPage(Model model) {
		List<TourneyDivision> divisions = tourneyManager.searchTourneyEntities(null, FINISHED_DIVISION_CONTEXT, null, null);
		model.addAttribute("divisionsIterator", TourneyEntryIterator.divisions(divisions));

		setupAnnounce(model);

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
		final RegistrationRecord.Context context = new RegistrationRecord.Context(t, language, 1);

		final RegistrationSearchManager searchManager = tourneyManager.getRegistrationSearchManager();
		final List<RegistrationRecord> tourneySubscriptions = searchManager.searchEntities(null, context, null, null);
		model.addAttribute("tourney", tourney);
		model.addAttribute("tourneyLanguage", language);
		model.addAttribute("tourneySubscriptions", tourneySubscriptions);

		setupAnnounce(model);

		return "/content/playground/tourney/subscriptions";
	}

	@RequestMapping("view")
	public String showEntityView(final Model model,
								 final @ModelAttribute EntityIdForm form,
								 @RequestParam(value = "p", required = false, defaultValue = "0") int page) throws UnknownEntityException {
		if (form.isShit()) {
			throw new UnknownEntityException(form.getT(), "tourney");
		}

		final Tourney.Id tourneyId = form.getTourneyId();
		final Tourney tourney = tourneyManager.getTourneyEntity(tourneyId);
		if (tourney == null) {
			throw new UnknownEntityException(form.getT(), "tourney");
		}
		model.addAttribute("tourney", tourney);

		if (form.isTourney()) {
			return showTourneyView(tourney, model);
		} else if (form.isRound()) {
			return showRoundView(model, form, page);
		} else if (form.isGroup()) {
			return showGroupView(model, form);
		}
		throw new UnknownEntityException(form, "tourney");
	}

	private String showTourneyView(Tourney tourney, Model model) {
		final TourneyDivision.Context ctx = new TourneyDivision.Context(tourney.getId(), null);
		final List<TourneyDivision> divisions = tourneyManager.searchTourneyEntities(null, ctx, null, null);
		model.addAttribute("divisions", divisions);
		return "/content/playground/tourney/tourney";
	}

	private String showRoundView(Model model, EntityIdForm form, int page) throws UnknownEntityException {
		final TourneyRound.Id roundId = form.getRoundId();
		final TourneyGroup.Context ctx = new TourneyGroup.Context(roundId, null);

		final int totalCount = tourneyManager.getTotalCount(null, ctx);
		final List<TourneyGroup> groups = tourneyManager.searchTourneyEntities(null, ctx, null, Range.limit(page * 30, 30));

		model.addAttribute("round", tourneyManager.getTourneyEntity(roundId));
		model.addAttribute("groups", groups);

		model.addAttribute("currentPage", page);
		model.addAttribute("groupsCount", totalCount);

		return "/content/playground/tourney/round";
	}

	private String showGroupView(Model model, EntityIdForm form) throws UnknownEntityException {
		final TourneyGroup.Id groupId = form.getGroupId();
		final TourneyGroup group = tourneyManager.getTourneyEntity(groupId);
		if (group == null) {
			throw new UnknownEntityException(form, "tourney");
		}

		model.addAttribute("round", group.getRound());
		model.addAttribute("groups", new TourneyGroup[]{group});

		model.addAttribute("currentPage", 0);
		model.addAttribute("groupsCount", 1);

		return "/content/playground/tourney/round";
	}

	@RequestMapping("changeSubscription.ajax")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ServiceResponse changeSubscriptionAjax(@RequestParam("t") int tourneyNumber, @RequestBody SubscriptionForm form, Locale locale) {
		final Tourney tourney = tourneyManager.getTourneyEntity(new Tourney.Id(tourneyNumber));
		if (tourney == null) {
			return responseFactory.failure("tourney.subscription.err.unknown", locale);
		}

		Language language = null;
		if (form.getLanguage() != null) {
			language = Language.byCode(form.getLanguage());
			if (language == null) {
				return responseFactory.failure("tourney.subscription.err.language", locale);
			}
		}

		TourneySection section = null;
		try {
			final String sectionName = form.getSection();
			if (sectionName != null) {
				section = TourneySection.valueOf(sectionName.toUpperCase());
			}
		} catch (IllegalArgumentException ex) {
			return responseFactory.failure("tourney.subscription.err.section", locale);
		}

		final Player principal = getPrincipal();
		try {
			final boolean doRegistration = section != null && language != null;
			if (doRegistration) {
				final Restriction restriction = restrictionManager.validateRestriction(principal, "tourneys.count", getActiveTourneysCount(principal));
				if (restriction != null) {
					return responseFactory.failure("tourney.subscribe.forbidden", new Object[]{restriction.getThreshold()}, locale);
				}
			}

			final RegistrationRecord subscription = tourneyManager.getRegistration(principal, tourney);
			if (subscription != null) {
				tourneyManager.unregister(principal, tourney, subscription.getLanguage(), subscription.getSection());
			}

			if (doRegistration) { // register
				tourneyManager.register(principal, tourney, language, section);
			}
		} catch (RegistrationException ex) {
			log.error("Subscription can't be changed: {}", form, ex);
			return responseFactory.failure("tourney.subscription.err.internal", locale);
		}

		final RegistrationsSummary subscriptions = tourneyManager.getRegistrationsSummary(tourney);
		final Map<String, Map<String, Integer>> res = new HashMap<>();
		for (Language l : Language.values()) {
			final Map<String, Integer> stringIntegerMap = new HashMap<>();
			res.put(l.name(), stringIntegerMap);
			for (final TourneySection s : TourneySection.values()) {
				stringIntegerMap.put(s.name(), subscriptions.getPlayers(l, s));
			}
		}
		return responseFactory.success(res);
	}

	private void setupAnnounce(Model model) {
		final Player personality = getPrincipal();

		final List<Tourney> announces = tourneyManager.searchTourneyEntities(null, SCHEDULED_TOURNEY_CONTEXT, null, null);
		Tourney announce = null;
		if (announces.size() > 1) {
			log.warn("More than one scheduled tourney. Shouldn't be possible: {}", announces.size());
			announce = announces.get(0);
		} else if (announces.size() == 1) {
			announce = announces.get(0);
		}
		if (announce != null) {
			model.addAttribute("announce", announce);

			final Statistics statistic = statisticManager.getStatistic(personality);
			model.addAttribute("statistics", statistic);

			model.addAttribute("restriction",
					restrictionManager.validateRestriction(personality, "tourneys.count", getActiveTourneysCount(personality)));

			model.addAttribute("subscription", tourneyManager.getRegistration(personality, announce));
			model.addAttribute("subscriptions", tourneyManager.getRegistrationsSummary(announce));
		}
	}

	private int getActiveTourneysCount(Personality personality) {
		int totalCount = 0;
		totalCount += tourneyManager.getTotalCount(personality, ACTIVE_TOURNEY_CONTEXT);
		totalCount += tourneyManager.getRegistrationSearchManager().getTotalCount(personality, new RegistrationRecord.Context(1));
		return totalCount;
	}

	@Autowired
	public void setTourneyManager(RegularTourneyManager tourneyManager) {
		this.tourneyManager = tourneyManager;
	}

	@Autowired
	public void setStatisticManager(StatisticsManager statisticManager) {
		this.statisticManager = statisticManager;
	}

	@Autowired
	public void setRestrictionManager(RestrictionManager restrictionManager) {
		this.restrictionManager = restrictionManager;
	}
}
