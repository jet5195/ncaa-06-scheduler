package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class TwelveTeamEightGameHomeBalanceTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "true,true"})
    void eachTeamHasFourHomeGamesEveryYear(boolean protectedRivals, boolean reverseDivisionOrder) throws Exception {
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(12, 8, protectedRivals);
        // Pairing order must not depend on how the other division was imported.
        if (reverseDivisionOrder) Collections.reverse(conference.getDivisions().get(1).getSchools());
        for (int year = 1980; year < 2004; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            assertEquals(48, games.findAll().size());
            Set<String> teamWeeks = new HashSet<>();
            for (Game game : games.findAll()) {
                assertTrue(teamWeeks.add(game.getHomeTeam().getTgid() + ":" + game.getWeek()));
                assertTrue(teamWeeks.add(game.getAwayTeam().getTgid() + ":" + game.getWeek()));
            }
            for (School school : conference.getSchools()) {
                List<Game> schedule = games.findGamesByTeam(school);
                assertEquals(8, schedule.size());
                assertEquals(4, schedule.stream().filter(game -> game.getHomeTeam().equals(school)).count(),
                        school + " home games in " + year + ", protected rivals=" + protectedRivals);
                Set<School> opponents = new HashSet<>();
                for (Game game : schedule) {
                    assertTrue(opponents.add(game.getHomeTeam().equals(school) ? game.getAwayTeam() : game.getHomeTeam()));
                }
                if (protectedRivals) assertTrue(opponents.contains(school.getxDivRival()));
            }
        }
    }
}
