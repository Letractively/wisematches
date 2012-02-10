package wisematches.playground.tournament.r1;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class TournamentTicketBatch {
	private final long totalTickets;
	private final Map<TournamentSection, Long> sectionsMap = new HashMap<TournamentSection, Long>();

	public TournamentTicketBatch(Map<TournamentSection, Long> sectionsMap) {
		int k = 0;
		this.sectionsMap.putAll(sectionsMap);
		for (Long aLong : sectionsMap.values()) {
			k += aLong;
		}
		totalTickets = k;
	}

	public long getTotalTickets() {
		return totalTickets;
	}

	public long getBoughtTickets(TournamentSection section) {
		Long aLong = sectionsMap.get(section);
		if (aLong == null) {
			return 0;
		}
		return aLong;
	}
}