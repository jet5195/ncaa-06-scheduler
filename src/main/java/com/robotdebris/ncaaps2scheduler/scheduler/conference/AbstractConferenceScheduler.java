package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.SchedulerUtils;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.robotdebris.ncaaps2scheduler.SchedulerUtils.findEmptyWeeks;

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
            if (school.getNumOfConferenceGames() < numOfSchools - 1) {
                for (School opponent : list) {
                    if (!school.equals(opponent) && !school.isOpponent(opponent)) {
                        int week = findConfGameWeek(school, opponent);
                        if ((school.getNumOfAwayConferenceGames() >= numOfSchools / 2)
                                || opponent.getNumOfHomeConferenceGames() >= numOfSchools / 2) {
                            // add a home game for school
                            if (gameRepository.getYear() % 2 == 0) {
                                addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), false);
                            } else {
                                addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), false);
                            }
                        } else if ((school.getNumOfHomeConferenceGames() >= numOfSchools / 2)
                                || opponent.getNumOfAwayConferenceGames() >= numOfSchools / 2) {
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

    int findConfGameWeek(School school, School opponent) throws Exception {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
        // If both schools are each other's #1 rival, schedule the game for week 13 (14
        // in game).. or 12 if unavailable
        // TODO move games if week 13 isn't available
        // bug fix, first check if getRivals is null or empty
        if (!CollectionUtils.isEmpty(school.getRivals()) && !CollectionUtils.isEmpty(opponent.getRivals())
                && school.getRivals().get(0).equals(opponent)) {
            if (emptyWeeks.isEmpty()) {
                throw new Exception("No empty weeks available!");
            }
            if (emptyWeeks.contains(13)) {
                return 13;
            } else if (emptyWeeks.contains(12)) {
                return 12;
            }
        }
        if (emptyWeeks.size() > 1) {
            emptyWeeks.remove(Integer.valueOf(14));
        }
        if (emptyWeeks.isEmpty()) {
            throw new Exception("No empty weeks available!");
        }
        return SchedulerUtils.randomizeWeek(emptyWeeks);
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
        if (!school1.isOpponent(school2) && school1.getSchedule().size() < 12 && school2.getSchedule().size() < 12
                && school1.getGameByWeek(week) == null && school2.getGameByWeek(week) == null) {
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
