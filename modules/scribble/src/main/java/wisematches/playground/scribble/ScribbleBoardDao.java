package wisematches.playground.scribble;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wisematches.personality.Personality;
import wisematches.playground.GameResolution;
import wisematches.playground.search.LastMoveInfo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class ScribbleBoardDao extends HibernateDaoSupport {
	public ScribbleBoardDao() {
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public ScribbleBoard getScribbleBoard(long boardId) {
		final HibernateTemplate template = getHibernateTemplate();
		ScribbleBoard scribbleBoard = template.get(ScribbleBoard.class, boardId);
		template.evict(scribbleBoard);
		return scribbleBoard;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void saveScribbleBoard(ScribbleBoard scribbleBoard) {
		final HibernateTemplate template = getHibernateTemplate();
		template.saveOrUpdate(scribbleBoard);
		template.flush();
		template.evict(scribbleBoard);
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Collection<Long> getActiveBoardsIds(Personality player) {
		final HibernateTemplate template = getHibernateTemplate();
		return (List<Long>) template.find("select board.boardId from wisematches.playground.scribble.ScribbleBoard " +
				" board join board.playerHands hand where board.gameResolution is NULL and hand.playerId = ?", player.getId());
	}


	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getActiveBoardsCount(Personality personality) {
		final HibernateTemplate template = getHibernateTemplate();
		return DataAccessUtils.intResult(template.find("select count(*) from wisematches.playground.scribble.ScribbleBoard " +
				" board join board.playerHands hand where board.gameResolution is NULL and hand.playerId = ?", personality.getId()));
	}


	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Collection<LastMoveInfo> findExpiringBoards() {
		final HibernateTemplate template = getHibernateTemplate();
		return template.find("select new " + LastMoveInfo.class.getName() + "(board.boardId, board.gameSettings.daysPerMove, board.lastMoveTime) from " +
				ScribbleBoard.class.getName() + " board where board.gameResolution is null");
	}
/*

	@SuppressWarnings("unchecked")
	public RatedBoardsInfo getRatedBoards(long playerId, Date startDate, Date endDate) {
		final HibernateTemplate template = getHibernateTemplate();

		final StringBuilder query = new StringBuilder();
		query.append("select board.boardId, board.finishedDate, hands.previousRating + hands.ratingDelta from ");
		query.append(ScribbleBoard.class.getName());
		query.append(" board join board.playersIterator.playerHands hands");
		query.append(" where (board.gameState = ? or board.gameState = ? or board.gameState = ?)");
		query.append(" and hands.playerId = ?");
		if (startDate != null) {
			query.append(" and board.finishedDate > ").append(startDate.getTime());
		}
		if (endDate != null) {
			query.append(" and board.finishedDate < ").append(endDate.getTime());
		}

		@SuppressWarnings("unchecked")
		final List<Object[]> list = template.find(query.toString(),
				new Object[]{
						GameState.FINISHED, GameState.DREW, GameState.INTERRUPTED, playerId
				});

		final long boardIds[] = new long[list.size()];
		final Date time[] = new Date[list.size()];
		final int rating[] = new int[list.size()];

		for (int i = 0; i < list.size(); i++) {
			final Object[] values = list.get(i);
			boardIds[i] = (Long) values[0];
			time[i] = (Date) values[1];
			rating[i] = (Integer) values[2];
		}
		return new RatedBoardsInfo(boardIds, time, rating);
	}
*/

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getGamesCount(EnumSet<GameResolution> states) {
		if (states != null && states.size() == 0) {
			return 0;
		}

		final HibernateTemplate template = getHibernateTemplate();

		final StringBuilder query = new StringBuilder();
		query.append("select count(*) from ");
		query.append(ScribbleBoard.class.getName());
		query.append(" board ");

		if (states == null) {
			query.append(" where board.gameResolution is null ");
			return ((Number) template.find(query.toString()).get(0)).intValue();
		} else {
			int index = 0;
			final Object[] values = new Object[states.size()];
			query.append(" where board.gameResolution in ( ");
			boolean first = true;
			for (GameResolution state : states) {
				if (!first) {
					query.append(", ");
				}
				query.append("?");
				first = false;
				values[index++] = state;
			}
			query.append(")");
			return DataAccessUtils.intResult(template.find(query.toString(), values));
		}
	}
}
