package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class TwelveTeamNineGameRotationTest {
    @Autowired GameRepository games;
    @Autowired ConferenceSchedulerFactory factory;
    @SpyBean ScheduleService scheduleService;

    @ParameterizedTest
    @CsvSource({"0,false", "0,true", "1980,false", "1980,true",
            "2005,false", "2005,true", "2020,false", "2020,true"})
    void retainsOpponentFrequencyAndBothStadiumsWithinFourSeasons(int startYear, boolean reverseOrder) throws Exception {
        doAnswer(call -> ((List<Integer>) call.getArgument(0)).getFirst())
                .when(scheduleService).randomIntFromList(anyList());
        Conference conference = ConferenceSchedulerTest.setupConference(12, 9, false);
        conference.setConfGamesStartWeek(3);
        if (reverseOrder) Collections.reverse(conference.getDivisions().get(1).getSchools());
        List<List<Game>> seasons = new ArrayList<>();
        for (int year = startYear; year < startYear + 18; year++) {
            games.removeAll();
            games.setYear(year);
            factory.getScheduler(conference).generateConferenceSchedule(conference, games);
            assertEquals(54, games.findAll().size());
            seasons.add(List.copyOf(games.findAll()));
            if (seasons.size() >= 4) checkWindow(conference, seasons, 4);
            if (seasons.size() >= 6) checkWindow(conference, seasons, 6);
        }
    }

    private void checkWindow(Conference conference, List<List<Game>> seasons, int length) {
        List<Game> window = seasons.subList(seasons.size() - length, seasons.size())
                .stream().flatMap(List::stream).toList();
        for (School first : conference.getDivisions().get(0).getSchools()) {
            for (School second : conference.getDivisions().get(1).getSchools()) {
                long firstHosts = window.stream().filter(game -> game.getHomeTeam().equals(first)
                        && game.getAwayTeam().equals(second)).count();
                long secondHosts = window.stream().filter(game -> game.getHomeTeam().equals(second)
                        && game.getAwayTeam().equals(first)).count();
                String context = first + " vs " + second + ", rolling " + length + " years ending at sample " + seasons.size();
                if (length == 4) {
                    assertTrue(firstHosts >= 1 && secondHosts >= 1, "Both stadiums must be visited: " + context);
                } else {
                    // Preserve the existing six-season opponent frequency and venue totals.
                    assertEquals(4, firstHosts + secondHosts, "Four meetings per six seasons: " + context);
                    assertEquals(2, firstHosts, "Two home meetings: " + context);
                    assertEquals(2, secondHosts, "Two away meetings: " + context);
                }
            }
        }
    }
}
