package wisematches.server.services.message.impl;

import org.easymock.EasyMock;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import wisematches.core.Player;
import wisematches.core.PlayerType;
import wisematches.core.personality.DefaultPlayer;
import wisematches.playground.restriction.impl.RestrictionDescription;
import wisematches.playground.restriction.impl.RestrictionManagerImpl;
import wisematches.server.services.message.Message;
import wisematches.server.services.message.MessageDirection;
import wisematches.server.services.message.MessageManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/config/database-junit-config.xml",
		"classpath:/config/personality-config.xml",
		"classpath:/config/scribble-config.xml",
		"classpath:/config/playground-junit-config.xml"
})
public class HibernateMessageManagerTest {
	@Autowired
	MessageManager messageManager;

	public HibernateMessageManagerTest() {
	}

	@Test
	public void testMessage() {
		final Player p1 = new DefaultPlayer(901, null, null, null, null, null);
		final Player p2 = new DefaultPlayer(902, null, null, null, null, null);

		assertEquals(0, messageManager.getTodayMessagesCount(p1, MessageDirection.RECEIVED));
		assertEquals(0, messageManager.getTodayMessagesCount(p1, MessageDirection.SENT));
		assertEquals(0, messageManager.getTodayMessagesCount(p2, MessageDirection.SENT));
		assertEquals(0, messageManager.getTodayMessagesCount(p2, MessageDirection.RECEIVED));

		assertEquals(0, messageManager.getTodayMessagesCount(p1, MessageDirection.RECEIVED));
		assertEquals(0, messageManager.getTodayMessagesCount(p1, MessageDirection.SENT));
		assertEquals(0, messageManager.getTodayMessagesCount(p2, MessageDirection.SENT));
		assertEquals(0, messageManager.getTodayMessagesCount(p2, MessageDirection.RECEIVED));

		assertEquals(0, messageManager.getNewMessagesCount(p1));
		assertEquals(0, messageManager.getNewMessagesCount(p2));

		messageManager.sendMessage(p1, p2, "B1");
		assertEquals(0, messageManager.getNewMessagesCount(p1));
		assertEquals(1, messageManager.getNewMessagesCount(p2));

		messageManager.sendMessage(p1, p2, "B2");
		assertEquals(0, messageManager.getNewMessagesCount(p1));
		assertEquals(2, messageManager.getNewMessagesCount(p2));

		messageManager.sendMessage(p2, p1, "B3");
		assertEquals(1, messageManager.getNewMessagesCount(p1));
		assertEquals(2, messageManager.getNewMessagesCount(p2));

		assertEquals(2, messageManager.getMessages(p1, MessageDirection.SENT).size());
		assertEquals(1, messageManager.getNewMessagesCount(p1));
		assertEquals(2, messageManager.getNewMessagesCount(p2));

		assertEquals(1, messageManager.getMessages(p2, MessageDirection.SENT).size());
		assertEquals(1, messageManager.getNewMessagesCount(p1));
		assertEquals(2, messageManager.getNewMessagesCount(p2));

		assertEquals(1, messageManager.getMessages(p1, MessageDirection.RECEIVED).size());
		assertEquals(1, messageManager.getNewMessagesCount(p1)); // READ_COMMITTED isolation.
		assertEquals(2, messageManager.getNewMessagesCount(p2));  // READ_COMMITTED isolation.

		assertEquals(2, messageManager.getMessages(p2, MessageDirection.RECEIVED).size());
		assertEquals(1, messageManager.getNewMessagesCount(p1));  // READ_COMMITTED isolation.
		assertEquals(2, messageManager.getNewMessagesCount(p2));  // READ_COMMITTED isolation.

		assertEquals(1, messageManager.getTodayMessagesCount(p2, MessageDirection.SENT));
		assertEquals(2, messageManager.getTodayMessagesCount(p1, MessageDirection.SENT));
		assertEquals(2, messageManager.getTodayMessagesCount(p2, MessageDirection.RECEIVED));
		assertEquals(1, messageManager.getTodayMessagesCount(p1, MessageDirection.RECEIVED));
	}

