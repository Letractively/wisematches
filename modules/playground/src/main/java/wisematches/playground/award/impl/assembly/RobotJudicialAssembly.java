package wisematches.playground.award.impl.assembly;

import wisematches.personality.Personality;
import wisematches.personality.player.computer.robot.RobotType;
import wisematches.playground.award.AwardWeight;
import wisematches.playground.tracking.StatisticManager;
import wisematches.playground.tracking.Statistics;
import wisematches.playground.tracking.StatisticsListener;

import java.util.Set;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class RobotJudicialAssembly extends AbstractJudicialAssembly {
	private StatisticManager statisticManager;
	private final TheAwardsListener listener = new TheAwardsListener();

	public RobotJudicialAssembly() {
	}

	protected void processStatisticUpdated(Personality person, Set<String> changes, Statistics statistics) {
		final int dullWins = statistics.getRobotWins(RobotType.DULL);
		final int traineeWins = statistics.getRobotWins(RobotType.TRAINEE);
		final int expertsWins = statistics.getRobotWins(RobotType.EXPERT);
		if (changes.contains("robotWinsDull")) {
			if (dullWins == 10) {
				grantAward(person, "robot.conqueror", AwardWeight.BRONZE, null);

				if (traineeWins >= 5) {
					grantAward(person, "robot.conqueror", AwardWeight.SILVER, null);

					if (expertsWins >= 1) {
						grantAward(person, "robot.conqueror", AwardWeight.GOLD, null);
					}
				}
			}
		} else if (changes.contains("robotWinsTrainee")) {
			if (dullWins >= 10 && traineeWins == 5) {
				grantAward(person, "robot.conqueror", AwardWeight.SILVER, null);

				if (expertsWins >= 1) {
					grantAward(person, "robot.conqueror", AwardWeight.BRONZE, null);
				}
			}
		} else if (changes.contains("robotWinsExpert")) {
			if (dullWins >= 10 && traineeWins >= 5 && expertsWins == 1) {
				grantAward(person, "robot.conqueror", AwardWeight.GOLD, null);
			}
		}
	}

	public void setStatisticManager(StatisticManager statisticManager) {
		if (this.statisticManager != null) {
			this.statisticManager.removeStatisticListener(listener);
		}

		this.statisticManager = statisticManager;

		if (this.statisticManager != null) {
			this.statisticManager.addStatisticListener(listener);
		}
	}

	private final class TheAwardsListener implements StatisticsListener {
		private TheAwardsListener() {
		}

		@Override
		public void playerStatisticUpdated(Personality personality, Set<String> changes, Statistics statistic) {
			processStatisticUpdated(personality, changes, statistic);
		}
	}
}