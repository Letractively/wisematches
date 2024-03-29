package wisematches.playground.scribble.expiration;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import wisematches.core.Personality;
import wisematches.core.expiration.impl.AbstractExpirationManager;
import wisematches.playground.*;
import wisematches.playground.scribble.ScribbleBoard;
import wisematches.playground.scribble.ScribblePlayManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class ScribbleExpirationManager extends AbstractExpirationManager<Long, ScribbleExpirationType> implements InitializingBean {
	private SessionFactory sessionFactory;
	private ScribblePlayManager boardManager;

	private final TheBoardListener stateListener = new TheBoardListener();

	private static final int MILLIS_IN_DAY = 86400000;//24 * 60 * 60 * 1000;
	private static final Logger log = LoggerFactory.getLogger("wisematches.scribble.ExpirationManager");

	public ScribbleExpirationManager() {
		super(ScribbleExpirationType.class, log);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		lock.lock();
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					final Session session = sessionFactory.getCurrentSession();
					final Criteria criteria = session.createCriteria(ScribbleBoard.class)
							.add(Restrictions.isNull("resolution"))
							.setProjection(Projections.projectionList()
									.add(Projections.property("boardId"))
									.add(Projections.property("settings.daysPerMove").as("daysPerMove"))
									.add(Projections.property("lastMoveTime")));

					final List list = criteria.list();
					for (Object o : list) {
						final Object[] values = (Object[]) o;
						final Long id = (Long) values[0];
						final Date d = new Date(((Date) values[2]).getTime() + ((Number) values[1]).intValue() * MILLIS_IN_DAY);
						scheduleTermination(id, d);
					}
				}
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected boolean executeTermination(Long boardId) {
		try {
			final ScribbleBoard board = boardManager.openBoard(boardId);
			if (board != null) {
				if (!board.isActive()) {
					log.info("Terminate game: {}", boardId);
					board.terminate();
					return true;
				} else {
                    log.info("Looks like the game still active: {}. Current time: {}, Last move time: {}/{}, Millis per move: {}",
                            boardId, System.currentTimeMillis(), board.getLastMoveTime().getTime(), board.getLastMoveTime(),
                            board.getSettings().getDaysPerMove() * 86400000);
                }
			} else {
				return true; // no board - nothing to do
			}
		} catch (BoardLoadingException e) {
			log.error("Board {} can't be loaded for termination", boardId, e);
		} catch (GameMoveException e) {
			log.error("Board {} can't be terminated", boardId, e);
		}
		return false;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setBoardManager(ScribblePlayManager boardManager) {
		if (this.boardManager != null) {
			this.boardManager.removeBoardListener(stateListener);
		}

		this.boardManager = boardManager;

		if (this.boardManager != null) {
			this.boardManager.addBoardListener(stateListener);
		}
	}

	private class TheBoardListener implements BoardListener {
		private TheBoardListener() {
		}

		@Override
		public void gameStarted(GameBoard<? extends GameSettings, ? extends GamePlayerHand, ? extends GameMove> board) {
			gameMoveDone(board, null, null);
		}

		@Override
		public void gameMoveDone(GameBoard<? extends GameSettings, ? extends GamePlayerHand, ? extends GameMove> board, GameMove move, GameMoveScore moveScore) {
			scheduleTermination(board.getBoardId(), new Date(board.getLastMoveTime().getTime() + board.getSettings().getDaysPerMove() * MILLIS_IN_DAY));
		}

		@Override
		public void gameFinished(GameBoard<? extends GameSettings, ? extends GamePlayerHand, ? extends GameMove> board, GameResolution resolution, Collection<Personality> winners) {
			cancelTermination(board.getBoardId());
		}
	}
}
