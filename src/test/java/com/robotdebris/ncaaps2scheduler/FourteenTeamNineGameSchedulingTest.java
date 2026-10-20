package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class FourteenTeamNineGameSchedulingTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest
    @ValueSource(ints = {0, 1980, 2005, 2020})
    void unprotectedOpponentsHaveEqualFrequencyInRollingCycles(int startYear) throws Exception {
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(14, 9, false);
        List<List<Game>> seasons = new ArrayList<>();
        for (int year = startYear; year < startYear + 28; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            seasons.add(List.copyOf(games.findAll()));
            if (seasons.size() >= 7) assertFrequency(conference, seasons, 7, 3, false);
            if (seasons.size() >= 14) assertFrequency(conference, seasons, 14, 6, true);
        }
    }

    private void assertFrequency(Conference conference, List<List<Game>> seasons,
            int window, int meetings, boolean balancedVenues) {
        List<Game> windowGames = seasons.subList(seasons.size() - window, seasons.size())
                .stream().flatMap(List::stream).toList();
        for (School first : conference.getDivisions().get(0).getSchools()) {
            for (School second : conference.getDivisions().get(1).getSchools()) {
                long firstHosts = windowGames.stream().filter(game -> game.getHomeTeam().equals(first)
                        && game.getAwayTeam().equals(second)).count();
                long secondHosts = windowGames.stream().filter(game -> game.getHomeTeam().equals(second)
                        && game.getAwayTeam().equals(first)).count();
                String context = first + " vs " + second + " in rolling " + window + " seasons ending at sample " + seasons.size();
                assertEquals(meetings, firstHosts + secondHosts, context);
                if (balancedVenues) {
                    assertEquals(3, firstHosts, context + " home meetings");
                    assertEquals(3, secondHosts, context + " away meetings");
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"0,false", "0,true", "1980,false", "1980,true",
            "2005,false", "2005,true", "2020,false", "2020,true"})
    void balancesAnnualVenuesAndAlternatesEveryNextMeeting(int startYear, boolean protectedRivals) throws Exception {
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(14, 9, protectedRivals);
        // Protected rivals must be linked by identity, not the imported row order.
        Collections.reverse(conference.getDivisions().get(1).getSchools());
        Map<String, Integer> lastHosts = new HashMap<>();
        Map<Integer, Long> lastHomeCounts = new HashMap<>();
        Map<Integer, Set<School>> allOpponents = new HashMap<>();
        for (int year = startYear; year < startYear + 28; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            assertEquals(63, games.findAll().size(), "14 teams times 9 games / 2 in " + year);
            Set<String> matchups = new HashSet<>();
            Set<String> teamWeeks = new HashSet<>();
            for (Game game : games.findAll()) {
                int home = game.getHomeTeam().getTgid();
                int away = game.getAwayTeam().getTgid();
                assertNotEquals(home, away);
                assertTrue(game.isConferenceGame());
                assertTrue(game.getWeek() >= conference.getConfGamesStartWeek() && game.getWeek() < 15);
                assertTrue(teamWeeks.add(home + ":" + game.getWeek()), "Home team double-booked");
                assertTrue(teamWeeks.add(away + ":" + game.getWeek()), "Away team double-booked");
                String pair = Math.min(home, away) + ":" + Math.max(home, away);
                assertTrue(matchups.add(pair), "Duplicate matchup");
                Integer lastHost = lastHosts.put(pair, home);
                if (lastHost != null) {
                    assertNotEquals(lastHost, Integer.valueOf(home),
                            "Next meeting must reverse venue: " + pair + " in " + year);
                }
            }
            for (School school : conference.getSchools()) {
                List<Game> schedule = games.findGamesByTeam(school);
                assertEquals(9, schedule.size(), school + " in " + year);
                Set<School> opponents = new HashSet<>();
                for (Game game : schedule) {
                    School opponent = game.getHomeTeam().equals(school) ? game.getAwayTeam() : game.getHomeTeam();
                    assertTrue(opponents.add(opponent));
                }
                if (protectedRivals) assertTrue(opponents.contains(school.getxDivRival()));
                List<School> division = conference.getDivisions().stream()
                        .filter(div -> div.getSchools().contains(school)).findFirst().orElseThrow().getSchools();
                for (School opponent : division) {
                    if (!opponent.equals(school)) assertTrue(opponents.contains(opponent), "Divisional opponent missing");
                }
                long homeCount = schedule.stream().filter(game -> game.getHomeTeam().equals(school)).count();
                assertTrue(homeCount == 4 || homeCount == 5, school + " home count=" + homeCount + " in " + year);
                Long previousHomeCount = lastHomeCounts.put(school.getTgid(), homeCount);
                if (previousHomeCount != null) assertEquals(9L, previousHomeCount + homeCount,
                        "Five-home and four-home seasons must alternate for " + school);
                Set<School> cycleOpponents = allOpponents.computeIfAbsent(school.getTgid(), key -> new HashSet<>());
                cycleOpponents.addAll(opponents);
                if (year >= startYear + 6) assertEquals(13, cycleOpponents.size(), "Every opponent within seven years");
            }
        }
    }
}
