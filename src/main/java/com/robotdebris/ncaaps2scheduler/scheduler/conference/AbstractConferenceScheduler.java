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
    protected ScheduleService scheduleService;

    @Autowired
    public void initializeSchedulingServices(GameRepository gameRepository, ScheduleService scheduleService) {
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
        scheduleRoundRobinConfGames(schoolList, false);
    }

    void scheduleRoundRobinConfGames(List<School> schoolList, boolean reverseVenues) {
        boolean isEvenYear = gameRepository.getYear() % 2 == 0;
        for (School school : schoolList) {
            List<School> opponentsToSchedule = schoolList.stream()
                    .filter(opponent -> !school.equals(opponent)
                            && !scheduleService.isOpponentForSchool(school, opponent))
                    .toList();
            int j = 0;
            for (School opponent : opponentsToSchedule) {
                boolean isHomeGame = isEvenYear ? j % 2 == 0 : j % 2 == 1;
                if (reverseVenues) isHomeGame = !isHomeGame;
                scheduleConferenceGame(school, opponent, isHomeGame);
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

    /** Place a selected matchup, keeping week selection independent of its host. */
    void scheduleConferenceGame(School school, School opponent, boolean schoolIsHome) {
        int week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(schoolIsHome ? opponent : school,
                schoolIsHome ? school : opponent, week, true);
    }

    /** One offset pairs every school in each equally sized division exactly once. */
    void scheduleCrossDivisionMatching(List<School> firstDivision, List<School> secondDivision,
            int offset, boolean firstDivisionIsHome) {
        for (int index = 0; index < firstDivision.size(); index++) {
            School school = firstDivision.get(index);
            School opponent = secondDivision.get((index + offset) % secondDivision.size());
            scheduleConferenceGame(school, opponent, firstDivisionIsHome);
        }
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

    public List<School> rotateDivByYear(List<School> div, int year, int divisor) {
        int rotationAmount = year % divisor;
        Collections.rotate(div, rotationAmount);
        return div;
    }

}
