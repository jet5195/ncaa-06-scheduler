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
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class ConferenceOpponentFrequencyTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest(name = "{0} teams, {1} games, rivals={2}, start={5}, reversed={6}")
    @MethodSource("formats")
    void rotatingOpponentsHaveEqualFrequencyAndVenueTotals(int teams, int count, boolean rivals,
            int window, int period, int startYear, boolean reversed) throws Exception {
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(teams, count, rivals);
        conference.setConfGamesStartWeek(3);
        if (reversed) Collections.reverse(conference.getDivisions().get(1).getSchools());
        List<List<Game>> seasons = new ArrayList<>();
        // Slide through every phase of the existing rotation, rather than checking
        // only a conveniently aligned cycle. The window also makes equal venue
        // totals arithmetically possible; it is not a maximum acceptable wait.
        for (int year = startYear; year < startYear + window + period - 1; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            assertEquals(teams * count / 2, games.findAll().size(), "Complete season in " + year);
            seasons.add(List.copyOf(games.findAll()));
            if (seasons.size() < window) continue;
            List<Game> sample = seasons.subList(seasons.size() - window, seasons.size())
                    .stream().flatMap(List::stream).toList();
            int rotatingOpponents = teams / 2 - (rivals ? 1 : 0);
            int rotatingGames = count - (teams / 2 - 1) - (rivals ? 1 : 0);
            int expected = window * rotatingGames / rotatingOpponents;
            assertEquals(0, window * rotatingGames % rotatingOpponents, "Integral frequency window");
            assertEquals(0, expected % 2, "Integral home/away totals");
            for (School first : conference.getDivisions().get(0).getSchools()) {
                for (School second : conference.getDivisions().get(1).getSchools()) {
                    long home = sample.stream().filter(g -> g.getHomeTeam().equals(first)
                            && g.getAwayTeam().equals(second)).count();
                    long away = sample.stream().filter(g -> g.getHomeTeam().equals(second)
                            && g.getAwayTeam().equals(first)).count();
                    boolean protectedPair = rivals && second.equals(first.getxDivRival());
                    int meetings = protectedPair ? window : expected;
                    String context = first + " vs " + second + ", years " + (year - window + 1)
                            + "-" + year + ", protected=" + protectedPair;
                    assertEquals(meetings, home + away, "Equal opponent frequency: " + context);
                    // Odd windows can include one extra annual protected home game.
                    if (!protectedPair || window % 2 == 0) {
                        assertEquals(meetings / 2, home, "Home meetings: " + context);
                        assertEquals(meetings / 2, away, "Away meetings: " + context);
                    }
                }
            }
        }
    }

    private static Stream<Arguments> formats() {
        // teams, games, protected flag, fair comparison window, existing pattern period.
        // Keep the original 60-year regression window for 12/9/protected;
        // its new ten-year cycle is also checked over the shorter window.
        // Retain the original twelve-year regression window as well as the
        // stronger four-year home-and-away coverage requirement.
        int[][] formats = {{12,8,0,12,12}, {12,8,0,4,4}, {12,8,1,10,10},
                {12,9,0,6,6}, {12,9,1,60,10}, {12,9,1,10,10},
                {14,8,0,7,7}, {14,8,1,12,12},
                {14,9,0,14,14}, {14,9,1,6,6}};
        List<Arguments> cases = new ArrayList<>();
        for (int[] format : formats) for (int start : new int[]{1980, 2000})
            for (boolean reversed : new boolean[]{false, true})
                cases.add(Arguments.of(format[0], format[1], format[2] == 1,
                        format[3], format[4], start, reversed));
        return cases.stream();
    }
}
