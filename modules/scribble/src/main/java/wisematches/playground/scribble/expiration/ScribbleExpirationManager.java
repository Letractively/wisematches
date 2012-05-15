package wisematches.playground.scribble.expiration;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import wisematches.playground.*;
import wisematches.playground.expiration.impl.AbstractExpirationManager;
import wisematches.playground.scribble.ScribbleBoard;
import wisematches.playground.scribble.ScribbleBoardManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class ScribbleExpirationManager extends AbstractExpirationManager<Long, ScribbleExpirationType> implements InitializingBean {
	private SessionFactory sessionFactory;
	private ScribbleBoardManager boardManager;

	private final TheBoardStateListener stateListener = new TheBoardStateListener();

	private static final int MILLIS_IN_DAY = 86400000;//24 * 60 * 60 * 1000;

	public ScribbleExpirationManager() {
		super(ScribbleExpirationType.class);
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
							.add(Restrictions.isNull("gameResolution"))
							.setProjection(Projections.projectionList()
									.add(Projections.property("boardId"))
									.add(Projections.property("gameSettings.daysPerMove").as("daysPerMove"))
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
			final GameBoard board = boardManager.openBoard(boardId);
			if (board != null) {
				if (!board.isGameActive()) {
					log.info("Terminate game: " + boardId);
					board.terminate();
					return true;
				} else {
					log.info("Looks like the game still active: " + boardId);
				}
			} else {
				return true; // no board - nothing to do
			}
		} catch (BoardLoadingException e) {
			log.error("Board " + boardId + " can't be loaded for termination", e);
		} catch (GameMoveException e) {
			log.error("Board " + boardId + " can't be terminated", e);
		}
		return false;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setBoardManager(ScribbleBoardManager boardManager) {
		if (this.boardManager != null) {
			this.boardManager.removeBoardStateListener(stateListener);
		}

		this.boardManager = boardManager;

		if (this.boardManager != null) {
			this.boardManager.addBoardStateListener(stateListener);
		}
	}

	private class TheBoardStateListener implements BoardStateListener {
		private TheBoardStateListener() {
		}

		@Override
		public void gameStarted(GameBoard<? extends GameSettings, ? extends GamePlayerHand> board) {
			gameMoveDone(board, null, null);
		}

		@Override
		public void gameMoveDone(GameBoard<? extends GameSettings, ? extends GamePlayerHand> board, GameMove move, GameMoveScore moveScore) {
			scheduleTermination(board.getBoardId(), new Date(board.getLastMoveTime().getTime() + board.getGameSettings().getDaysPerMove() * MILLIS_IN_DAY));
		}

		@Override
		public void gameFinished(GameBoard<? extends GameSettings, ? extends GamePlayerHand> board, GameResolution resolution, Collection<? extends GamePlayerHand> winners) {
			cancelTermination(board.getBoardId());
		}
	}
}