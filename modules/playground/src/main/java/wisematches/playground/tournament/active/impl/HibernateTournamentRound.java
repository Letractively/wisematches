package wisematches.playground.tournament.active.impl;

import wisematches.personality.Language;
import wisematches.playground.tournament.TournamentRound;
import wisematches.playground.tournament.TournamentSection;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
/*
@Entity
@Table(name = "tournament_round")
*/
public class HibernateTournamentRound implements TournamentRound {
	public HibernateTournamentRound() {
	}

	@Override
	public int getNumber() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getTournament() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Language getLanguage() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public TournamentSection getTournamentSection() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Date getStartDate() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Date getFinishDate() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getTotalGames() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getFinishedGames() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean isFinished() {
		throw new UnsupportedOperationException("Not implemented"); //To change body of implemented methods use File | Settings | File Templates.
	}
}
