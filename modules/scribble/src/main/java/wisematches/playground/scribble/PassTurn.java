package wisematches.playground.scribble;

import wisematches.core.Personality;
import wisematches.playground.GameMove;

import java.util.Date;

/**
 * Predefined move that indicates that turn was passed.
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public final class PassTurn extends GameMove {
	protected PassTurn(Personality player) {
		super(player);
	}

	public PassTurn(Personality player, int points, Date moveTime) {
		super(player, points, moveTime);
	}
}