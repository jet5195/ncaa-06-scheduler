package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.configuration.AppConstants;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.ConferenceRepository;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import com.robotdebris.ncaaps2scheduler.util.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
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

/** Real workbook settings and rivalry data, using the full conference service entry point. */
@SpringBootTest(properties = "spring.profiles.active=test")
class RealAlignmentConferenceRequirementsTest {
    private static final int START_YEAR = 2000;
    private static final int SEASONS = 28;
    private static final long WEEK_SEED = 2026;
    @Autowired SchoolService schoolService;
    @Autowired ConferenceRepository conferences;
    @Autowired GameRepository games;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest(name = "{0}, weeks={1}")
    @MethodSource("alignments")
    void realAlignmentsSatisfyConferenceRequirements(String workbook, WeekChoice choice) throws Exception {
        // Reload schools as well as alignment so each workbook starts with fresh
        // memberships, protected opponents and ordinary rivalry lists.
        games.removeAll();
        schoolService.loadSchoolDataFromFile(TestUtil.createMockMultipartFile("NCAA_NEXT24_School_Data.xlsx"));
        scheduleService.setAlignmentFile(TestUtil.createMockMultipartFile(workbook));
        List<Conference> active = conferences.findByNCAADivision(NCAADivision.FBS).stream()
                .filter(c -> !AppConstants.INDEPENDENT_STRINGS.contains(c.getName()))
                .filter(c -> c.getSchools() != null && !c.getSchools().isEmpty()).toList();
        assertFalse(active.isEmpty(), "No schedulable FBS conferences loaded: " + workbook);
        Set<School> members = new HashSet<>();
        active.forEach(c -> members.addAll(c.getSchools()));
        Random random = new Random(WEEK_SEED);
        doAnswer(call -> {
            List<Integer> available = call.getArgument(0);
            assertFalse(available.isEmpty(), "Week selector received no candidates: " + workbook);
            List<Integer> ordered = available.stream().sorted().toList();
            return choice == WeekChoice.EARLIEST ? ordered.getFirst() : ordered.get(random.nextInt(ordered.size()));
        }).when(scheduleService).randomIntFromList(anyList());

        Map<Pair, Meeting> lastMeetings = new HashMap<>();
        Map<Integer, Long> previousHomeCounts = new HashMap<>();
        Map<Conference, List<List<PlayedGame>>> history = new HashMap<>();
        Map<Conference, Map<HostedPair, Integer>> rolling = new HashMap<>();
        for (int year = START_YEAR; year < START_YEAR + SEASONS; year++) {
            games.removeAll();
            games.setYear(year);
            String context = workbook + ", year=" + year + ", weeks=" + choice + ", seed=" + WEEK_SEED;
            assertDoesNotThrow(() -> scheduleService.addAllConferenceGames(), "Generation failed: " + context);
            List<PlayedGame> season = games.findAll().stream().map(g -> new PlayedGame(
                    g.getHomeTeam(), g.getAwayTeam(), g.getWeek(), g.isConferenceGame())).toList();
            checkSeason(active, members, season, previousHomeCounts, lastMeetings, year, context);
            for (Conference conference : active) {
                List<PlayedGame> conferenceGames = season.stream()
                        .filter(g -> conference.equals(g.home().getConference())).toList();
                List<List<PlayedGame>> samples = history.computeIfAbsent(conference, c -> new ArrayList<>());
                Map<HostedPair, Integer> counts = rolling.computeIfAbsent(conference, c -> new HashMap<>());
                samples.add(conferenceGames);
                addMeetings(counts, conferenceGames, 1);
                int window = window(conference, context);
                if (samples.size() > window) addMeetings(counts, samples.get(samples.size() - window - 1), -1);
                if (samples.size() >= window) checkFrequency(conference, counts, window, context);
            }
        }
    }

