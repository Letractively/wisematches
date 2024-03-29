package wisematches.playground.tourney.regular;

/**
 * Tracks user subscriptions as well as any internal updates for the user subscription.
 * <p/>
 * Please note that in case of manual resubscription two methods will be invoked: unregistered and
 * when register again but if subscription was changed by manager, when resubsribed method will be invoked.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface RegistrationListener {
	/**
	 * Indicates that a player has been registered to a tourney.
	 *
	 * @param subscription information about registered player and subscription details.
	 * @param reasonKey    contains reason key is subscription was created by system. For example, that player won previous round.
	 *                     It's always null in case of manual subscription.
	 */
	void registered(RegistrationRecord subscription, String reasonKey);

	/**
	 * Indicates that a player has been unregistered from a tourney.
	 *
	 * @param subscription subscription information
	 * @param reasonKey    contains reason key is subscription was created by system. For example, that player won previous round.
	 *                     It's always null in case of manual subscription.
	 */
	void unregistered(RegistrationRecord subscription, String reasonKey);

	/**
	 * Indicates that subscription section was changed for the specified player.
	 *
	 * @param player     the player id
	 * @param tourney    the tourney number
	 * @param oldSection old section.
	 * @param newSection new section. Can be null if player is moved outside tourney because there is no
	 *                   appropriate group where it can play.
	 */
	void sectionChanged(long player, int tourney, TourneySection oldSection, TourneySection newSection);
}
