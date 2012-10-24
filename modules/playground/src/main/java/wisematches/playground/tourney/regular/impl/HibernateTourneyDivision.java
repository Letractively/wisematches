package wisematches.playground.tourney.regular.impl;

import wisematches.personality.Language;
import wisematches.playground.tourney.regular.TourneyDivision;
import wisematches.playground.tourney.regular.TourneySection;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Entity
@Table(name = "tourney_regular_division")
public class HibernateTourneyDivision implements TourneyDivision {
	@Column(name = "id")
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long internalId;

	@Column(name = "activeRound")
	private int activeRound;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "language", updatable = false)
	private Language language;

	@Column(name = "section", updatable = false)
	private TourneySection section;

	@JoinColumn(name = "tourneyId")
	@OneToOne(fetch = FetchType.LAZY)
	private HibernateTourney tourney;

	@Column(name = "started")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedDate;

	@Column(name = "finished")
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedDate;

	@Deprecated
	private HibernateTourneyDivision() {
	}

	public HibernateTourneyDivision(HibernateTourney tourney, Language language, TourneySection section) {
		this.tourney = tourney;
		this.language = language;
		this.section = section;
		this.activeRound = 0;
	}


	long getInternalId() {
		return internalId;
	}

	@Override
	public State getState() {
		return State.getState(startedDate, finishedDate);
	}

	@Override
	public int getActiveRound() {
		return activeRound;
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public TourneySection getSection() {
		return section;
	}

	@Override
	public HibernateTourney getTourney() {
		return tourney;
	}

	@Override
	public Id getId() {
		return new Id(tourney.getId(), language, section);
	}

	@Override
	public Date getStartedDate() {
		return startedDate;
	}

	@Override
	public Date getFinishedDate() {
		return finishedDate;
	}

	void startRound(HibernateTourneyRound round) {
		if (finishedDate != null) {
			throw new IllegalStateException("Division already finished");
		}
		if (activeRound == 0) {
			startedDate = new Date();
		}
		activeRound = round.getRound();
	}

	void finishRound(HibernateTourneyRound round) {
		if (finishedDate != null) {
			throw new IllegalStateException("Division already finished");
		}
		activeRound = 0;
		if (round.isFinal()) {
			finishedDate = new Date();
		}
	}

	@Override
	public String toString() {
		return "HibernateTourneyDivision{" +
				"internalId=" + internalId +
				", round=" + activeRound +
				", tourney=" + tourney +
				", section=" + section +
				", language=" + language +
				", startedDate=" + startedDate +
				", finishedDate=" + finishedDate +
				'}';
	}
}
