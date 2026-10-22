package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

/** Conference-only placement: fresh calendars with ample weeks, reproducible choices. */
@SpringBootTest
class ConferenceWeekPlacementTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest(name = "{0}, year={1}, start-week={2}, choice={3}")
    @MethodSource("placementCases")
    void weekChoicesPreserveCompleteConferenceSchedule(Format format, int year,
            int startWeek, Choice choice) throws Exception {
        Conference conference = ConferenceSchedulerTest.setupConference(
                format.teams(), format.configuredGames(), format.protectedRivals());
        conference.setConfGamesStartWeek(startWeek);
        String context = format + ", year=" + year + ", start-week=" + startWeek;

        // Compare full-season opponents AND hosts, not dates: choosing a different
        // available week must not alter the conference rotation being scheduled.
        generate(conference, year, Choice.EARLIEST, context + ", reference=EARLIEST");
        Set<Fixture> reference = fixtures();
        int expectedGames = format.configuredGames() == 0 ? format.teams() - 1 : format.configuredGames();
        assertEquals(format.teams() * expectedGames / 2, reference.size(), "Incomplete reference: " + context);
        // Reference metadata is not an oracle for correctness. Validate each
        // choice on its own, including EARLIEST as its own matrix case.
        generate(conference, year, choice, context + ", choice=" + choice);
        Set<Fixture> actual = fixtures();
        assertAll(context + ", choice=" + choice,
                () -> assertEquals(reference, actual, "Week choices changed opponents or venues"),
                () -> checkSchedule(conference, format, context + ", choice=" + choice));
    }

    private Set<Fixture> fixtures() {
        Set<Fixture> result = new HashSet<>();
        for (Game game : games.findAll()) result.add(new Fixture(game.getHomeTeam().getTgid(), game.getAwayTeam().getTgid()));
        return result;
    }

    private void generate(Conference conference, int year,
            Choice choice, String context) throws Exception {
        games.removeAll();
        games.setYear(year);
        Random random = new Random(choice.seed);
        doAnswer(call -> {
            List<Integer> available = call.getArgument(0);
            assertFalse(available.isEmpty(), "No candidates passed to week selector: " + context);
            // Sort a copy to keep the seed independent of the candidate list's order.
            List<Integer> ordered = available.stream().sorted().toList();
            return switch (choice) {
                case EARLIEST -> ordered.getFirst();
                case LATEST -> ordered.getLast();
                default -> ordered.get(random.nextInt(ordered.size()));
            };
        }).when(scheduleService).randomIntFromList(anyList());

        assertDoesNotThrow(() -> factory.getScheduler(conference)
                .generateConferenceSchedule(conference, games), "Generation failed: " + context);
    }

    private void checkSchedule(Conference conference, Format format, String context) {
        int expectedGames = format.configuredGames() == 0 ? format.teams() - 1 : format.configuredGames();
        List<Executable> checks = new ArrayList<>();
        checks.add(() -> assertEquals(format.teams() * expectedGames / 2, games.findAll().size(),
                "Incomplete conference season: " + context));
        Set<Pair> pairs = new HashSet<>();
        Set<TeamWeek> occupied = new HashSet<>();
        for (Game game : games.findAll()) {
            School home = game.getHomeTeam(), away = game.getAwayTeam();
            String detail = context + ": " + away + " at " + home + ", week=" + game.getWeek();
            checks.add(() -> assertNotEquals(home, away, "Self-game: " + detail));
            checks.add(() -> assertTrue(conference.getSchools().contains(home) && conference.getSchools().contains(away),
                    "Opponent outside conference: " + detail));
            checks.add(() -> assertTrue(game.isConferenceGame(), "Conference classification lost: " + detail));
            checks.add(() -> assertTrue(game.getWeek() >= conference.getConfGamesStartWeek() && game.getWeek() < 15,
                    "Outside conference window: " + detail));
            int first = Math.min(home.getTgid(), away.getTgid());
            int second = Math.max(home.getTgid(), away.getTgid());
            checks.add(() -> assertTrue(pairs.add(new Pair(first, second)), "Duplicate pairing: " + detail));
            checks.add(() -> assertTrue(occupied.add(new TeamWeek(home.getTgid(), game.getWeek())), "Home double-booked: " + detail));
            checks.add(() -> assertTrue(occupied.add(new TeamWeek(away.getTgid(), game.getWeek())), "Away double-booked: " + detail));
        }
        for (School school : conference.getSchools()) {
            List<Game> schedule = games.findGamesByTeam(school);
            checks.add(() -> assertEquals(expectedGames, schedule.size(), "Wrong game count for " + school + ": " + context));
            long homeCount = schedule.stream().filter(g -> g.getHomeTeam().equals(school)).count();
            checks.add(() -> assertTrue(homeCount >= expectedGames / 2 && homeCount <= (expectedGames + 1) / 2,
                    "Home/away imbalance for " + school + ": " + context));
            Set<School> opponents = new HashSet<>();
            for (Game game : schedule) opponents.add(game.getHomeTeam().equals(school)
                    ? game.getAwayTeam() : game.getHomeTeam());
            List<School> required = format.configuredGames() == 0 ? conference.getSchools()
                    : conference.getDivisions().stream().filter(d -> d.getSchools().contains(school))
                            .findFirst().orElseThrow().getSchools();
            for (School opponent : required) if (!opponent.equals(school))
                checks.add(() -> assertTrue(opponents.contains(opponent), "Missing required opponent " + school + " vs " + opponent
                        + ": " + context));
            if (format.protectedRivals()) checks.add(() -> assertTrue(opponents.contains(school.getxDivRival()),
                    "Missing protected rival for " + school + ": " + context));
        }
        assertAll(context, checks);
    }

    private static Stream<Arguments> placementCases() {
        List<Format> formats = new ArrayList<>();
        for (int teams : new int[]{8, 9, 10, 11}) formats.add(new Format(teams, 0, false));
        for (int teams : new int[]{12, 14}) for (int count : new int[]{8, 9})
            for (boolean rivals : new boolean[]{false, true}) formats.add(new Format(teams, count, rivals));
        List<Arguments> cases = new ArrayList<>();
        for (Format format : formats) for (int year : new int[]{2004, 2005, 2020})
            for (int startWeek : new int[]{0, 3}) for (Choice choice : Choice.values())
                cases.add(Arguments.of(format, year, startWeek, choice));
        return cases.stream();
    }

    private enum Choice {
        EARLIEST(0), LATEST(0), SEED_0(0), SEED_1(1), SEED_7(7), SEED_42(42), SEED_2026(2026);
        final long seed;
        Choice(long seed) { this.seed = seed; }
    }

    private record Format(int teams, int configuredGames, boolean protectedRivals) { }
    private record Fixture(int home, int away) { }
    private record Pair(int first, int second) { }
    private record TeamWeek(int team, int week) { }
}
