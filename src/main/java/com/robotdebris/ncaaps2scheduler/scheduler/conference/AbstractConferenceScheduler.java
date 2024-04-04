package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                                addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), false);
                            } else {
                                addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), false);
                            }
                        } else if ((scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= numOfSchools / 2)
                                || scheduleService.getNumOfAwayConferenceGamesForSchool(opponent) >= numOfSchools / 2) {
                            // add an away game for school
                            if (gameRepository.getYear() % 2 == 0) {
                                addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), false);
                            } else {
                                addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), false);
                            }
                        } else {
                            addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), false);
                        }
                    }
                }
            }
        }
    }

    List<School> orderDivByXDivRivals(List<School> div1) {
        List<School> orderedDiv = new ArrayList<>();
        for (School school : div1) {
            orderedDiv.add(school.getXDivRival());
        }
        return orderedDiv;
    }

//    private boolean addYearlySeriesHelper(School s1, School s2, int week, int day, int year, boolean specifyHome) {
//        School school1 = schoolService.schoolSearch(s1);
//        School school2 = schoolService.schoolSearch(s2);
//        return addYearlySeriesHelper(school1, school2, week, day, year, specifyHome);
//    }

    boolean addYearlySeriesHelper(School school1, School school2, int week, int day, int year,
                                  boolean specifyHome) {
        if (!scheduleService.isOpponentForSchool(school1, school2) && scheduleService.getScheduleBySchool(school1).size() < 12 && scheduleService.getScheduleBySchool(school2).size() < 12
                && scheduleService.getGameBySchoolAndWeek(school1, week) == null && scheduleService.getGameBySchoolAndWeek(school2, week) == null) {
            // check if out of division conf opponent here?
            if (!specifyHome) {
                scheduleService.addGameYearlySeries(school1, school2, week, day, year);
            } else {
                scheduleService.addGameSpecificHomeTeam(school1, school2, week, day);
            }
            return true;
        }
        return false;
    }

}
