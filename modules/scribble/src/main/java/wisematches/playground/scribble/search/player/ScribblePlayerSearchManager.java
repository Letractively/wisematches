package wisematches.playground.scribble.search.player;

import wisematches.playground.search.SearchCriteria;
import wisematches.playground.search.descriptive.AbstractDescriptiveSearchManager;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class ScribblePlayerSearchManager extends AbstractDescriptiveSearchManager<PlayerEntityBean, PlayerSearchArea> {
	public ScribblePlayerSearchManager() {
		super(PlayerEntityBean.class);
	}

	@Override
	protected String getEntitiesList(PlayerSearchArea area, SearchCriteria[] criteria) {
		String r = "wisematches.personality.account.impl.HibernateAccountImpl account, wisematches.playground.scribble.tracking.ScribbleStatisticsEditor stats";

		switch (area) {
			case PLAYERS:
				return r;
			case FRIENDS:
				return r + ", wisematches.playground.friends.impl.HibernateFriendRelation r";
			case FORMERLY:
				return r + ", wisematches.playground.scribble.ScribbleBoard board left join board.playerHands l left join board.playerHands r";
			default:
				throw new UnsupportedOperationException("Area criteria not implemented for " + area);
		}
	}

	@Override
	protected String getWhereCriterias(PlayerSearchArea area, SearchCriteria[] criteria) {
		String r = "account.id=stats.playerId and ";
		switch (area) {
			case PLAYERS:
				return r + "not account.id=:pid";
			case FRIENDS:
				return r + "r.friend=account.id and r.person=:pid";
			case FORMERLY:
				return r + "r.playerId=account.id and not r.playerId=l.playerId and l.playerId=:pid";
			default:
				throw new UnsupportedOperationException("Area criteria not implemented for " + area);
		}
	}

	@Override
	protected String getGroupCriterias(PlayerSearchArea context, SearchCriteria[] criteria) {
		return null;
	}
}
