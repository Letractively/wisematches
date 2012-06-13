package wisematches.playground.tournament;

import wisematches.personality.Language;

import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 * @deprecated migrate to Tournament.
 */
public interface Announcement {
    int getNumber();

    Date getScheduledDate();

    int getTotalTickets(Language language);

    int getBoughtTickets(Language language, TournamentSection category);
}
