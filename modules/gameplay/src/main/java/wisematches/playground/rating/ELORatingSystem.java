package wisematches.playground.rating;

import wisematches.playground.RatingSystem;

/**
 * This is implementation of  <a href="http://en.wikipedia.org/wiki/Elo_rating_system">ELO Rating System</a>.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class ELORatingSystem implements RatingSystem {
	public ELORatingSystem() {
	}

	@Override
	public String getName() {
		return "ELO";
	}

	public short[] calculateRatings(short[] ratings, short[] points) {
		if (ratings == null) {
			throw new NullPointerException("Players array can't be null");
		}
		if (points == null) {
			throw new NullPointerException("Points can't be null");
		}
		if (ratings.length < 2) {
			throw new IllegalArgumentException("For score calculation at least two players should be specified");
		}
		if (ratings.length != points.length) {
			throw new IllegalArgumentException("Players number does not equals with points number");
		}

		final short[] res = new short[ratings.length];
		for (int i = 0; i < ratings.length; i++) {
			final float rating1 = ratings[i];
			final float points1 = points[i];

			// Now calculate delta with each other players...
			float expectedPoints = 0;
			float actualPoints = 0;
			for (int j = 0; j < ratings.length; j++) {
				if (i == j) {
					continue;
				}
				final float rating2 = ratings[j];
				final float points2 = points[j];
				actualPoints += getActualPoints(points1, points2);
				expectedPoints += getExpectedPoints(rating1, rating2);
			}
			res[i] = (short) Math.round(rating1 + getKValue(rating1) * (actualPoints - expectedPoints));
		}
		return res;
	}

	private float getActualPoints(float points1, float points2) {
		float sea = 0;
		if (points1 > points2) {
			sea = 1.f;
		} else if (points1 == points2) {
			sea = 0.5f;
		}
		return sea;
	}

	/**
	 * Returns delta between expected and actual score points.
	 *
	 * @param rating1 the rating of first player
	 * @param rating2 the rating of second player
	 * @return the delta between expected and actual score points.
	 */
	private float getExpectedPoints(float rating1, float rating2) {
		return (float) (1 / (1 + Math.pow(10, (rating2 - rating1) / 400f)));
	}

	private float getKValue(float rating) {
		if (rating <= 2200) { //2200 - masters
			return 32;
		}
		return 16;
	}
}
