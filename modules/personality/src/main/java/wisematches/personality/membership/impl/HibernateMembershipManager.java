package wisematches.personality.membership.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import wisematches.personality.Membership;
import wisematches.personality.Personality;
import wisematches.personality.Relationship;
import wisematches.personality.account.AccountManager;
import wisematches.personality.membership.MembershipActivation;
import wisematches.personality.membership.MembershipListener;
import wisematches.personality.membership.MembershipManager;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class HibernateMembershipManager implements MembershipManager {
	private SessionFactory sessionFactory;
	private AccountManager accountManager;

	private final Set<MembershipListener> listeners = new CopyOnWriteArraySet<>();

	private static final Log log = LogFactory.getLog("wisematches.server.membership");

	public HibernateMembershipManager() {
	}

	@Override
	public void addMembershipListener(MembershipListener l) {
		if (l != null) {
			listeners.add(l);
		}
	}

	@Override
	public void removeMembershipListener(MembershipListener l) {
		listeners.remove(l);
	}

/*
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void breakingDayTime(Date midnight) {
		final Session session = sessionFactory.getCurrentSession();
//
		// final Query query = session.createQuery("from HibernateMembershipActivation as r where r.started is not null and r.finished is null");

	}
*/

	@Override
	public MembershipActivation activateMembership(Personality person, Membership membership, int days, Relationship relationship) {
		throw new UnsupportedOperationException("TODO: Not implemented");
	}
	/*
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public MembershipActivation activateMembership(Personality person, Membership membership, int days, Relationship relationship) {
		final Session session = sessionFactory.getCurrentSession();

		final HibernateMembershipActivation activation = new HibernateMembershipActivation(person.getId(), membership, days, relationship);
		session.save(activation);

		HibernateActiveMembership activeMembership = (HibernateActiveMembership) session.get(HibernateActiveMembership.class, person.getId());
		if (activeMembership == null) {
			activeMembership = new HibernateActiveMembership(activation);
			session.save(activeMembership);

//			updateMembership(activeMembership);

			for (MembershipListener listener : listeners) {
				listener.membershipActivated(activation);
			}
		}
		return activation;
	}
*/

	/*

		private void updateMembership(HibernateActiveMembership activeMembership) {
			try {
				final long player = activeMembership.getPlayer();
				final Membership membership = activeMembership.getActivation().getMembership();

				final Account account = accountManager.getAccount(player);
				final AccountEditor editor = new AccountEditor(account);
				editor.setMembership(membership != null ? membership : Membership.BASIC);
				accountManager.updateAccount(editor.createAccount());
			} catch (Exception ex) {
				log.error("Membership can't be updated", ex);
			}
		}

	*/
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}
}