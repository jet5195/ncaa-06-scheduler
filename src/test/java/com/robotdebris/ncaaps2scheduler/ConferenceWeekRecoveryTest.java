package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.model.DayOfWeek;
import com.robotdebris.ncaaps2scheduler.model.GameResult;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    void resetCalendar() {
        games.removeAll();
        games.setYear(2004);
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
    }

    @ParameterizedTest
    @CsvSource({"false,13", "true,14"})
    void relocatesEitherTeamsGameWithoutLeavingConferenceWindow(boolean blockFirstMove, int expectedWeek) {
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

    @Test
    void rearrangesAChainWhenNeitherTeamCanMoveOneGame() {
        var conference = ConferenceSchedulerTest.setupConference(8, 0, false);
        conference.setConfGamesStartWeek(12);
        var schools = conference.getSchools();
        School a=schools.get(0), b=schools.get(1), c=schools.get(2), d=schools.get(3);
        School e=schools.get(4), f=schools.get(5), g=schools.get(6), h=schools.get(7);
        addConferenceGame(a,c,12);
        addConferenceGame(a,d,14);
        addConferenceGame(b,e,13);
        addConferenceGame(b,f,14);
        addConferenceGame(c,g,13);
        addConferenceGame(e,h,12);
        List<Game> original = List.copyOf(games.findAll());
        // Use nondefault metadata to detect rebuilding games with default fields.
        GameResult result = new GameResult(7,3,1);
        for (Game game : original) {
            game.setTime(930);
            game.setDay(DayOfWeek.FRIDAY);
            game.setGameResult(result);
            game.setGameNumber(17);
            game.setWeight(77);
            game.setUserGame(true);
        }
        assertTrue(scheduleService.findEmptyWeeks(a,b).isEmpty());
        // One valid solution swaps A-C and C-G between 12 and 13, then
        // places A-B in 12. No single existing game can make room on its own.
        int week = scheduleService.findConfGameWeek(a,b);
        addConferenceGame(a,b,week);
        assertEquals(7,games.findAll().size());
        for (Game old : original) {
            Game retained = games.findGameByTeams(old.getHomeTeam(),old.getAwayTeam()).orElseThrow();
            assertSame(old,retained,"Relocation should retain the existing game and all its metadata");
            assertEquals(930,retained.getTime());
            assertEquals(DayOfWeek.FRIDAY,retained.getDay());
            assertSame(result,retained.getGameResult());
            assertEquals(17,retained.getGameNumber());
            assertEquals(77,retained.getWeight());
            assertTrue(retained.isUserGame());
        }
        Set<String> occupied = new HashSet<>();
        for (Game game : games.findAll()) {
            assertTrue(game.isConferenceGame());
            assertTrue(game.getWeek() >= 12 && game.getWeek() < 15);
            assertTrue(occupied.add(game.getHomeTeam().getTgid()+":"+game.getWeek()));
            assertTrue(occupied.add(game.getAwayTeam().getTgid()+":"+game.getWeek()));
        }
    }

    @Test
    void impossiblePlacementLeavesExistingGamesUnchanged() {
        var conference = ConferenceSchedulerTest.setupConference(6,0,false);
        conference.setConfGamesStartWeek(13);
        var schools = conference.getSchools();
        School a=schools.get(0), b=schools.get(1), c=schools.get(2);
        addConferenceGame(a,c,13);
        addConferenceGame(b,c,14);
        // An unrelated movable game makes recovery explore a legal first move
        // that cannot solve the triangle; that speculative move must be undone.
        addConferenceGame(schools.get(3),schools.get(4),13);
        List<Game> original = List.copyOf(games.findAll());
        // Three mutually opposing teams require three weeks, but only two
        // are allowed. Recovery must not partly apply an unsuccessful swap.
        assertThrows(NoWeeksAvailableException.class,() -> scheduleService.findConfGameWeek(a,b));
        assertEquals(original,games.findAll());
        assertEquals(13,original.get(0).getWeek());
        assertEquals(14,original.get(1).getWeek());
        assertEquals(13,original.get(2).getWeek());
        assertTrue(original.stream().allMatch(Game::isConferenceGame));
    }

    private void addConferenceGame(School away, School home, int week) {
        scheduleService.addGame(new GameBuilder().setAwayTeam(away).setHomeTeam(home)
                .setWeek(week).setConferenceGame(true).build());
    }
}
