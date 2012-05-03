package wisematches.playground.scribble;

import wisematches.personality.Language;
import wisematches.playground.GameSettings;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Scribble game has a language of letters.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Embeddable
public final class ScribbleSettings extends GameSettings {
	@Column(name = "language", updatable = false)
	private String language;

	private static final long serialVersionUID = -5153027839777003987L;

	/**
	 * This is Hibernate constructor.
	 */
	protected ScribbleSettings() {
	}

	/**
	 * Creates new scribble game settings with specified parameters and board language.
	 *
	 * @param title	the title of the game.
	 * @param language the code of the language.
	 */
	public ScribbleSettings(String title, Language language) {
		this(title, language, DEFAULT_TIMEOUT_DAYS);
	}

	/**
	 * Creates new scribble game settings with specified parameters and board language.
	 *
	 * @param title	   the title of the game.
	 * @param language	the code of the language.
	 * @param daysPerMove days per move.
	 */
	public ScribbleSettings(String title, Language language, int daysPerMove) {
		this(title, language, daysPerMove, true, false);
	}

	public ScribbleSettings(String title, Language language, int daysPerMove, boolean ratedGame, boolean scratch) {
		super(title, daysPerMove, ratedGame, scratch);
		this.language = language.code();
	}

	@Override
	public int getMinPlayers() {
		return 2;
	}

	@Override
	public int getMaxPlayers() {
		return 4;
	}

	/**
	 * Returns code of the language.
	 *
	 * @return code of the language.
	 */
	public String getLanguage() {
		return language;
	}

	public static class Builder extends GameSettings.Builder {
		private Language language;

		public Builder() {
		}

		@Override
		public ScribbleSettings build() {
			return new ScribbleSettings(title, language, daysPerMove, ratedGame, scratch);
		}

		public Language getLanguage() {
			return language;
		}

		public void setLanguage(Language language) {
			this.language = language;
		}
	}
}
