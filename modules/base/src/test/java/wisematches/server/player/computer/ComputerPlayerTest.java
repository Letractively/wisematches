package wisematches.server.player.computer;

import org.junit.Test;
import wisematches.server.player.computer.guest.GuestPlayer;
import wisematches.server.player.computer.robot.RobotPlayer;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class ComputerPlayerTest {
	@Test
	public void testNotUniquePlayers() {
		new ComputerPlayer(0, "mock", 100);
		try {
			new ComputerPlayer(0, "mock", 100);
			fail("Exception must be here");
		} catch (IllegalArgumentException ex) {
			;
		}
	}

	@Test
	public void testGetComputerPlayer() {
		ComputerPlayer p1 = new ComputerPlayer(999, "mock", 100);
		ComputerPlayer p2 = new ComputerPlayer(998, "mock", 101);

		assertSame(p1, ComputerPlayer.<ComputerPlayer>getComputerPlayer(999));
		assertSame(p2, ComputerPlayer.<ComputerPlayer>getComputerPlayer(998));
		assertNull(ComputerPlayer.<ComputerPlayer>getComputerPlayer(997));
	}

	@Test
	public void testGetComputerPlayers() {
		assertEquals(RobotPlayer.getRobotPlayers(), ComputerPlayer.getComputerPlayers(RobotPlayer.class));
		assertEquals(Arrays.asList(GuestPlayer.BASIC), ComputerPlayer.getComputerPlayers(GuestPlayer.class));
	}
}
