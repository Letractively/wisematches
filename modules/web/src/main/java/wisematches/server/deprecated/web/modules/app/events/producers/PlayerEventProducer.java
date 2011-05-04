package wisematches.server.deprecated.web.modules.app.events.producers;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class PlayerEventProducer {//implements EventProducer {
/*
    private EventNotificator notificator;

    private PlayerStatisticManagerImpl statisticsManager;

    private final AccountLockListener lockAccountListener = new TheLockAccountListener();
    private final PlayerStatisticListener playerStatisticListener = new ThePlayerStatisticListener();
    private AccountLockManager lockAccountManager;

    public void activateProducer(EventNotificator notificator) {
        this.notificator = notificator;
    }

    public void deactivateProducer() {
        this.notificator = null;
    }

    public void setPlayerStatisticManager(PlayerStatisticManagerImpl statisticsManager) {
        if (this.statisticsManager != null) {
            this.statisticsManager.removePlayerStatisticListener(playerStatisticListener);
        }

        this.statisticsManager = statisticsManager;

        if (this.statisticsManager != null) {
            this.statisticsManager.addPlayerStatisticListener(playerStatisticListener);
        }
    }

    public void setAccountLockManager(AccountLockManager lockAccountManager) {
        if (this.lockAccountManager != null) {
            this.lockAccountManager.removeAccountLockListener(lockAccountListener);
        }

        this.lockAccountManager = lockAccountManager;

        if (this.lockAccountManager != null) {
            this.lockAccountManager.addAccountLockListener(lockAccountListener);
        }
    }

    public static PlayerInfoBean convertPlayer(final long playerId, final PlayerManager playerManager) {
        return convertPlayer(playerManager.getPlayer(playerId));
    }

    public static PlayerInfoBean convertPlayer(final Player player) {
        return new PlayerInfoBean(player.getId(), player.getNickname(), getMemberType(player), player.getRating());
    }

    public static MemberType getMemberType(Player player) {
        if (player instanceof RobotPlayer) {
            return MemberType.ROBOT;
        } else if (player instanceof GuestPlayer) {
            return MemberType.GUEST;
        }
        return MemberType.GUEST;
    }

    private class ThePlayerStatisticListener implements PlayerStatisticListener {
        public void playerStatisticUpdated(long playerId, HibernatePlayerStatistic statistic) {
            if (notificator != null) {
                notificator.fireEvent(new PlayerStatisticEvent(playerId));
            }
        }
    }

    private class TheLockAccountListener implements AccountLockListener {
        public void accountLocked(Player player, String publicReason, String privateReason, Date unlockdate) {
            if (notificator != null) {
                notificator.fireEvent(new PlayerLockedEvent(player.getId(), publicReason, unlockdate.getTime()));
            }
        }

        public void accountUnlocked(Player player) {
            if (notificator != null) {
                notificator.fireEvent(new PlayerUnlockedEvent(player.getId()));
            }
        }
    }
*/
}
