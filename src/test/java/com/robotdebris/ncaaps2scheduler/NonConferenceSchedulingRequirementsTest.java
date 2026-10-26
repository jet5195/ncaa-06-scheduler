package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.*;
import com.robotdebris.ncaaps2scheduler.repository.ExcelGameRepository;
import com.robotdebris.ncaaps2scheduler.repository.ExcelSchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Controlled calendars test requirements without prescribing random opponents or venues. */
@Timeout(10)
class NonConferenceSchedulingRequirementsTest {
    private final ExcelGameRepository games = new ExcelGameRepository();
    private final ExcelSchoolRepository schools = new ExcelSchoolRepository();
    private final ScheduleService service = new ScheduleService(games, schools);
    private int nextId = 1000;

    NonConferenceSchedulingRequirementsTest() {
        ReflectionTestUtils.setField(service, "schoolService", new SchoolService(schools, games, null, null));
        games.setYear(2004);
    }

    @Test
    void fillsBothFbsTeamsToTwelveWithoutRepeatingExistingOpponents() {
        School a = school(1, "A", NCAADivision.FBS), b = school(2, "B", NCAADivision.FBS);
        schools.saveAll(List.of(a, b));
        fill(a, 11);
        fill(b, 11);
        service.addRandomNonConfGames();
        assertEquals(12, games.findGamesByTeam(a).size());
        assertEquals(12, games.findGamesByTeam(b).size());
        assertFalse(games.findGameByTeams(a, b).orElseThrow().isConferenceGame());
        assertValidCalendar(games.findAll());
    }

    @Test
    void fallsBackToFcsWhenNoFbsOpponentNeedsGames() {
        School a = school(1, "A", NCAADivision.FBS), fcs = school(2, "FCS", NCAADivision.FCS);
        schools.saveAll(List.of(a, fcs));
        fill(a, 11);
        service.addRandomNonConfGames();
        assertEquals(12, games.findGamesByTeam(a).size());
        Game added = games.findGameByTeams(a, fcs).orElseThrow();
        assertFalse(added.isConferenceGame());
        assertFalse(a.isInConference(fcs));
        assertValidCalendar(games.findAll());
    }

    @Test
    void doesNotFillAgainstAnotherTeamInTheSameConference() {
        School a = school(1, "A", NCAADivision.FBS), b = school(2, "B", NCAADivision.FBS);
        b.setConference(a.getConference());
        School fcs = school(3, "FCS", NCAADivision.FCS);
        schools.saveAll(List.of(a, b, fcs));
        fill(a, 11);
        fill(b, 11);
        service.addRandomNonConfGames();
        assertTrue(games.findGameByTeams(a, b).isEmpty());
        assertEquals(12, games.findGamesByTeam(a).size());
        assertEquals(12, games.findGamesByTeam(b).size());
        assertValidCalendar(games.findAll());
    }

    @Test
    void leavesAnAlreadyFullFbsTeamUntouched() {
        School a = school(1, "A", NCAADivision.FBS), full = school(2, "Full", NCAADivision.FBS);
        School fcs = school(3, "FCS", NCAADivision.FCS);
        schools.saveAll(List.of(a, full, fcs));
        fill(a, 11);
        fill(full, 12);
        List<Game> original = List.copyOf(games.findGamesByTeam(full));
        service.addRandomNonConfGames();
        assertEquals(original, games.findGamesByTeam(full));
        assertEquals(12, games.findGamesByTeam(a).size());
        assertTrue(games.findGameByTeams(a, full).isEmpty());
        assertValidCalendar(games.findAll());
    }

    @Test
    void normalAutoFillPreservesConferenceGameAndItsMetadata() {
        School a = school(1, "A", NCAADivision.FBS), mate = school(2, "Mate", NCAADivision.FBS);
        mate.setConference(a.getConference());
        School fcs = school(3, "FCS", NCAADivision.FCS);
        schools.saveAll(List.of(a, fcs));
        Game protectedGame = new GameBuilder().setHomeTeam(a).setAwayTeam(mate)
                .setWeek(10).setConferenceGame(true).setDay(DayOfWeek.FRIDAY).build();
        protectedGame.setTime(930);
        protectedGame.setGameNumber(17);
        games.saveGame(protectedGame);
        fill(a, 10);
        service.autoAddGames(false);
        assertSame(protectedGame, games.findGameByTeams(a, mate).orElseThrow());
        assertEquals(10, protectedGame.getWeek());
        assertEquals(a, protectedGame.getHomeTeam());
        assertTrue(protectedGame.isConferenceGame());
        assertEquals(DayOfWeek.FRIDAY, protectedGame.getDay());
        assertEquals(930, protectedGame.getTime());
        assertEquals(17, protectedGame.getGameNumber());
        assertEquals(12, games.findGamesByTeam(a).size());
        assertValidCalendar(games.findAll());
    }

