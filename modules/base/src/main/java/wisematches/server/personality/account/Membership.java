package wisematches.server.personality.account;

/**
 * Each player has one of predefined membership listed in this enumeration.
 * <p/>
 * According to the membership some features can be limited or disable for the player.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public enum Membership {
	GUEST,

	ROBOT,
	/**
	 * This is regular player without any specific features.
	 */
	BASIC,
	/**
	 * Silver player. It's more well than the <i>GUEST<i> player.
	 */
	SILVER,
	/**
	 * Gold player. It's more well than the <i>GOLD<i> player.
	 */
	GOLD,
	/**
	 * Platinum player. A player with this membership has full access.
	 */
	PLATINUM
}