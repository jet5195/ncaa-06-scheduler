package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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

    public void scheduleRoundRobinConfGames(Conference conf) {
        scheduleRoundRobinConfGames(conf.getSchools());
    }

    /**
     * Schedule round-robin conference games for a list of schools based on specific
     * conditions.
     *
     * @param schoolList The list of schools participating in the round-robin
     *                   conference games.
     */
    void scheduleRoundRobinConfGames(List<School> schoolList) {
        boolean isEvenYear = gameRepository.getYear() % 2 == 0;
        // int i = 0;
        for (School school : schoolList) {
            List<School> opponentsToSchedule = schoolList.stream()
                    .filter(opponent -> !school.equals(opponent)
                            && !scheduleService.isOpponentForSchool(school, opponent))
                    .toList();
            int j = 0;
            for (School opponent : opponentsToSchedule) {
                boolean isHomeGame = isEvenYear ? j % 2 == 0 : j % 2 == 1;
                int week = scheduleService.findConfGameWeek(school, opponent);
                addYearlySeriesHelper(isHomeGame ? opponent : school, isHomeGame ? school : opponent, week,
                        true);
                j++;
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

    void scheduleCrossDivisionalRival(List<School> div1, List<School> div2, School school)
            throws Exception {
        School opponent = school.getxDivRival();
        int week = scheduleService.findConfGameWeek(school, opponent);
        // should be home or away game?
        if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= div1.size() / 2) {
            addYearlySeriesHelper(school, opponent, week, true);
        } else {
            addYearlySeriesHelper(opponent, school, week, true);
        }
    }

    // private boolean addYearlySeriesHelper(School s1, School s2, int week, int
    // day, int year, boolean specifyHome) {
    // School school1 = schoolService.schoolSearch(s1);
    // School school2 = schoolService.schoolSearch(s2);
    // return addYearlySeriesHelper(school1, school2, week, day, year, specifyHome);
    // }

    /**
     * Adds a game to the schedule, alternating home and away teams based on the
     * year if not specified.
     *
     * @param school1     The first school.
     * @param school2     The second school.
     * @param week        The week of the game.
     * @param specifyHome If true, school1 is away and school2 is home; if false,
     *                    alternates yearly.
     */
    void addYearlySeriesHelper(School school1, School school2, int week, boolean specifyHome) {
        GameBuilder builder = new GameBuilder().setWeek(week).setConferenceGame(true);

        if (!specifyHome) {
            builder.setTeamsWithYearlyRotation(school1, school2, scheduleService.getYear());
        } else {
            builder.setAwayTeam(school1).setHomeTeam(school2);
        }

        Game game = builder.build();
        scheduleService.addGame(game);
    }

    public void scheduleXDivGamesByIndex(School school, List<School> div1, List<School> div2, int index,
            int[] opponentIndices)
            throws Exception {
        // Schedule games against the determined opponents
        for (int opponentIndex : opponentIndices) {
            School opponent = div2.get(opponentIndex);
            if (school.getxDivRival() != null && school.getxDivRival().equals(opponent)) {
                if (opponentIndex == div2.size() - 1) {
                    opponentIndex = 0;
                } else {
                    opponentIndex++;
                }
                opponent = div2.get(opponentIndex);
            }
            if (scheduleService.isOpponentForSchool(school, opponent)) {
                if (opponentIndex == div2.size() - 1) {
                    opponentIndex = 0;
                } else {
                    opponentIndex++;
                }
                opponent = div2.get(opponentIndex);
            }
            int week = scheduleService.findConfGameWeek(school, opponent);
            boolean isHomeGame = opponentIndex % 2 == 0; // Alternate home and away games
            if (index % 2 == 0) {
                isHomeGame = !isHomeGame;
            }
            addYearlySeriesHelper(isHomeGame ? opponent : school, isHomeGame ? school : opponent, week, true);
        }
    }

    public List<School> rotateDivByYear(List<School> div, int year, int divisor) {
        int rotationAmount = year % divisor;
        Collections.rotate(div, rotationAmount);
        return div;
    }

}
