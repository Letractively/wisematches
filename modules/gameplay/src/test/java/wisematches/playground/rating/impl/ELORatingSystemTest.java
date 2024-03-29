package wisematches.playground.rating.impl;

import org.junit.Test;
import wisematches.playground.rating.ELORatingSystem;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class ELORatingSystemTest {
	public ELORatingSystemTest() {
	}

	@Test
	public void testCalculateRatings() throws Exception {
		short[] ints;
		final ELORatingSystem e = new ELORatingSystem();

		ints = e.calculateRatings(
				new short[]{1613, 1609, 1477, 1388, 1586, 1720},
				new short[]{100, 200, 100, 80, 80, 200});
		assertEquals(1601, ints[0]);

		ints = e.calculateRatings(
				new short[]{1613, 1609, 1477, 1388, 1586, 1720},
				new short[]{100, 80, 80, 200, 100, 100});
		assertEquals(1617, ints[0]);
	}
}
