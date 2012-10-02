package wisematches.playground.tourney;

import wisematches.database.Orders;
import wisematches.database.Range;
import wisematches.personality.Personality;
import wisematches.playground.search.SearchFilter;
import wisematches.playground.search.SearchManager;

import java.util.List;

/**
 * {@code TournamentManager} provides access to active tournaments/round and group.
 * <p/>
 * You can access announcement and rounds directly via the manager but groups are available only through
 * the {@code TournamentSearchManager} because there are a lot of groups and games in one announcement.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface TourneyEntityManager<E extends TourneyEntity>
		extends SearchManager<E, TourneyEntity.Context<? extends E, ?>, SearchFilter> {

	void addTourneyEntityListener(TourneyEntityListener<? super E> l);

	void removeTourneyEntityListener(TourneyEntityListener<? super E> l);


	<T extends E, K extends TourneyEntity.Id<? extends T, ?>> T getTournamentEntity(K id);

	<T extends E, C extends TourneyEntity.Context<? extends T, ?>> List<T> searchTournamentEntities(Personality person, C context, SearchFilter filter, Orders orders, Range range);
}
