package wisematches.server.standing.statistic.impl;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import wisematches.server.personality.account.*;
import wisematches.server.standing.statistic.RatingsStatistic;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/config/test-server-base-config.xml",
		"classpath:/config/database-config.xml",
		"classpath:/config/server-base-config.xml"
})
public class PlayerStatisticDaoTest {
	private Account person;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private PlayerStatisticDao playerStatisticDao;

	public PlayerStatisticDaoTest() {
	}

	@Before
	public void setUp() throws InadmissibleUsernameException, DuplicateAccountException {
		final String uuid = UUID.randomUUID().toString();
		person = accountManager.createAccount(new AccountEditor(uuid + "@mock.wm", uuid, "AS").createAccount());
	}

	@After
	public void tearDown() throws UnknownAccountException {
		accountManager.removeAccount(person);
	}

	@Test
	public void test_playerStatistic() throws InterruptedException, InadmissibleUsernameException, DuplicateAccountException {
		final HibernatePlayerStatistic statistic = playerStatisticDao.loadPlayerStatistic(person);
		assertNotNull(statistic);
		final long time = System.currentTimeMillis() - 1000;

		statistic.setAverageTurnTime(1);
		statistic.incrementDrawGames();
		statistic.incrementDrawGames();
		statistic.setLastMoveTime(new Date(300000));
		statistic.incrementLostGames();
		statistic.incrementLostGames();
		statistic.incrementLostGames();
		statistic.incrementLostGames();
		statistic.incrementTimeouts();
		statistic.incrementTimeouts();
		statistic.incrementTimeouts();
		statistic.incrementTimeouts();
		statistic.incrementTimeouts();
		statistic.incrementTurnsCount();
		statistic.incrementTurnsCount();
		statistic.incrementTurnsCount();
		statistic.incrementTurnsCount();
		statistic.incrementTurnsCount();
		statistic.incrementTurnsCount();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementWonGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		statistic.incrementActiveGames();
		playerStatisticDao.savePlayerStatistic(statistic);

		final HibernateRatingsStatistic ri1 = statistic.getRatingsStatistic();
		ri1.setHighestRating(1);
		ri1.setLowestRating(2);
		ri1.setAverageMovesPerGame(3);
		ri1.setAverageOpponentRating(4);
		ri1.setAverageRating(5);
		ri1.setHighestWonOpponentRating(6);
		ri1.setHighestWonOpponentId(7);
		ri1.setLowestLostOpponentRating(8);
		ri1.setLowestLostOpponentId(9);
		playerStatisticDao.savePlayerStatistic(statistic);

		final HibernateMovesStatistic ws = statistic.getMovesStatistic();
		ws.setWordsCount(2);
		ws.setAvgWordLength(3);
		ws.setAvgWordPoints(4);
		ws.setMaxWordLength(5);
		ws.setMaxWordPoints(6);
		playerStatisticDao.savePlayerStatistic(statistic);

/*
		final HibernateRatingsStatistic ri2 = statistic.getNinetyDaysRatingInfo();
		ri2.setHighestRating(10);
		ri2.setLowestRating(20);
		ri2.setAverageMovesPerGame(30);
		ri2.setAverageOpponentRating(40);
		ri2.setAverageRating(50);
		ri2.setHighestWonOpponentRating(60);
		ri2.setHighestWonOpponentId(70);
		ri2.setLowestLostOpponentRating(80);
		ri2.setLowestLostOpponentId(90);
		playerStatisticManager.updatePlayerStatistic(statistic);

		final HibernateRatingsStatistic ri3 = statistic.getThirtyDaysRatingInfo();
		ri3.setHighestRating(100);
		ri3.setLowestRating(200);
		ri3.setAverageMovesPerGame(300);
		ri3.setAverageOpponentRating(400);
		ri3.setAverageRating(500);
		ri3.setHighestWonOpponentRating(600);
		ri3.setHighestWonOpponentId(700);
		ri3.setLowestLostOpponentRating(800);
		ri3.setLowestLostOpponentId(900);
		playerStatisticManager.updatePlayerStatistic(statistic);

		final HibernateRatingsStatistic ri4 = statistic.getYearRatingInfo();
		ri4.setHighestRating(1000);
		ri4.setLowestRating(2000);
		ri4.setAverageMovesPerGame(3000);
		ri4.setAverageOpponentRating(4000);
		ri4.setAverageRating(5000);
		ri4.setHighestWonOpponentRating(6000);
		ri4.setHighestWonOpponentId(7000);
		ri4.setLowestLostOpponentRating(8000);
		ri4.setLowestLostOpponentId(9000);
		playerStatisticManager.updatePlayerStatistic(statistic);
*/

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		final HibernatePlayerStatistic s = playerStatisticDao.loadPlayerStatistic(person);
		assertEquals(1, s.getAverageTurnTime());
		assertEquals(2, s.getDrawGames());
		assertEquals(300000, s.getLastMoveTime().getTime());
		assertEquals(4, s.getLostGames());
		assertEquals(5, s.getTimeouts());
		assertEquals(6, s.getTurnsCount());
		assertEquals(7, s.getWonGames());
		assertEquals(8, s.getActiveGames());
		assertTrue(s.getUpdateTime().getTime() >= time);

		final RatingsStatistic sri1 = s.getRatingsStatistic();
		assertEquals(1, sri1.getHighest());
		assertEquals(2, sri1.getLowest());
		assertEquals(3, sri1.getAverageMovesPerGame());
		assertEquals(4, sri1.getAverageOpponentRating());
		assertEquals(5, sri1.getAverage());
		assertEquals(6, sri1.getHighestWonOpponentRating());
		assertEquals(7, sri1.getHighestWonOpponentId());
		assertEquals(8, sri1.getLowestLostOpponentRating());
		assertEquals(9, sri1.getLowestLostOpponentId());

		final HibernateMovesStatistic ws2 = s.getMovesStatistic();
		assertEquals(2, ws2.getWordsCount());
		assertEquals(3, ws2.getAverageWordLength());
		assertEquals(4, ws2.getAvgPoints());
		assertEquals(5, ws2.getMaxWordLength());
		assertEquals(6, ws2.getMaxWordPoints());

/*
		final HibernateRatingsStatistic sri2 = s.getNinetyDaysRatingInfo();
		assertEquals(10, sri2.getHighest());
		assertEquals(20, sri2.getLowest());
		assertEquals(30, sri2.getAverageMovesPerGame());
		assertEquals(40, sri2.getAverageOpponentRating());
		assertEquals(50, sri2.getAverage());
		assertEquals(60, sri2.getHighestWonOpponentRating());
		assertEquals(70, sri2.getHighestWonOpponentId());
		assertEquals(80, sri2.getLowestLostOpponentRating());
		assertEquals(90, sri2.getLowestLostOpponentId());

		final HibernateRatingsStatistic sri3 = s.getThirtyDaysRatingInfo();
		assertEquals(100, sri3.getHighest());
		assertEquals(200, sri3.getLowest());
		assertEquals(300, sri3.getAverageMovesPerGame());
		assertEquals(400, sri3.getAverageOpponentRating());
		assertEquals(500, sri3.getAverage());
		assertEquals(600, sri3.getHighestWonOpponentRating());
		assertEquals(700, sri3.getHighestWonOpponentId());
		assertEquals(800, sri3.getLowestLostOpponentRating());
		assertEquals(900, sri3.getLowestLostOpponentId());

		final HibernateRatingsStatistic sri4 = statistic.getYearRatingInfo();
		assertEquals(1000, sri4.getHighest());
		assertEquals(2000, sri4.getLowest());
		assertEquals(3000, sri4.getAverageMovesPerGame());
		assertEquals(4000, sri4.getAverageOpponentRating());
		assertEquals(5000, sri4.getAverage());
		assertEquals(6000, sri4.getHighestWonOpponentRating());
		assertEquals(7000, sri4.getHighestWonOpponentId());
		assertEquals(8000, sri4.getLowestLostOpponentRating());
		assertEquals(9000, sri4.getLowestLostOpponentId());
*/

		playerStatisticDao.removePlayerStatistic(s);
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		assertNull(playerStatisticDao.loadPlayerStatistic(person));
	}
}
