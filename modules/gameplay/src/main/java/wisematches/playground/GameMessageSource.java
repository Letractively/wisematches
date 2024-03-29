package wisematches.playground;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DelegatingMessageSource;
import wisematches.core.*;
import wisematches.playground.propose.Criterion;
import wisematches.playground.propose.CriterionViolation;
import wisematches.playground.tourney.regular.TourneyRelationship;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class GameMessageSource extends DelegatingMessageSource implements MessageSource {
	private static final Map<Locale, DateFormat> DATE_FORMATTER = new ConcurrentHashMap<>();
	private static final Map<Locale, DateFormat> TIME_FORMATTER = new ConcurrentHashMap<>();

	static {
		for (Language lang : Language.values()) {
			DATE_FORMATTER.put(lang.getLocale(), DateFormat.getDateInstance(DateFormat.LONG, lang.getLocale()));
			TIME_FORMATTER.put(lang.getLocale(), DateFormat.getTimeInstance(DateFormat.SHORT, lang.getLocale()));
		}
	}

	public GameMessageSource() {
	}

	public String getMessage(String code, Locale locale) {
		return super.getMessage(code, null, locale);
	}

	public String getMessage(String code, Object a1, Locale locale) {
		return super.getMessage(code, new Object[]{a1}, locale);
	}

	public String getMessage(String code, Object a1, Object a2, Locale locale) {
		return super.getMessage(code, new Object[]{a1, a2}, locale);
	}

	public String getMessage(String code, Object a1, Object a2, Object a3, Locale locale) {
		return super.getMessage(code, new Object[]{a1, a2, a3}, locale);
	}


	public String getBoardTitle(BoardDescription<?, ?> board, Locale locale) {
		return getBoardTitle(board.getSettings().getTitle(), board.getRelationship(), locale);
	}

	public String getBoardTitle(String title, GameRelationship relationship, Locale locale) {
		if (relationship != null) {
			if (relationship.getCode() == TourneyRelationship.CODE) {
				return relationship.getId() +
						getNumeralEnding((int) relationship.getId(), locale) + " " +
						getMessage("tourney.label", locale);
			}
		}
		return title;
	}


	public String getRobotNick(Robot player, Locale locale) {
		return getRobotNick(player.getRobotType(), locale);
	}

	public String getRobotNick(RobotType type, Locale locale) {
		return getMessage("game.player." + type.name().toLowerCase(), locale);
	}

	public String getPlayerNick(Member player, Locale locale) {
		return player.getNickname();
	}

	public String getVisitorNick(Visitor player, Locale locale) {
		return getVisitorNick(player.getLanguage(), locale);
	}

	public String getVisitorNick(Language language, Locale locale) {
		return getMessage("game.player.guest", locale);
	}

	public String getPersonalityNick(Personality p, Locale locale) {
		if (p instanceof Robot) {
			return getRobotNick((Robot) p, locale);
		} else if (p instanceof Visitor) {
			return getVisitorNick((Visitor) p, locale);
		} else if (p instanceof Member) {
			return getPlayerNick((Member) p, locale);
		}
		throw new IllegalArgumentException("Unsupported personality type: " + p.getClass());
	}


	public String formatDate(Date date, Locale locale) {
		return DATE_FORMATTER.get(locale).format(date);
	}

	public String formatTime(Date date, Locale locale) {
		return TIME_FORMATTER.get(locale).format(date);
	}

	public String formatSpentTime(BoardDescription<?, ?> board, Locale locale) {
		return formatTimeMinutes(getSpentMinutes(board), locale);
	}

	public String formatElapsedTime(Date date, Locale locale) {
		final long startTime = date.getTime();
		final long endTime = System.currentTimeMillis();
		return formatTimeMinutes((endTime - startTime) / 1000 / 60, locale);
	}

	public String formatRemainedTime(Date date, Locale locale) {
		final long startTime = System.currentTimeMillis();
		final long endTime = date.getTime();
		return formatTimeMinutes((endTime - startTime) / 1000 / 60, locale);
	}

	public String formatRemainedTime(BoardDescription<?, ?> board, Locale locale) {
		return formatTimeMinutes(getRemainedMinutes(board), locale);
	}

	public String formatCriterion(Criterion criterion, Locale locale, boolean isShort) {
		return getMessage("game.join.err." + criterion.getCode() + (isShort ? ".label" : ".description"), criterion.getExpected(), null, locale);
	}

	public String formatViolation(CriterionViolation violation, Locale locale, boolean isShort) {
		return getMessage("game.join.err." + violation.getCode() + (isShort ? ".label" : ".description"), violation.getExpected(), violation.getReceived(), locale);
	}

	public int getAge(Date date) {
		DateMidnight birthdate = new DateMidnight(date);
		DateTime now = new DateTime();
		Years age = Years.yearsBetween(birthdate, now);
		return age.getYears();
	}

	public long getTimeMillis(Date date) {
		return date.getTime();
	}

	public long getSpentMinutes(BoardDescription<?, ?> board) {
		final long startTime = board.getStartedDate().getTime();
		final long endTime;
		final Date finishedTime = board.getFinishedDate();
		if (finishedTime != null) {
			endTime = finishedTime.getTime();
		} else {
			endTime = System.currentTimeMillis();
		}
		return (endTime - startTime) / 1000 / 60;
	}

	public long getRemainedMinutes(BoardDescription<?, ?> board) {
		final int daysPerMove = board.getSettings().getDaysPerMove();
		final long elapsedTime = System.currentTimeMillis() - board.getLastMoveTime().getTime();

		final long minutesPerMove = daysPerMove * 24 * 60;
		final long minutesElapsed = (elapsedTime / 1000 / 60);
		return minutesPerMove - minutesElapsed;
	}

	public String formatTimeMillis(long time, Locale locale) {
		return formatTimeMinutes(time / 60 / 1000, locale);
	}

	public String formatTimeMinutes(long time, Locale locale) {
		final int days = (int) (time / 60 / 24);
		final int hours = (int) ((time - (days * 24 * 60)) / 60);
		final int minutes = (int) (time % 60);

		final Language language = Language.byLocale(locale);
		final Localization localization = language.getLocalization();
		if (days == 0 && hours == 0 && minutes == 0) {
			return localization.getMomentAgoLabel();
		}

		if (hours <= 0 && minutes <= 0) {
			return days + " " + localization.getDaysLabel(days);
		}
		if (days <= 0 && minutes <= 0) {
			return hours + " " + localization.getHoursLabel(hours);
		}
		if (days <= 0 && hours <= 0) {
			return minutes + " " + localization.getMinutesLabel(minutes);
		}

		final StringBuilder b = new StringBuilder();
		if (days > 0) {
			b.append(days).append(localization.getDaysCode()).append(" ");
		}
		if (hours > 0) {
			b.append(hours).append(localization.getHoursCode()).append(" ");
		}
		if ((days == 0 || hours == 0) && (minutes > 0)) {
			b.append(minutes).append(localization.getMinutesCode()).append(" ");
		}
		return b.toString().trim();
	}

	public String formatDays(int days, Locale locale) {
		return Language.byLocale(locale).getLocalization().getDaysLabel(days);
	}

	public String formatHours(int hours, Locale locale) {
		return Language.byLocale(locale).getLocalization().getHoursLabel(hours);
	}

	public String formatMinutes(int minutes, Locale locale) {
		return Language.byLocale(locale).getLocalization().getMinutesLabel(minutes);
	}

	public String getWordEnding(int quantity, Locale locale) {
		return Language.byLocale(locale).getLocalization().getWordEnding(quantity);
	}

	public String getNumeralEnding(int value, Locale locale) {
		return Language.byLocale(locale).getLocalization().getNumeralEnding(value);
	}
//
//
//	public void setMessageSource(MessageSource messageSource) {
//		this.messageSource = messageSource;
//	}
}