	@Test
	public void testNotifications() {
		final Player p1 = new DefaultPlayer(901, null, null, null, null, null);
		final Player p2 = new DefaultPlayer(902, null, null, null, null, null);

		messageManager.sendNotification(p1, "B1");
		messageManager.sendNotification(p1, "B2");
		messageManager.sendNotification(p2, "B3");

		assertEquals(2, messageManager.getMessages(p1, MessageDirection.RECEIVED).size());
		assertEquals(1, messageManager.getMessages(p2, MessageDirection.RECEIVED).size());

		final Object[] objects = messageManager.getMessages(p1, MessageDirection.RECEIVED).toArray();
		final Message m1 = (Message) objects[0];
		assertEquals(901, m1.getRecipient());
		assertEquals("B1", m1.getText());

		final Message m2 = (Message) objects[1];
		assertEquals(901, m2.getRecipient());
		assertEquals("B2", m2.getText());

		final Message m3 = (Message) messageManager.getMessages(p2, MessageDirection.RECEIVED).toArray()[0];
		assertEquals(902, m3.getRecipient());
		assertEquals("B3", m3.getText());

		messageManager.removeMessage(p2, m3.getId(), MessageDirection.RECEIVED);
		assertEquals(2, messageManager.getMessages(p1, MessageDirection.RECEIVED).size());
		assertEquals(0, messageManager.getMessages(p2, MessageDirection.RECEIVED).size());
		assertEquals(1, messageManager.getTodayMessagesCount(p2, MessageDirection.RECEIVED));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCleanup() {
		final Map<PlayerType, Comparable<Integer>> a1 = new HashMap<>();
		a1.put(PlayerType.BASIC, 20);
		a1.put(PlayerType.SILVER, 40);
		a1.put(PlayerType.GOLD, 40);
		a1.put(PlayerType.PLATINUM, 40);

		final Map<PlayerType, Comparable<Integer>> a2 = new HashMap<>();
		a2.put(PlayerType.BASIC, 10);
		a2.put(PlayerType.SILVER, 20);
		a2.put(PlayerType.GOLD, 20);
		a2.put(PlayerType.PLATINUM, 20);

		final RestrictionManagerImpl r = new RestrictionManagerImpl();
		r.setRestrictions(Arrays.asList(
				new RestrictionDescription<>("messages.hist.private", 0, a1),
				new RestrictionDescription<>("messages.hist.notice", 0, a2)));

		final SQLQuery query = EasyMock.createMock(SQLQuery.class);
		EasyMock.expect(query.executeUpdate()).andReturn(1);
		EasyMock.replay(query);

		final Session session = EasyMock.createMock(Session.class);
		EasyMock.expect(session.createSQLQuery(EasyMock.isA(String.class))).andReturn(query);
		EasyMock.replay(session);

		final SessionFactory sessionFactory = EasyMock.createMock(SessionFactory.class);
		EasyMock.expect(sessionFactory.getCurrentSession()).andReturn(session);
		EasyMock.replay(sessionFactory);
/*
		expect(template.bulkUpdate("DELETE m FROM player_message as m INNER JOIN account_personality as a ON a.id=m.recipient and " +
                "((m.notification and " +
                "(a.membership = 'GUEST' and created < DATE_SUB(curdate(), INTERVAL 0 DAY)) or " +
                "(a.membership = 'BASIC' and created < DATE_SUB(curdate(), INTERVAL 20 DAY)) or " +
                "(a.membership = 'SECOND' and created < DATE_SUB(curdate(), INTERVAL 40 DAY)) or " +
                "(a.membership = 'FIRST' and created < DATE_SUB(curdate(), INTERVAL 40 DAY)) or " +
                "(a.membership = 'PLATINUM' and created < DATE_SUB(curdate(), INTERVAL 40 DAY))) or " +
                "(not m.notification and " +
                "(a.membership = 'GUEST' and created < DATE_SUB(curdate(), INTERVAL 0 DAY)) or " +
                "(a.membership = 'BASIC' and created < DATE_SUB(curdate(), INTERVAL 10 DAY)) or " +
                "(a.membership = 'SECOND' and created < DATE_SUB(curdate(), INTERVAL 20 DAY)) or " +
                "(a.membership = 'FIRST' and created < DATE_SUB(curdate(), INTERVAL 20 DAY)) or " +
                "(a.membership = 'PLATINUM' and created < DATE_SUB(curdate(), INTERVAL 20 DAY))))")).andReturn(0);
*/


		final HibernateMessageManager m = new HibernateMessageManager();
		m.setSessionFactory(sessionFactory);
		m.setRestrictionManager(r);

		m.cleanup();

		EasyMock.verify(sessionFactory, session, query);
	}
}