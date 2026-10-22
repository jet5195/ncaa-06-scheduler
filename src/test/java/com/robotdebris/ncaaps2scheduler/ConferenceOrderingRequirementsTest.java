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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.Collections;
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

/** Fixed shuffled alignments: ordering may change the schedule, but not its requirements. */
@SpringBootTest
class ConferenceOrderingRequirementsTest {
    private static final int SEASONS = 28;
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest(name = "{0}, layout-seed={1}, start={2}, weeks={3}, shuffled={4}")
    @MethodSource("cases")
    void shuffledAlignmentsPreserveSchedulingRequirements(Format format, long layoutSeed,
            int startYear, WeekChoice choice, boolean shuffled) {
        Conference conference = shuffledConference(format, layoutSeed, shuffled);
        long weekSeed = 20_000 + layoutSeed;
        Random weeks = new Random(weekSeed);
        doAnswer(call -> {
            List<Integer> available = call.getArgument(0);
            assertFalse(available.isEmpty(), "Week selector received no candidates");
            List<Integer> ordered = available.stream().sorted().toList();
            return choice == WeekChoice.EARLIEST ? ordered.getFirst() : ordered.get(weeks.nextInt(ordered.size()));
        }).when(scheduleService).randomIntFromList(anyList());

        Map<Pair, Integer> lastHost = new HashMap<>();
        Map<Integer, Long> previousHomeCounts = new HashMap<>();
        Map<HostedPair, Integer> rollingMeetings = new HashMap<>();
        List<List<PlayedGame>> seasons = new ArrayList<>();
        for (int year = startYear; year < startYear + SEASONS; year++) {
            games.removeAll();
            games.setYear(year);
            String context = format + ", layout-seed=" + layoutSeed + ", year=" + year
                    + ", weeks=" + choice + ", week-seed=" + weekSeed + ", shuffled=" + shuffled;
            assertDoesNotThrow(() -> factory.getScheduler(conference)
                    .generateConferenceSchedule(conference, games), "Generation failed: " + context);
            List<PlayedGame> season = games.findAll().stream().map(g -> new PlayedGame(
                    g.getHomeTeam().getTgid(), g.getAwayTeam().getTgid(), g.getWeek(), g.isConferenceGame())).toList();
            checkAnnualRequirements(conference, format, season, previousHomeCounts, context);
            for (PlayedGame game : season) {
                Pair pair = new Pair(Math.min(game.home(), game.away()), Math.max(game.home(), game.away()));
                Integer previous = lastHost.put(pair, game.home());
                if (previous != null) assertNotEquals(previous.intValue(), game.home(),
                        "Next meeting must reverse venue: " + pair + ", " + context);
            }
            seasons.add(season);
            addMeetings(rollingMeetings, season, 1);
            if (seasons.size() > format.window())
                addMeetings(rollingMeetings, seasons.get(seasons.size() - format.window() - 1), -1);
            if (seasons.size() >= format.window()) checkFrequency(conference, format, rollingMeetings, context);
        }
    }

    private Conference shuffledConference(Format format, long seed, boolean shuffled) {
        Conference conference = ConferenceSchedulerTest.setupConference(format.teams(), format.games(), false);
        conference.setName("Ordering test " + format + ", seed=" + seed + ", shuffled=" + shuffled);
        conference.setConfGamesStartWeek(3);
        if (!shuffled) {
            assertEquals(0, format.games(), "Canonical controls cover round-robin placement");
            conference.setDivisions(new ArrayList<>());
            return conference;
        }
        if (format.games() == 0) {
            conference.setDivisions(new ArrayList<>());
        } else {
            List<School> first = conference.getDivisions().get(0).getSchools();
            List<School> second = conference.getDivisions().get(1).getSchools();
            if (format.rivals()) {
                // Assign a reciprocal bijection independently of either division's
                // eventual list order, rather than pairing the shuffled indexes.
                List<School> partners = new ArrayList<>(second);
                Collections.shuffle(partners, new Random(10_000 + seed));
                assertNotEquals(second, partners, "Fixture must vary the original protected pairings");
                for (int i = 0; i < first.size(); i++) {
                    first.get(i).setxDivRival(partners.get(i));
                    partners.get(i).setxDivRival(first.get(i));
                }
            }
            shuffleAndCheck(first, seed);
            shuffleAndCheck(second, 1_000 + seed);
            conference.getDivisions().forEach(d -> d.getSchools().forEach(s -> s.setDivision(d)));
            // Exercise which division is processed first as well as school order.
            if (seed % 2 != 0) Collections.reverse(conference.getDivisions());
            if (format.rivals()) {
                Set<School> partners = new HashSet<>();
                for (School school : first) {
                    assertTrue(second.contains(school.getxDivRival()));
                    assertEquals(school, school.getxDivRival().getxDivRival());
                    assertTrue(partners.add(school.getxDivRival()), "Fixture must be one-to-one");
                }
                assertTrue(java.util.stream.IntStream.range(0, first.size())
                        .anyMatch(i -> !first.get(i).getxDivRival().equals(second.get(i))),
                        "Protected fixture must not depend on corresponding list indexes");
            }
        }
        shuffleAndCheck(conference.getSchools(), 2_000 + seed);
        return conference;
    }

    private void shuffleAndCheck(List<School> schools, long seed) {
        List<School> original = List.copyOf(schools);
        Collections.shuffle(schools, new Random(seed));
        assertNotEquals(original, schools, "Fixture must actually change school order, seed=" + seed);
        assertEquals(new HashSet<>(original), new HashSet<>(schools), "Ordering must preserve membership");
    }

