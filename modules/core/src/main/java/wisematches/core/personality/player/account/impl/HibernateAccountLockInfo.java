package wisematches.core.personality.player.account.impl;

import wisematches.core.personality.player.account.Account;
import wisematches.core.personality.player.account.AccountLockInfo;

import javax.persistence.*;
import java.util.Date;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
@Entity
@Table(name = "account_lock")
class HibernateAccountLockInfo implements AccountLockInfo {
	@Transient
	private Account account;

	@Id
	@Column(name = "playerId")
	private long playerId;

	@Column(name = "publicReason")
	private String publicReason;

	@Column(name = "privateReason")
	private String privateReason;

	@Column(name = "lockDate")
	private Date lockDate;

	@Column(name = "unlockDate")
	private Date unlockDate;

	/**
	 * Constructor for Hibernate
	 */
	HibernateAccountLockInfo() {
	}

	HibernateAccountLockInfo(Account account, String publicReason, String privateReason, Date unlockDate) {
		this.account = account;
		this.playerId = account.getId();
		this.publicReason = publicReason;
		this.privateReason = privateReason;
		this.lockDate = new Date();
		this.unlockDate = unlockDate;
	}

	@Override
	public Account getAccount() {
		return account;
	}

	@Override
	public String getPublicReason() {
		return publicReason;
	}

	@Override
	public String getPrivateReason() {
		return privateReason;
	}

	@Override
	public Date getLockDate() {
		return lockDate;
	}

	@Override
	public Date getUnlockDate() {
		return unlockDate;
	}


	void setAccount(Account account) {
		this.account = account;
		this.playerId = account.getId();
	}

	void setPublicReason(String publicReason) {
		this.publicReason = publicReason;
	}

	void setPrivateReason(String privateReason) {
		this.privateReason = privateReason;
	}

	void setLockDate(Date lockDate) {
		this.lockDate = lockDate;
	}

	void setUnlockDate(Date unlockDate) {
		this.unlockDate = unlockDate;
	}

	@Override
	public String toString() {
		return "LockAccountInfo{" +
				"account=" + account +
				", playerId=" + playerId +
				", publicReason='" + publicReason + '\'' +
				", privateReason='" + privateReason + '\'' +
				", lockDate=" + lockDate +
				", unlockDate=" + unlockDate +
				'}';
	}
}
