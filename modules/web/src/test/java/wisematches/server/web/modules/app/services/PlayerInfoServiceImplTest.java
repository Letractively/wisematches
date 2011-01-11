package wisematches.server.web.modules.app.services;

import static org.easymock.EasyMock.*;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import wisematches.kernel.player.Player;
import wisematches.server.core.account.PlayerManager;
import wisematches.server.core.statistic.PlayerStatistic;
import wisematches.server.core.statistic.StatisticsManager;
import wisematches.server.core.sessions.PlayerSessionsManager;
import wisematches.server.web.rpc.RemoteServiceContextAccessor;

/**
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class PlayerInfoServiceImplTest {
/*
    private PlayerInfoServiceImpl playerInfoService;

    @Before
    public void init() {
        playerInfoService = new PlayerInfoServiceImpl();
    }

    @After
    public void thearDown() {
        RemoteServiceContextAccessor.destroy();
    }

    @Test
    public void test_getSessionPlayer() throws SerializationException {
        final Player player = createStrictMock(Player.class);
        expect(player.getId()).andReturn(13L);
        expect(player.getUsername()).andReturn("mock");
        expect(player.getRating()).andReturn(123);
        replay(player);

        RemoteServiceContextAccessor.expectPlayer(player);
        final PlayerInfoBean player1 = playerInfoService.getSessionPlayer();
        assertEquals(13L, player1.getPlayerId());
        assertEquals("mock", player1.getPlayerName());
        assertEquals(123, player1.getCurrentRating());
        assertEquals("/rpc/PlayerImagesService?playerId=13", player1.getPlayerIconUrl());
        assertEquals(MemberType.BASIC, player1.getMemberType());
    }

    @Test
    public void test_getShortPlayerInfo() throws UnknownPlayerException {
        final PlayerManager playerManager = createStrictMock(PlayerManager.class);
        expect(playerManager.getPlayer(13L)).andReturn(null);
        replay(playerManager);

        final StatisticsManager statisticsManager = createStrictMock(StatisticsManager.class);

        playerInfoService.setPlayerManager(playerManager);
        playerInfoService.setStatisticsManager(statisticsManager);

        try {
            playerInfoService.getShortPlayerInfo(13L);
            fail("Exception must be here");
        } catch (UnknownPlayerException ex) {
            ;
        }

        final Player player = createMock(Player.class);
        expect(player.getRating()).andReturn(321);
        expect(player.getUsername()).andReturn("Test Name");
        replay(player);

        reset(playerManager);
        expect(playerManager.getPlayer(13L)).andReturn(player);
        replay(playerManager);

        final PlayerStatistic value = new PlayerStatistic(13L);
        value.setWonGames(2);
        value.setLostGames(1);
        value.setDrawGames(0);
        value.setAverageTurnTime(2);
        value.getNinetyDaysRaingInfo().setHighestRating(1); // 90days

        expect(statisticsManager.getPlayerStatistic(13L)).andReturn(value);
        replay(statisticsManager);

        final ShortPlayerInfo info = playerInfoService.getShortPlayerInfo(13L);
        assertEquals(1, info.getMaxRatings());
        assertEquals(2, info.getAverageTimePerMove());
        assertEquals(3, info.getFinishedGames());

        assertEquals(321, info.getCurrentRating());
        assertEquals(13L, info.getPlayerId());
        assertEquals("Test Name", info.getPlayerName());
        assertEquals(MemberType.BASIC, info.getMemberType());

        verify(player, statisticsManager, playerManager);
    }

    @Test
    public void test_getPlayerOnlineState() throws UnknownPlayerException {
        final Player player = createNiceMock("Player", Player.class);

        final PlayerManager playerManager = createStrictMock(PlayerManager.class);
        expect(playerManager.getPlayer(13L)).andReturn(null);
        expect(playerManager.getPlayer(13L)).andReturn(player);
        expect(playerManager.getPlayer(13L)).andReturn(player);
        replay(playerManager);

        final PlayerSessionsManager playerSessionsManager = createStrictMock(PlayerSessionsManager.class);
        expect(playerSessionsManager.isPlayerOnline(player)).andReturn(false);
        expect(playerSessionsManager.isPlayerOnline(player)).andReturn(true);
        replay(playerSessionsManager);

        playerInfoService.setPlayerSessionsManager(playerSessionsManager);
        playerInfoService.setPlayerManager(playerManager);

        try {
            playerInfoService.getPlayerOnlineState(13L);
            fail("Exception should be here");
        } catch (UnknownPlayerException ex) {
            ;
        }
        assertSame(PlayerOnlineState.OFFLINE, playerInfoService.getPlayerOnlineState(13L));
        assertSame(PlayerOnlineState.ONLINE, playerInfoService.getPlayerOnlineState(13L));

        verify(playerSessionsManager);
    }
*/
}