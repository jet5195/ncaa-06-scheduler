package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class TwelveTeamProtectedRivalSchedulingTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest
    @ValueSource(ints = {0, 1980, 2005, 2020})
    void nineGamesPreserveRivalsBalanceVenuesAndRotateOpponents(int startYear) throws Exception {
        // Select the earliest available week so failures can be reproduced.
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(12, 9, true);
        // Protected pairings must work independently of the opposite division's list order.
        Collections.reverse(conference.getDivisions().get(1).getSchools());
        Map<String, Integer> previousHosts = new HashMap<>();
        Map<Integer, Long> previousHomeCounts = new HashMap<>();
        Map<Integer, Set<Integer>> opponentsOverCycle = new HashMap<>();
        for (int year = startYear; year < startYear + 12; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            assertEquals(54, games.findAll().size(), "12 teams times 9 games / 2");
            Set<String> teamWeeks = new HashSet<>();
            Map<String, Integer> hosts = new HashMap<>();
            for (Game game : games.findAll()) {
                int home = game.getHomeTeam().getTgid();
                int away = game.getAwayTeam().getTgid();
                assertNotEquals(home, away);
                assertTrue(game.isConferenceGame());
                assertTrue(game.getWeek() >= 0 && game.getWeek() < 15);
                assertTrue(teamWeeks.add(home + ":" + game.getWeek()), "Home team double-booked");
                assertTrue(teamWeeks.add(away + ":" + game.getWeek()), "Away team double-booked");
                String pair = Math.min(home, away) + ":" + Math.max(home, away);
                assertNull(hosts.put(pair, home), "Duplicate matchup");
                if (previousHosts.containsKey(pair)) {
                    assertNotEquals(previousHosts.get(pair), Integer.valueOf(home),
                            "Next meeting must reverse venue, even after a gap: " + pair + " in " + year);
                }
            }
            for (School school : conference.getSchools()) {
                List<Game> schedule = games.findGamesByTeam(school);
                assertEquals(9, schedule.size(), school + " in " + year);
                Set<School> opponents = new HashSet<>();
                for (Game game : schedule) {
                    School opponent = game.getHomeTeam().equals(school) ? game.getAwayTeam() : game.getHomeTeam();
                    assertTrue(opponents.add(opponent));
                    opponentsOverCycle.computeIfAbsent(school.getTgid(), key -> new HashSet<>()).add(opponent.getTgid());
                }
                assertTrue(opponents.contains(school.getxDivRival()), "Protected rival missing");
                List<School> division = conference.getDivisions().stream()
                        .filter(div -> div.getSchools().contains(school)).findFirst().orElseThrow().getSchools();
                for (School divisionalOpponent : division) {
                    if (!divisionalOpponent.equals(school)) assertTrue(opponents.contains(divisionalOpponent));
                }
                long homeCount = schedule.stream().filter(game -> game.getHomeTeam().equals(school)).count();
                assertTrue(homeCount == 4 || homeCount == 5, school + " has " + homeCount + " home games");
                Long previousHomeCount = previousHomeCounts.put(school.getTgid(), homeCount);
                if (previousHomeCount != null) {
                    assertEquals(9L, previousHomeCount + homeCount,
                            school + " must alternate four and five home games annually");
                }
            }
            // Keep the last venue for absent matchups too, so skipped seasons
            // cannot hide a failure to alternate at the next occurrence.
            previousHosts.putAll(hosts);
        }
        for (School school : conference.getSchools()) {
            assertEquals(11, opponentsOverCycle.get(school.getTgid()).size(), "Every conference opponent over cycle");
        }
    }
}