    private void checkAnnualRequirements(Conference conference, Format format, List<PlayedGame> season,
            Map<Integer, Long> previousHomeCounts, String context) {
        int count = format.games() == 0 ? format.teams() - 1 : format.games();
        assertEquals(format.teams() * count / 2, season.size(), "Complete season: " + context);
        Set<Integer> members = new HashSet<>();
        conference.getSchools().forEach(s -> members.add(s.getTgid()));
        Set<Pair> pairs = new HashSet<>();
        Set<TeamWeek> occupied = new HashSet<>();
        for (PlayedGame game : season) {
            String detail = context + ", " + game;
            assertNotEquals(game.home(), game.away(), "Self-game: " + detail);
            assertTrue(members.contains(game.home()) && members.contains(game.away()), "Outside conference: " + detail);
            assertTrue(game.conference(), "Conference flag lost: " + detail);
            assertTrue(game.week() >= 3 && game.week() < 15, "Outside conference window: " + detail);
            assertTrue(pairs.add(new Pair(Math.min(game.home(), game.away()), Math.max(game.home(), game.away()))),
                    "Duplicate matchup: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.home(), game.week())), "Home double-booked: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.away(), game.week())), "Away double-booked: " + detail);
        }
        for (School school : conference.getSchools()) {
            int id = school.getTgid();
            List<PlayedGame> schedule = season.stream().filter(g -> g.home() == id || g.away() == id).toList();
            assertEquals(count, schedule.size(), school + ": " + context);
            Set<Integer> opponents = new HashSet<>();
            schedule.forEach(g -> opponents.add(g.home() == id ? g.away() : g.home()));
            List<School> required = format.games() == 0 ? conference.getSchools() : school.getDivision().getSchools();
            for (School opponent : required) if (!opponent.equals(school))
                assertTrue(opponents.contains(opponent.getTgid()), "Required opponent missing: " + school
                        + " vs " + opponent + ", " + context);
            if (format.rivals()) assertTrue(opponents.contains(school.getxDivRival().getTgid()),
                    "Protected rival missing: " + school + ", " + context);
            long home = schedule.stream().filter(g -> g.home() == id).count();
            assertTrue(home >= count / 2 && home <= (count + 1) / 2, "Annual balance: " + school + ", " + context);
            Long previous = previousHomeCounts.put(id, home);
            if (previous != null && count % 2 != 0)
                assertEquals((long) count, previous + home, "Annual home alternation: " + school + ", " + context);
        }
    }

    private void addMeetings(Map<HostedPair, Integer> counts, List<PlayedGame> season, int amount) {
        season.forEach(g -> counts.merge(new HostedPair(g.home(), g.away()), amount, Integer::sum));
    }

    private void checkFrequency(Conference conference, Format format, Map<HostedPair, Integer> counts, String context) {
        for (School first : conference.getSchools()) for (School second : conference.getSchools()) {
            if (first.getTgid() >= second.getTgid()) continue;
            boolean annual = format.games() == 0 || first.getDivision().equals(second.getDivision())
                    || (format.rivals() && first.getxDivRival().equals(second));
            int rotatingGames = format.games() - (format.teams() / 2 - 1) - (format.rivals() ? 1 : 0);
            int rotatingOpponents = format.teams() / 2 - (format.rivals() ? 1 : 0);
            int expected = annual ? format.window() : format.window() * rotatingGames / rotatingOpponents;
            int home = counts.getOrDefault(new HostedPair(first.getTgid(), second.getTgid()), 0);
            int away = counts.getOrDefault(new HostedPair(second.getTgid(), first.getTgid()), 0);
            String detail = first + " vs " + second + ", rolling " + format.window() + " seasons: " + context;
            assertEquals(expected, home + away, "Equal frequency: " + detail);
            // A seven-season window has an odd count for annual games; the
            // next-meeting assertion still requires those venues to alternate.
            assertTrue(Math.abs(home - away) <= 1, "Balanced venues: " + detail);
        }
    }

    private static Stream<Arguments> cases() {
        List<Format> formats = new ArrayList<>();
        for (int teams : new int[]{8, 9, 10, 11}) formats.add(new Format(teams, 0, false, 2));
        formats.addAll(List.of(new Format(12, 8, false, 4), new Format(12, 8, true, 10),
                new Format(12, 9, false, 6), new Format(12, 9, true, 10),
                new Format(14, 8, false, 7), new Format(14, 8, true, 12),
                new Format(14, 9, false, 14), new Format(14, 9, true, 6)));
        List<Arguments> cases = new ArrayList<>();
        for (Format format : formats) for (long seed : new long[]{0, 7, 42})
            for (int year : new int[]{1980, 2004}) for (WeekChoice choice : WeekChoice.values())
                cases.add(Arguments.of(format, seed, year, choice, true));
        // The same new week-choice sequences without shuffling distinguish
        // ordering defects from previously untested placement failures.
        for (long seed : new long[]{7, 42})
            cases.add(Arguments.of(new Format(11, 0, false, 2), seed, 1980, WeekChoice.SEEDED, false));
        return cases.stream();
    }

    private enum WeekChoice { EARLIEST, SEEDED }
    private record Format(int teams, int games, boolean rivals, int window) { }
    private record Pair(int first, int second) { }
    private record HostedPair(int home, int away) { }
    private record TeamWeek(int team, int week) { }
    private record PlayedGame(int home, int away, int week, boolean conference) { }
}