    private void checkSeason(List<Conference> active, Set<School> members, List<PlayedGame> season,
            Map<Integer, Long> previousHomeCounts, Map<Pair, Meeting> lastMeetings, int year, String context) {
        int expectedTotal = active.stream().mapToInt(c -> c.getSchools().size() * expectedGames(c) / 2).sum();
        assertEquals(expectedTotal, season.size(), "Complete conference season: " + context);
        Set<Pair> pairings = new HashSet<>();
        Set<TeamWeek> occupied = new HashSet<>();
        Map<School, List<PlayedGame>> schedules = new HashMap<>();
        for (PlayedGame game : season) {
            String detail = context + ", " + game;
            assertNotEquals(game.home(), game.away(), "Self-game: " + detail);
            assertTrue(members.contains(game.home()) && members.contains(game.away()), "Outside active conferences: " + detail);
            assertEquals(game.home().getConference(), game.away().getConference(), "Cross-conference game: " + detail);
            assertTrue(game.conference(), "Conference flag lost: " + detail);
            assertTrue(game.week() >= game.home().getConference().getConfGamesStartWeek() && game.week() < 15,
                    "Outside configured conference window: " + detail);
            Pair pair = new Pair(Math.min(game.home().getTgid(), game.away().getTgid()),
                    Math.max(game.home().getTgid(), game.away().getTgid()));
            assertTrue(pairings.add(pair), "Duplicate matchup: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.home().getTgid(), game.week())), "Home double-booked: " + detail);
            assertTrue(occupied.add(new TeamWeek(game.away().getTgid(), game.week())), "Away double-booked: " + detail);
            schedules.computeIfAbsent(game.home(), s -> new ArrayList<>()).add(game);
            schedules.computeIfAbsent(game.away(), s -> new ArrayList<>()).add(game);
            Meeting previous = lastMeetings.put(pair, new Meeting(year, game.home().getTgid()));
            if (previous != null) assertNotEquals(previous.host(), game.home().getTgid(),
                    "Repeated host from " + previous.year() + ": " + detail);
        }
        for (Conference conference : active) for (School school : conference.getSchools()) {
            List<PlayedGame> schedule = schedules.getOrDefault(school, List.of());
            String detail = conference.getName() + ", " + school + ": " + context;
            int count = expectedGames(conference);
            assertEquals(count, schedule.size(), "Game count: " + detail);
            Set<School> opponents = new HashSet<>();
            schedule.forEach(g -> opponents.add(g.home().equals(school) ? g.away() : g.home()));
            List<School> required = conference.getSchools().size() <= 11 ? conference.getSchools()
                    : school.getDivision().getSchools();
            for (School opponent : required) if (!opponent.equals(school))
                assertTrue(opponents.contains(opponent), "Missing required opponent " + opponent + ": " + detail);
            if (school.getxDivRival() != null) assertTrue(opponents.contains(school.getxDivRival()),
                    "Missing protected rival " + school.getxDivRival() + ": " + detail);
            long home = schedule.stream().filter(g -> g.home().equals(school)).count();
            assertTrue(home >= count / 2 && home <= (count + 1) / 2, "Annual home balance: " + detail + ", home=" + home);
            Long previous = previousHomeCounts.put(school.getTgid(), home);
            if (previous != null && count % 2 != 0) assertEquals((long) count, previous + home,
                    "Annual home alternation: " + detail);
        }
    }

    private void checkFrequency(Conference conference, Map<HostedPair, Integer> counts, int window, String context) {
        boolean roundRobin = conference.getSchools().size() <= 11;
        for (School first : conference.getSchools()) for (School second : conference.getSchools()) {
            if (first.getTgid() >= second.getTgid()) continue;
            boolean annual = roundRobin || first.getDivision().equals(second.getDivision())
                    || second.equals(first.getxDivRival());
            int expected = window;
            if (!annual) {
                int protectedGames = first.getxDivRival() == null ? 0 : 1;
                int rotatingGames = expectedGames(conference) - (first.getDivision().getSchools().size() - 1) - protectedGames;
                int eligible = conference.getSchools().size() - first.getDivision().getSchools().size() - protectedGames;
                assertEquals(0, window * rotatingGames % eligible, "Integral frequency window: " + conference + ", " + context);
                expected = window * rotatingGames / eligible;
            }
            int home = counts.getOrDefault(new HostedPair(first.getTgid(), second.getTgid()), 0);
            int away = counts.getOrDefault(new HostedPair(second.getTgid(), first.getTgid()), 0);
            String detail = conference.getName() + ", " + first + " vs " + second + ", rolling " + window + ": " + context;
            assertEquals(expected, home + away, "Equal frequency: " + detail);
            assertTrue(Math.abs(home - away) <= 1, "Balanced venue totals: " + detail);
        }
    }

    private int expectedGames(Conference conference) {
        return conference.getNumOfConfGames() > 0 ? conference.getNumOfConfGames() : conference.getSchools().size() - 1;
    }

    private int window(Conference conference, String context) {
        int teams = conference.getSchools().size();
        if (teams <= 11) return 2;
        assertTrue(teams == 12 || teams == 14, "Unsupported fixture format: " + conference + ", " + context);
        int count = expectedGames(conference);
        assertTrue(count == 8 || count == 9, "Unsupported fixture game count: " + conference + ", " + context);
        boolean rivals = conference.getSchools().stream().anyMatch(s -> s.getxDivRival() != null);
        if (teams == 12) return count == 8 ? (rivals ? 10 : 4) : (rivals ? 10 : 6);
        return count == 8 ? (rivals ? 12 : 7) : (rivals ? 6 : 14);
    }

    private void addMeetings(Map<HostedPair, Integer> counts, List<PlayedGame> season, int amount) {
        season.forEach(g -> counts.merge(new HostedPair(g.home().getTgid(), g.away().getTgid()), amount, Integer::sum));
    }

    private static Stream<Arguments> alignments() {
        List<Arguments> cases = new ArrayList<>();
        for (String workbook : List.of("Default_06_07_Conferences.xlsx", "RobotDebris_Custom_Conf_V2.xlsx",
                "CookyBots_Alignment.xlsx", "Test_All_Scenarios_Alignment.xlsx"))
            for (WeekChoice choice : WeekChoice.values()) cases.add(Arguments.of(workbook, choice));
        return cases.stream();
    }

    private enum WeekChoice { EARLIEST, SEEDED }
    private record Pair(int first, int second) { }
    private record HostedPair(int home, int away) { }
    private record TeamWeek(int team, int week) { }
    private record Meeting(int year, int host) { }
    private record PlayedGame(School home, School away, int week, boolean conference) { }
}
