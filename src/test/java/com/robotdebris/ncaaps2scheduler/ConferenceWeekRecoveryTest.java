package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class ConferenceWeekRecoveryTest {
    @Autowired GameRepository games;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest
    @CsvSource({"false,13", "true,14"})
    void relocatesEitherTeamsGameWithoutLeavingConferenceWindow(boolean blockFirstMove, int expectedWeek) {
        games.removeAll();
        games.setYear(2004);
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        var conference = ConferenceSchedulerTest.setupConference(6, 0, false);
        conference.setConfGamesStartWeek(13);
        List<School> schools = conference.getSchools();
        School a = schools.get(0), b = schools.get(1), c = schools.get(2), d = schools.get(3), e = schools.get(4);

        // Small partial schedules isolate relocation; this does not ask the
        // full-season generator to fit a round robin into two weeks.
        addConferenceGame(a, c, 13);
        addConferenceGame(b, d, 14);
        if (blockFirstMove) addConferenceGame(c, e, 14);
        List<Game> original = List.copyOf(games.findAll());

        // A is free only in 14; B only in 13. Either move A-C to 14,
        // or (when C is busy there) move B-D to 13 to make room for A-B.
        int week = scheduleService.findConfGameWeek(a, b);
        assertEquals(expectedWeek, week);
        addConferenceGame(a, b, week);
        assertEquals(original.size() + 1, games.findAll().size());
        for (Game old : original) {
            Game retained = games.findGameByTeams(old.getHomeTeam(), old.getAwayTeam()).orElseThrow();
            assertEquals(old.getHomeTeam(), retained.getHomeTeam(), "Relocation must preserve the host");
            assertEquals(old.getAwayTeam(), retained.getAwayTeam(), "Relocation must preserve the visitor");
        }
        Set<String> occupied = new HashSet<>();
        for (Game game : games.findAll()) {
            assertTrue(game.isConferenceGame());
            assertTrue(game.getWeek() >= 13 && game.getWeek() < 15);
            assertTrue(occupied.add(game.getHomeTeam().getTgid() + ":" + game.getWeek()));
            assertTrue(occupied.add(game.getAwayTeam().getTgid() + ":" + game.getWeek()));
        }
    }

    private void addConferenceGame(School away, School home, int week) {
        scheduleService.addGame(new GameBuilder().setAwayTeam(away).setHomeTeam(home)
                .setWeek(week).setConferenceGame(true).build());
    }
}
