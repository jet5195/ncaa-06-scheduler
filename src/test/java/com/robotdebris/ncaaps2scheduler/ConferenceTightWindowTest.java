package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

/** Tight calendars with an independently constructed feasible schedule for every opponent graph. */
@SpringBootTest
class ConferenceTightWindowTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest(name = "{0}, year={1}, extra-weeks={2}, choice={3}")
    @MethodSource("cases")
    void placesTheSameGamesInsideAKnownFeasibleWindow(Format format, int year, int extraWeeks, Choice choice) {
        verifyWindow(format, year, extraWeeks, choice);
    }

    // Kept as unmet requirements, not assertions that failure is desirable.
    @Tag("known-limitations")
    @ParameterizedTest(name = "{0}, year={1}, extra-weeks={2}, choice={3}")
    @MethodSource("knownLimitations")
    void knownMinimumWindowLimitation(Format format, int year, int extraWeeks, Choice choice) {
        verifyWindow(format, year, extraWeeks, choice);
    }

    private void verifyWindow(Format format, int year, int extraWeeks, Choice choice) {
        Conference conference = ConferenceSchedulerTest.setupConference(format.teams(), format.configuredGames(), format.rivals());
        if (format.configuredGames() == 0) conference.setDivisions(new ArrayList<>());
        else conference.getDivisions().forEach(d -> d.getSchools().forEach(s -> s.setDivision(d)));
        String context = format + ", year=" + year + ", extra-weeks=" + extraWeeks + ", choice=" + choice;

        // An ample calendar supplies only opponents and hosts. It is not the
        // feasibility oracle for the tight calendar tested below.
        conference.setConfGamesStartWeek(0);
        generate(conference, year, Choice.EARLIEST, "Ample reference: " + context);
        List<PlayedGame> reference = snapshot();
        int count = format.configuredGames() == 0 ? format.teams() - 1 : format.configuredGames();
        int minimumWeeks = format.configuredGames() == 0 ? format.teams() - 1 + format.teams() % 2 : count;
        int startWeek = 15 - minimumWeeks - extraWeeks;
        conference.setConfGamesStartWeek(startWeek);

        // The witness uses circle-method divisional rounds and bipartite
        // matching, independently of production's greedy week placement.
        List<PlayedGame> witness = feasibleCalendar(conference, reference, startWeek);
        checkCalendar(conference, format, witness, "Feasibility witness: " + context);
        assertEquals(fixtures(reference), fixtures(witness), "Witness changed opponents or hosts: " + context);

        generate(conference, year, choice, "Tight placement, start-week=" + startWeek + ": " + context);
        List<PlayedGame> actual = snapshot();
        checkCalendar(conference, format, actual, "Tight placement: " + context);
        assertEquals(fixtures(reference), fixtures(actual), "Week placement changed opponents or hosts: " + context);
    }

    private void generate(Conference conference, int year, Choice choice, String context) {
        games.removeAll();
        games.setYear(year);
        Random random = new Random(choice.seed);
        doAnswer(call -> {
            List<Integer> available = call.getArgument(0);
            assertFalse(available.isEmpty(), "No candidates: " + context);
            var ordered = available.stream().sorted().toList();
            return switch (choice) {
                case EARLIEST -> ordered.getFirst();
                case LATEST -> ordered.getLast();
                default -> ordered.get(random.nextInt(ordered.size()));
            };
        }).when(scheduleService).randomIntFromList(anyList());
        assertDoesNotThrow(() -> factory.getScheduler(conference).generateConferenceSchedule(conference, games), context);
    }

    private List<PlayedGame> snapshot() {
        return games.findAll().stream().map(g -> new PlayedGame(g.getHomeTeam().getTgid(),
                g.getAwayTeam().getTgid(), g.getWeek(), g.isConferenceGame())).toList();
    }

    private List<PlayedGame> feasibleCalendar(Conference conference, List<PlayedGame> reference, int startWeek) {
        Map<Pair, PlayedGame> opponents = new HashMap<>();
        reference.forEach(g -> assertNull(opponents.put(pair(g.home(), g.away()), g), "Duplicate reference matchup"));
        List<Integer> all = conference.getSchools().stream().map(School::getTgid).toList();
        List<PlayedGame> result = new ArrayList<>();
        if (all.size() <= 11) {
            List<List<PlayedGame>> rounds = roundRobinRounds(all, opponents);
            for (int i = 0; i < rounds.size(); i++) addRound(result, rounds.get(i), startWeek + i);
            return result;
        }
        List<Integer> first = conference.getDivisions().get(0).getSchools().stream().map(School::getTgid).toList();
        List<Integer> second = conference.getDivisions().get(1).getSchools().stream().map(School::getTgid).toList();
        List<List<PlayedGame>> firstRounds = roundRobinRounds(first, opponents);
        List<List<PlayedGame>> secondRounds = roundRobinRounds(second, opponents);
        List<List<PlayedGame>> crossRounds = crossDivisionMatchings(first, second, opponents);
        int week = startWeek;
        if (first.size() % 2 == 0) {
            for (int i = 0; i < firstRounds.size(); i++) {
                addRound(result, firstRounds.get(i), week);
                addRound(result, secondRounds.get(i), week++);
            }
        } else {
            // Seven-team divisions each need seven rounds with one bye per
            // school. Use one complete cross matching to pair their byes,
            // putting one cross game alongside the six divisional games.
            Map<Integer, List<PlayedGame>> secondByBye = new HashMap<>();
            secondRounds.forEach(r -> secondByBye.put(bye(second, r), r));
            Map<Integer, PlayedGame> byeGames = new HashMap<>();
            for (PlayedGame game : crossRounds.removeFirst()) {
                int firstTeam = first.contains(game.home()) ? game.home() : game.away();
                byeGames.put(firstTeam, game);
            }
            for (List<PlayedGame> round : firstRounds) {
                int firstBye = bye(first, round);
                PlayedGame crossGame = byeGames.get(firstBye);
                int secondBye = crossGame.home() == firstBye ? crossGame.away() : crossGame.home();
                addRound(result, round, week);
                addRound(result, secondByBye.get(secondBye), week);
                result.add(atWeek(crossGame, week++));
            }
        }
        for (List<PlayedGame> round : crossRounds) addRound(result, round, week++);
        return result;
    }

    private List<List<PlayedGame>> roundRobinRounds(List<Integer> teams, Map<Pair, PlayedGame> opponents) {
        List<Integer> ring = new ArrayList<>(teams);
        if (ring.size() % 2 != 0) ring.add(null);
        List<List<PlayedGame>> rounds = new ArrayList<>();
        for (int round = 0; round < ring.size() - 1; round++) {
            List<PlayedGame> matches = new ArrayList<>();
            for (int i = 0; i < ring.size() / 2; i++) {
                Integer a = ring.get(i), b = ring.get(ring.size() - i - 1);
                if (a != null && b != null) {
                    PlayedGame game = opponents.get(pair(a, b));
                    assertNotNull(game, "Required round-robin pairing missing: " + a + " vs " + b);
                    matches.add(game);
                }
            }
            rounds.add(matches);
            ring.add(1, ring.removeLast());
        }
        return rounds;
    }

    private List<List<PlayedGame>> crossDivisionMatchings(List<Integer> first, List<Integer> second,
            Map<Pair, PlayedGame> opponents) {
        Map<Integer, List<Integer>> remaining = new HashMap<>();
        for (int a : first) remaining.put(a, new ArrayList<>(second.stream()
                .filter(b -> opponents.containsKey(pair(a, b))).toList()));
        int degree = remaining.get(first.getFirst()).size();
        assertTrue(degree > 0, "Reference must include cross-division games");
        first.forEach(a -> assertEquals(degree, remaining.get(a).size()));
        List<List<PlayedGame>> rounds = new ArrayList<>();
        for (int round = 0; round < degree; round++) {
            Map<Integer, Integer> matched = new HashMap<>();
            for (int a : first) assertTrue(augment(a, remaining, matched, new HashSet<>()),
                    "Could not construct a complete cross-division matching");
            List<PlayedGame> games = new ArrayList<>();
            for (int b : second) {
                int a = matched.get(b);
                games.add(opponents.get(pair(a, b)));
                assertTrue(remaining.get(a).remove(Integer.valueOf(b)));
            }
            rounds.add(games);
        }
        assertTrue(remaining.values().stream().allMatch(List::isEmpty));
        return rounds;
    }

    private boolean augment(int first, Map<Integer, List<Integer>> edges, Map<Integer, Integer> matched,
            Set<Integer> visited) {
        for (int second : edges.get(first)) if (visited.add(second)) {
            Integer previous = matched.get(second);
            if (previous == null || augment(previous, edges, matched, visited)) {
                matched.put(second, first);
                return true;
            }
        }
        return false;
    }

    private int bye(List<Integer> teams, List<PlayedGame> round) {
        return teams.stream().filter(t -> round.stream().noneMatch(g -> g.home() == t || g.away() == t))
                .findFirst().orElseThrow();
    }

    private void addRound(List<PlayedGame> target, List<PlayedGame> round, int week) {
        round.forEach(g -> target.add(atWeek(g, week)));
    }

    private PlayedGame atWeek(PlayedGame game, int week) {
        return new PlayedGame(game.home(), game.away(), week, game.conference());
    }

    private Set<HostedPair> fixtures(List<PlayedGame> games) {
        Set<HostedPair> result = new HashSet<>();
        games.forEach(g -> assertTrue(result.add(new HostedPair(g.home(), g.away())), "Duplicate hosted pairing"));
        return result;
    }

    private void checkCalendar(Conference conference, Format format, List<PlayedGame> season, String context) {
        int count = format.configuredGames() == 0 ? format.teams() - 1 : format.configuredGames();
        assertEquals(format.teams() * count / 2, season.size(), "Complete season: " + context);
        Set<Integer> members = new HashSet<>();
        conference.getSchools().forEach(s -> members.add(s.getTgid()));
        Set<Pair> pairings = new HashSet<>();
        Set<TeamWeek> occupied = new HashSet<>();
        for (PlayedGame game : season) {
            String detail = context + ", " + game;
            assertNotEquals(game.home(), game.away(), "Self-game: " + detail);
            assertTrue(members.contains(game.home()) && members.contains(game.away()), "Outside conference: " + detail);
            assertTrue(game.conference(), "Conference flag lost: " + detail);
            assertTrue(game.week() >= conference.getConfGamesStartWeek() && game.week() < 15, "Outside window: " + detail);
            assertTrue(pairings.add(pair(game.home(), game.away())), "Duplicate pairing: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.home(), game.week())), "Home double-booked: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.away(), game.week())), "Away double-booked: " + detail);
        }
        for (School school : conference.getSchools()) {
            int id = school.getTgid();
            List<PlayedGame> schedule = season.stream().filter(g -> g.home() == id || g.away() == id).toList();
            assertEquals(count, schedule.size(), "Game count for " + school + ": " + context);
            long home = schedule.stream().filter(g -> g.home() == id).count();
            assertTrue(home >= count / 2 && home <= (count + 1) / 2, "Home balance for " + school + ": " + context);
            Set<Integer> opponents = new HashSet<>();
            schedule.forEach(g -> opponents.add(g.home() == id ? g.away() : g.home()));
            List<School> required = format.configuredGames() == 0 ? conference.getSchools() : school.getDivision().getSchools();
            for (School opponent : required) if (!opponent.equals(school))
                assertTrue(opponents.contains(opponent.getTgid()), "Missing required opponent: " + context);
            if (format.rivals()) assertTrue(opponents.contains(school.getxDivRival().getTgid()), "Missing protected rival: " + context);
        }
    }

    private Pair pair(int a, int b) { return new Pair(Math.min(a, b), Math.max(a, b)); }

    private static Stream<Arguments> cases() {
        Set<WindowCase> deferred = knownCases();
        List<Format> formats = new ArrayList<>();
        for (int teams : new int[]{8, 9, 10, 11}) formats.add(new Format(teams, 0, false));
        for (int teams : new int[]{12, 14}) for (int count : new int[]{8, 9})
            for (boolean rivals : new boolean[]{false, true}) formats.add(new Format(teams, count, rivals));
        List<Arguments> cases = new ArrayList<>();
        for (Format format : formats) for (int year : new int[]{2004, 2005, 2020})
            for (int extra : new int[]{0, 1}) for (Choice choice : Choice.values())
                if (!deferred.contains(new WindowCase(format, year, extra, choice)))
                    cases.add(Arguments.of(format, year, extra, choice));
        return cases.stream();
    }

    private static Stream<Arguments> knownLimitations() {
        return knownCases().stream().map(c -> Arguments.of(c.format(), c.year(), c.extraWeeks(), c.choice()));
    }

    private static Set<WindowCase> knownCases() {
        var resource = ConferenceTightWindowTest.class.getResourceAsStream("/conference-tight-window-known-limitations.csv");
        assertNotNull(resource, "Known-limitations case list missing");
        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            Set<WindowCase> result = new java.util.LinkedHashSet<>();
            reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).forEach(line -> {
                String[] fields = line.split(",");
                Format format = new Format(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                        Boolean.parseBoolean(fields[2]));
                assertTrue(result.add(new WindowCase(format, Integer.parseInt(fields[3]),
                        Integer.parseInt(fields[4]), Choice.valueOf(fields[5]))), "Duplicate known limitation: " + line);
            });
            assertEquals(42, result.size(), "Review the documented quarantine when changing its case list");
            return result;
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private record WindowCase(Format format, int year, int extraWeeks, Choice choice) { }

    private enum Choice {
        EARLIEST(0), LATEST(0), SEED_42(42), SEED_2026(2026);
        final long seed;
        Choice(long seed) { this.seed = seed; }
    }
    private record Format(int teams, int configuredGames, boolean rivals) { }
    private record PlayedGame(int home, int away, int week, boolean conference) { }
    private record Pair(int first, int second) { }
    private record HostedPair(int home, int away) { }
    private record TeamWeek(int team, int week) { }
}
