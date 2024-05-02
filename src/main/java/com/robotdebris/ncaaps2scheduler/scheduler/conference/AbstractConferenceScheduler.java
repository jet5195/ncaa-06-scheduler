package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@Component
abstract class AbstractConferenceScheduler implements ConferenceScheduler {

	GameRepository gameRepository;
	private ScheduleService scheduleService;

	@Autowired
	public void AbstractSchedulerFactory(GameRepository gameRepository, ScheduleService scheduleService) {
		this.gameRepository = gameRepository;
		this.scheduleService = scheduleService;
	}

	public void scheduleRoundRobinConfGames(Conference conf) throws Exception {
		scheduleRoundRobinConfGames(conf.getSchools(), conf.getConfGamesStartWeek());
	}

	void scheduleRoundRobinConfGames(List<School> list, int confGamesStartDate) throws Exception {
		int numOfSchools = list.size();
		for (School school : list) {
			if (scheduleService.getNumOfConferenceGamesForSchool(school) < numOfSchools - 1) {
				for (School opponent : list) {
					if (!school.equals(opponent) && !scheduleService.isOpponentForSchool(school, opponent)) {
						int week = scheduleService.findConfGameWeek(school, opponent);
						if ((scheduleService.getNumOfAwayConferenceGamesForSchool(school) >= numOfSchools / 2)
								|| scheduleService.getNumOfHomeConferenceGamesForSchool(opponent) >= numOfSchools / 2) {
							// add a home game for school
							if (gameRepository.getYear() % 2 == 0) {
								addYearlySeriesHelper(opponent, school, week, false);
							} else {
								addYearlySeriesHelper(school, opponent, week, false);
							}
						} else if ((scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= numOfSchools / 2)
								|| scheduleService.getNumOfAwayConferenceGamesForSchool(opponent) >= numOfSchools / 2) {
							// add an away game for school
							if (gameRepository.getYear() % 2 == 0) {
								addYearlySeriesHelper(school, opponent, week, false);
							} else {
								addYearlySeriesHelper(opponent, school, week, false);
							}
						} else {
							addYearlySeriesHelper(school, opponent, week, false);
						}
					}
				}
			}
		}
	}

	List<School> orderDivByXDivRivals(List<School> div1) {
		List<School> orderedDiv = new ArrayList<>();
		for (School school : div1) {
			orderedDiv.add(school.getxDivRival());
		}
		return orderedDiv;
	}

//    private boolean addYearlySeriesHelper(School s1, School s2, int week, int day, int year, boolean specifyHome) {
//        School school1 = schoolService.schoolSearch(s1);
//        School school2 = schoolService.schoolSearch(s2);
//        return addYearlySeriesHelper(school1, school2, week, day, year, specifyHome);
//    }

	/**
	 * Adds a game to the schedule, alternating home and away teams based on the
	 * year if not specified.
	 * 
	 * @param school1     The first school.
	 * @param school2     The second school.
	 * @param week        The week of the game.
	 * @param year        The current year.
	 * @param specifyHome If true, school1 is away and school2 is home; if false,
	 *                    alternates yearly.
	 */
	void addYearlySeriesHelper(School school1, School school2, int week, boolean specifyHome) {
		GameBuilder builder = new GameBuilder().setWeek(week);

		if (!specifyHome) {
			builder.setTeamsWithYearlyRotation(school1, school2, scheduleService.getYear());
		} else {
			builder.setAwayTeam(school1).setHomeTeam(school2);
		}

		Game game = builder.build();
		scheduleService.addGame(game);
	}

}
