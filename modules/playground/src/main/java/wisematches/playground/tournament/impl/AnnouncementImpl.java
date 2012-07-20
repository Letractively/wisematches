package wisematches.playground.tournament.impl;

import wisematches.personality.Language;
import wisematches.playground.tournament.Announcement;
import wisematches.playground.tournament.TournamentSection;

import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class AnnouncementImpl implements Announcement {
	private int tournament;
	private Date scheduledDate;

	private final int[][] values = new int[Language.values().length][TournamentSection.values().length];

	public AnnouncementImpl(Announcement announcement) {
		this.tournament = announcement.getTournament();
		this.scheduledDate = announcement.getScheduledDate();

		for (Language language : Language.values()) {
			for (TournamentSection section : TournamentSection.values()) {
				setBoughtTickets(language, section, announcement.getBoughtTickets(language, section));
			}
		}
	}


	@Override
	public int getTournament() {
		return tournament;
	}

	@Override
	public Date getScheduledDate() {
		return scheduledDate;
	}

	@Override
	public int getTotalTickets(Language language) {
		int res = 0;
		for (int value : values[language.ordinal()]) {
			res += value;
		}
		return res;
	}

	@Override
	public int getBoughtTickets(Language language, TournamentSection section) {
		return values[language.ordinal()][section.ordinal()];
	}

	void setBoughtTickets(Language language, TournamentSection section, int count) {
		values[language.ordinal()][section.ordinal()] = count;
	}

	void changeBoughtTickets(Language language, TournamentSection section, int delta) {
		values[language.ordinal()][section.ordinal()] += delta;
	}
}