package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

/** Annual and gap-aware venue requirements for conference rotation formats. */
@SpringBootTest
class ConferenceRotationRequirementsTest {
    private static final int SEASONS = 28;
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @BeforeEach
    void chooseWeeksReproducibly() {
        games.removeAll();
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
    }

    @ParameterizedTest(name = "{0} teams, {1} games, rivals={2}, start={3}, reverse-order={4}")
    @MethodSource("formatsAndStartingYears")
    void generatedSeasonsSatisfyAnnualConferenceRequirements(int teams, int count,
            boolean rivals, int startYear, boolean reverseOrder) throws Exception {
        Conference conference = fixture(teams, count, rivals, reverseOrder);
        Map<Integer, Long> previousHomeCounts = new HashMap<>();
        for (int season = 0; season < SEASONS; season++) {
            int year = startYear + season;
            generate(conference, year);
            String context = conference + ", year=" + year + ", reverse-order=" + reverseOrder;
            assertEquals(teams * count / 2, games.findAll().size(), context);
            Set<String> teamWeeks = new HashSet<>();
            Set<Matchup> matchups = new HashSet<>();
            for (Game game : games.findAll()) {
                assertNotEquals(game.getHomeTeam(), game.getAwayTeam(), "Self-game: " + context);
                assertTrue(game.isConferenceGame(), "Nonconference game: " + context);
                assertTrue(game.getWeek() >= conference.getConfGamesStartWeek() && game.getWeek() < 15,
                        "Game outside conference window: " + context);
                assertTrue(matchups.add(matchup(game)), "Duplicate opponent pairing: " + context);
                assertTrue(teamWeeks.add(game.getHomeTeam().getTgid() + ":" + game.getWeek()),
                        "Home team double-booked: " + context);
                assertTrue(teamWeeks.add(game.getAwayTeam().getTgid() + ":" + game.getWeek()),
                        "Away team double-booked: " + context);
            }
            for (School school : conference.getSchools()) {
                List<Game> schedule = games.findGamesByTeam(school);
                assertEquals(count, schedule.size(), school + ": " + context);
                Set<School> opponents = new HashSet<>();
                for (Game game : schedule) {
                    School opponent = game.getHomeTeam().equals(school) ? game.getAwayTeam() : game.getHomeTeam();
                    assertTrue(opponents.add(opponent), "Repeated opponent for " + school + ": " + context);
                    assertTrue(conference.getSchools().contains(opponent), "Opponent outside conference: " + context);
                }
                List<School> division = conference.getDivisions().stream()
                        .filter(div -> div.getSchools().contains(school)).findFirst().orElseThrow().getSchools();
                for (School opponent : division) {
                    if (!opponent.equals(school)) assertTrue(opponents.contains(opponent),
                            "Missing divisional game " + school + " vs " + opponent + ": " + context);
                }
                if (rivals) assertTrue(opponents.contains(school.getxDivRival()),
                        "Protected rival missing for " + school + ": " + context);
                long homeCount = schedule.stream().filter(game -> game.getHomeTeam().equals(school)).count();
                if (count == 8) {
                    assertEquals(4, homeCount, "Eight-game home balance for " + school + ": " + context);
                } else {
                    assertTrue(homeCount == 4 || homeCount == 5,
                            "Nine-game home balance for " + school + ": " + context);
                    Long previousCount = previousHomeCounts.put(school.getTgid(), homeCount);
                    if (previousCount != null) assertEquals(9L, previousCount + homeCount,
                            "Annual five/four home alternation for " + school + ": " + context);
                }
            }
        }
    }

    @ParameterizedTest(name = "{0} teams, {1} games, rivals={2}, start={3}, reverse-order={4}")
    @MethodSource("formatsAndStartingYears")
    void everyNextMeetingSwapsVenueIncludingAfterGaps(int teams, int count,
            boolean rivals, int startYear, boolean reverseOrder) throws Exception {
        Conference conference = fixture(teams, count, rivals, reverseOrder);
        Map<Matchup, Meeting> lastMeetings = new HashMap<>();
        boolean checkedMeetingAfterGap = false;
        for (int season = 0; season < SEASONS; season++) {
            int year = startYear + season;
            generate(conference, year);
            // Prevent an empty/partial generator from passing the venue assertions vacuously.
            assertEquals(teams * count / 2, games.findAll().size(), conference + ", year=" + year);
            Set<Matchup> thisSeason = new HashSet<>();
            for (Game game : games.findAll()) {
                Matchup pair = matchup(game);
                assertTrue(thisSeason.add(pair), "Duplicate matchup in " + year + ": " + pair);
                Meeting previous = lastMeetings.put(pair, new Meeting(year, game.getHomeTeam().getTgid()));
                if (previous != null) {
                    if (year - previous.year() > 1) checkedMeetingAfterGap = true;
                    assertNotEquals(previous.host(), game.getHomeTeam().getTgid(),
                            conference + ", reverse-order=" + reverseOrder + ": "
                                    + game.getHomeTeam() + " hosted " + game.getAwayTeam()
                                    + " in both " + previous.year() + " and " + year);
                }
            }
        }
        assertTrue(checkedMeetingAfterGap, "Test must exercise a matchup returning after skipped seasons");
        for (School school : conference.getSchools()) {
            long opponentsSeen = lastMeetings.keySet().stream()
                    .filter(pair -> pair.first() == school.getTgid() || pair.second() == school.getTgid()).count();
            assertEquals(teams - 1, opponentsSeen, "Every conference opponent must be visited over the test horizon");
        }
    }

    private void generate(Conference conference, int year) throws Exception {
        games.removeAll();
        games.setYear(year);
        factory.getScheduler(conference).generateConferenceSchedule(conference, games);
    }

    private static Conference fixture(int teams, int count, boolean rivals, boolean reverseOrder) {
        Conference conference = ConferenceSchedulerTest.setupConference(teams, count, rivals);
        conference.setConfGamesStartWeek(3);
        if (reverseOrder) Collections.reverse(conference.getDivisions().get(1).getSchools());
        return conference;
    }

    private static Stream<Arguments> formatsAndStartingYears() {
        List<Arguments> cases = new ArrayList<>();
        for (int year : new int[] {1980, 2000, 2005, 2020}) {
            for (boolean reverseOrder : new boolean[] {false, true}) {
                cases.add(Arguments.of(12, 8, false, year, reverseOrder));
                cases.add(Arguments.of(12, 9, false, year, reverseOrder));
                cases.add(Arguments.of(12, 9, true, year, reverseOrder));
                cases.add(Arguments.of(14, 8, false, year, reverseOrder));
                cases.add(Arguments.of(14, 8, true, year, reverseOrder));
            }
        }
        return cases.stream();
    }

    private static Matchup matchup(Game game) {
        int home = game.getHomeTeam().getTgid(), away = game.getAwayTeam().getTgid();
        return new Matchup(Math.min(home, away), Math.max(home, away));
    }

    private record Matchup(int first, int second) { }
    private record Meeting(int year, int host) { }
}
