package wisematches.core.user;

/**
 * The {@code Player} interface represents simple user. This interface is read-only
 * and contains only methods that returns common and necessary information about the player,
 * like id, username, nickname, email and language.
 * <p/>
 * Any other player information, like gender, city and so on is defined in {@code PlayerProfile}
 * object.
 *
 * @author klimese
 */
public interface Player {
	/**
	 * Returns unique id of the player.
	 * <p/>
	 * This value can't be zero.
	 *
	 * @return unique, non zero id of the player.
	 */
	long getId();

	/**
	 * Returns a email of the player.
	 * <p/>
	 * This value can't be null.
	 *
	 * @return not null email of the player.
	 */
	String getEmail();

	/**
	 * Returns a username of the player.
	 * <p/>
	 * This value can't be null.
	 *
	 * @return not null username of the player.
	 */
	String getUsername();

	/**
	 * Returns default language of the player.
	 * <p/>
	 * This value can't be null.
	 *
	 * @return not null default language of the player.
	 */
	Language getLanguage();

	/**
	 * Returns current rating of the player.
	 *
	 * @return the current rating of the player.
	 */
	int getRating();
}