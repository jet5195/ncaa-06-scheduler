package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@Component
public class FourteenTeamConferenceScheduler extends AbstractConferenceScheduler {

	@Autowired
	ScheduleService scheduleService;

	@Override
	public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) {
		try {
			scheduleConferenceGamesDivisions14(conference, gameRepository);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

	private void scheduleConferenceGamesDivisions14(Conference conf, GameRepository gameRepository) throws Exception {
		List<School> div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
		List<School> div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

		// schedule inner div games
		scheduleRoundRobinConfGames(div1, conf.getConfGamesStartWeek());
		scheduleRoundRobinConfGames(div2, conf.getConfGamesStartWeek());

		boolean xDivRivals = div1.get(0).getXDivRival() != null;
		int numOfConfGames = conf.getNumOfConfGames();
		int yearOffset = Math.abs(gameRepository.getYear() - 2005);

		// Handle scheduling based on the number of conference games and cross-division
		// rivals
		if (numOfConfGames == 8) {
			if (!xDivRivals) {
				scheduleWithoutRivals(div1, div2, yearOffset, gameRepository);
			} else {
				scheduleWithRivals(div1, div2, yearOffset, gameRepository);
			}
		}
	}

	private void scheduleWithRivals(List<School> div1, List<School> div2, int yearOffset, GameRepository gameRepository)
			throws Exception {
		div2 = orderDivByXDivRivals(div1);
		// add protected rivalry games
		int i = 0;
		for (School school : div1) {
			School opponent = div2.get(i);
			int week = scheduleService.findConfGameWeek(school, opponent);
			addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), false);
			i++;
		}
		// at this point all we have to do is alternate that 1 game.. home away SHOULD
		// be taken care of too due to xdivrival
		int year = (yearOffset % 12) + 1;
		int j = year;
		for (School school : div1) {
			// if we're in years 6-11, alternate the schedule a bit. IE, play team 2 then 1,
			// so we actually swap home/away
			if (year >= 7) {
				if (j >= 7) {
					// odd & we only go back 5 for the first school!!
					if (j % 2 != 0 && div1.get(0).equals(school)) {
						j -= 5;
						// even
					} else {
						j -= 7;
					}
				}
			} else {
				if (j >= 7) {
					j -= 7;
				}
			}
			School opponent = div2.get(j);
			int week = scheduleService.findConfGameWeek(school, opponent);
			if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) < 4) {
				addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), true);
			} else {
				addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);
			}
			j++;
		}
		/*
		 * 0 1 2 3 4 5 6 _____________________________ 2010 1 2 3 4 5 6 0 2011 2 3 4 5 6
		 * 0 1 2012 3 4 5 6 0 1 2 2013 4 5 6 0 1 2 3 2014 5 6 0 1 2 3 4 2015 6 0 1 2 3 4
		 * 5 2016 1 2 3 4 5 6 0
		 */

	}

	private void scheduleWithoutRivals(List<School> div1, List<School> div2, int yearOffset,
			GameRepository gameRepository) throws Exception {
		// so there's 7 different schedules, figure it out based on the year
		int modulo = yearOffset % 7;
		for (int i = 0; i < 7; i++) {
			School school = div1.get(i);
			int firstOpponent = i + modulo;
			if (firstOpponent >= 7) {
				firstOpponent -= 7;
			}
			School opponent = div2.get(firstOpponent);
			int week = scheduleService.findConfGameWeek(school, opponent);
			addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);

			int secondOpponent = firstOpponent + 1;
			if (secondOpponent >= 7) {
				secondOpponent -= 7;
			}
			School opponent2 = div2.get(secondOpponent);
			int week2 = scheduleService.findConfGameWeek(school, opponent2);
			addYearlySeriesHelper(opponent2, school, week2, 5, gameRepository.getYear(), true);
		}
		/*
		 *
		 * 0: 0 1 1 2 2 3 3 4 4 5 5 6 6 0 1: 1 2 2 3 3 4 4 5 5 6 6 0 0 1 2: 2 3 3 4 4 5
		 * 5 6 6 0 0 1 1 2 3: 3 4 4 5 5 6 6 0 0 1 1 2 2 3 4: 4 5 5: 5 6 6: 6 0
		 */

	}

}