    @Test
    void trimmingAnOverfullSeasonProtectsConferenceGames() {
        School a = school(1, "A", NCAADivision.FBS), mate = school(2, "Mate", NCAADivision.FBS);
        mate.setConference(a.getConference());
        schools.saveAll(List.of(a));
        Game protectedGame = new GameBuilder().setHomeTeam(a).setAwayTeam(mate)
                .setWeek(12).setConferenceGame(true).build();
        games.saveGame(protectedGame);
        fill(a, 12);
        service.fixSchedule();
        assertEquals(12, games.findGamesByTeam(a).size());
        assertSame(protectedGame, games.findGameByTeams(a, mate).orElseThrow());
        assertEquals(12, protectedGame.getWeek());
        assertTrue(protectedGame.isConferenceGame());
        assertValidCalendar(games.findAll());
    }

    @ParameterizedTest
    @EnumSource(Exhaustion.class)
    void exhaustedOpponentOptionsLeaveExistingGamesIntact(Exhaustion exhaustion) {
        School a = school(1, "A", NCAADivision.FBS), fcs = school(2, "FCS", NCAADivision.FCS);
        fill(a, 11);
        if (exhaustion == Exhaustion.ALREADY_PLAYED) {
            Game old = games.findGamesByTeam(a).getFirst();
            old.setAwayTeam(fcs);
        }
        if (exhaustion == Exhaustion.NO_SHARED_WEEK) fill(fcs, 15);
        schools.saveAll(exhaustion == Exhaustion.NO_FCS_TEAMS ? List.of(a) : List.of(a, fcs));
        List<Game> before = List.copyOf(games.findAll());
        // Best effort: an impossible fill should leave a short season rather than crash.
        assertDoesNotThrow(service::addRandomNonConfGames);
        assertEquals(before, games.findAll());
        assertEquals(11, games.findGamesByTeam(a).size());
        assertValidCalendar(games.findAll());
    }

    @Test
    void anIndependentSchoolCannotBeItsOwnNonConferenceOpponent() {
        School independent = school(1, "Independent", NCAADivision.FBS);
        independent.getConference().setName("FBS Independents");
        assertFalse(service.isEligibleNonConfMatchup(independent, independent));
    }

    @Test
    void continuesFillingOtherSchoolsWhenOneHasNoEligibleOpponent() {
        School blocked = school(1, "Blocked", NCAADivision.FBS);
        School fillable = school(2, "Fillable", NCAADivision.FBS);
        fillable.setConference(blocked.getConference());
        School fcs = school(3, "FCS", NCAADivision.FCS);
        fill(blocked, 11);
        games.findGamesByTeam(blocked).getFirst().setAwayTeam(fcs);
        fill(fillable, 11);
        schools.saveAll(List.of(blocked, fillable, fcs));
        List<Game> originalBlocked = List.copyOf(games.findGamesByTeam(blocked));

        assertDoesNotThrow(service::addRandomNonConfGames);

        assertEquals(originalBlocked, games.findGamesByTeam(blocked));
        assertEquals(12, games.findGamesByTeam(fillable).size());
        assertTrue(games.findGameByTeams(fillable, fcs).isPresent());
        assertValidCalendar(games.findAll());
    }

    @Test
    void retainsGamesAddedBeforeOpponentOptionsRunOut() {
        School a = school(1, "A", NCAADivision.FBS), fcs = school(2, "FCS", NCAADivision.FCS);
        fill(a, 10);
        List<Game> original = List.copyOf(games.findAll());
        schools.saveAll(List.of(a, fcs));

        assertDoesNotThrow(service::addRandomNonConfGames);

        assertEquals(11, games.findGamesByTeam(a).size());
        assertTrue(games.findAll().containsAll(original));
        assertTrue(games.findGameByTeams(a, fcs).isPresent());
        assertValidCalendar(games.findAll());
    }

    private School school(int id, String name, NCAADivision classification) {
        School school = new School();
        school.setTgid(id);
        school.setName(name);
        school.setRivals(List.of());
        school.setConference(new Conference(id, name + " Conference", name, name,
                classification, false, 0, 0, ""));
        return school;
    }

    private void fill(School school, int count) {
        for (int week = 0; week < count; week++) {
            School opponent = school(nextId++, "Existing opponent " + nextId, NCAADivision.FCS);
            games.saveGame(new GameBuilder().setHomeTeam(school).setAwayTeam(opponent).setWeek(week).build());
        }
    }

    static void assertValidCalendar(List<Game> calendar) {
        Set<String> pairs = new HashSet<>(), occupied = new HashSet<>();
        for (Game game : calendar) {
            int home = game.getHomeTeam().getTgid(), away = game.getAwayTeam().getTgid();
            assertNotEquals(home, away, "Self-game: " + game);
            assertTrue(game.getWeek() >= 0 && game.getWeek() < 15, "Outside regular season: " + game);
            assertTrue(pairs.add(Math.min(home, away) + ":" + Math.max(home, away)), "Repeated opponents: " + game);
            assertTrue(occupied.add(home + ":" + game.getWeek()), "Home double-booked: " + game);
            assertTrue(occupied.add(away + ":" + game.getWeek()), "Away double-booked: " + game);
        }
    }

    private enum Exhaustion { NO_FCS_TEAMS, ALREADY_PLAYED, NO_SHARED_WEEK }
}
