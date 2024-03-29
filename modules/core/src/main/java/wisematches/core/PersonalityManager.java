package wisematches.core;

/**
 * The {@code PersonalityManager} provides ability to get players and machineries by id.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface PersonalityManager {
	void addPersonalityListener(PersonalityListener listener);

	void removePersonalityListener(PersonalityListener listener);


	/**
	 * Returns {@code Robot}, {@code Visitor} or {@code Player} personality based on specified id.
	 * <p/>
	 * If there is no personality with specified id {@code null} will be returned.
	 *
	 * @param id the of person
	 * @return {@code Robot}, {@code Visitor} or {@code Player} personality based on specified id.
	 */
	Personality getPerson(Long id);


	/**
	 * Returns {@code Player} by specified id. If there is no player with the id {@code null} will be returned.
	 * <p/>
	 * This method doesn't check robots and visitors and works only with players.
	 *
	 * @param id the of player
	 * @return the player by specified id or {@code null}.
	 */
	Member getMember(Long id);

	/**
	 * Returns robot player by specified type.
	 *
	 * @param type the robot's type.
	 * @return the robot player by specified type.
	 */
	Robot getRobot(RobotType type);

	/**
	 * Returns visitor player by specified language.
	 *
	 * @param language the visitor's language.
	 * @return the visitor player by specified language.
	 */
	Visitor getVisitor(Language language);
}