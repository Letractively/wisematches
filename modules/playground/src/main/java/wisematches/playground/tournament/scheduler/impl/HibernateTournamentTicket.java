package wisematches.playground.tournament.scheduler.impl;

import wisematches.personality.Language;
import wisematches.personality.Personality;
import wisematches.playground.tournament.TournamentSection;
import wisematches.playground.tournament.scheduler.TournamentPoster;
import wisematches.playground.tournament.scheduler.TournamentTicket;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Entity
@Table(name = "tournament_ticket")
class HibernateTournamentTicket implements TournamentTicket {
	@EmbeddedId
	private PK pk;

	@Column(name = "section")
	@Enumerated(EnumType.ORDINAL)
	private TournamentSection section;

	HibernateTournamentTicket() {
	}

	HibernateTournamentTicket(TournamentPoster poster, Personality player, Language language, TournamentSection section) {
		pk = new PK(poster, player, language);
		this.section = section;
	}

	long getPlayerId() {
		return pk.getPlayer();
	}

	int getTournamentId() {
		return pk.getPoster();
	}

	@Override
	public Language getLanguage() {
		return pk.getLanguage();
	}

	@Override
	public TournamentSection getSection() {
		return section;
	}

	public void setSection(TournamentSection section) {
		this.section = section;
	}

	@Embeddable
	protected static class PK implements Serializable {
		@Column(name = "poster")
		private int poster;

		@Column(name = "player")
		private long player;

		@Column(name = "language")
		@Enumerated(EnumType.STRING)
		private Language language;

		PK() {
		}

		PK(TournamentPoster poster, Personality player, Language language) {
			this.poster = poster.getNumber();
			this.player = player.getId();
			this.language = language;
		}

		public int getPoster() {
			return poster;
		}

		public long getPlayer() {
			return player;
		}

		public Language getLanguage() {
			return language;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			PK pk = (PK) o;
			return player == pk.player && poster == pk.poster && language == pk.language;
		}

		@Override
		public int hashCode() {
			int result = poster;
			result = 31 * result + (int) (player ^ (player >>> 32));
			result = 31 * result + language.hashCode();
			return result;
		}
	}
}
