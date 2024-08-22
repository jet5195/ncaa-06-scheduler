package com.robotdebris.ncaaps2scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceScheduler;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;

@SpringBootTest
public class ConferenceSchedulerTest {
    @Autowired
    ConferenceSchedulerFactory conferenceSchedulerFactory;

    @Autowired
    GameRepository gameRepository;

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void testConferenceSchedulingNumberOfHomeGames(int numOfTeams, int numOfGames, int year) {
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
        Conference conf = setupConference(numOfTeams, numOfGames,
                "Conference " + numOfTeams + " teams " + numOfGames + " games");
        if (numOfGames == 0) {
            numOfGames = conf.getSchools().size() - 1;
        }
        ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);
        try {
            scheduler.generateConferenceSchedule(conf, gameRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (School school : conf.getSchools()) {
            List<Game> schedule = gameRepository.findGamesByTeam(school);
            int homeGamesCount = (int) schedule.stream().filter(g -> g.getHomeTeam().equals(school)).count();

            if (numOfGames % 2 == 1) {
                // Adjust the expected home games count based on the number of games
                int expectedHomeGamesCount = (numOfGames + 1) / 2; // For odd numbers of games

                // Check if the home games count falls within the acceptable range
                assertTrue(homeGamesCount == expectedHomeGamesCount || homeGamesCount == expectedHomeGamesCount - 1,
                        "Expected home games count for school " + school.getName() + " to be " + expectedHomeGamesCount
                                + " or " + (expectedHomeGamesCount - 1) + " but was: " + homeGamesCount);
            } else {
                // For even numbers of games, the existing assertion remains the same
                assertEquals(numOfGames / 2, homeGamesCount,
                        "Expected home games count for school " + school.getName() + " to be " + numOfGames / 2
                                + " but was: " + homeGamesCount);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void testAlternateHomeLocationBackToBackSeasons(int numOfTeams, int numOfGames, int year) {
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
        Conference conf = setupConference(numOfTeams, numOfGames,
                "Conference " + numOfTeams + " teams " + numOfGames + " games");
        ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);

        List<Game> firstSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(year + 1);

        try {
            scheduler.generateConferenceSchedule(conf, gameRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Verify the home location alternates between years for each team
        for (Game game : firstSeasonSchedule) {
            // Check if the home location alternates between years
            boolean alternateHomeLocation = true;
            Optional<Game> game2 = gameRepository.findGameByTeams(game.getAwayTeam(), game.getHomeTeam());
            if (game2.isPresent()) {
                if (game.getHomeTeam().equals(game2.get().getHomeTeam())) {
                    alternateHomeLocation = false;
                    break;
                }
            }
            assertTrue(alternateHomeLocation,
                    "Expected home location to alternate between years for school " + game.getHomeTeam().getName());
        }
    }

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void testAllSchoolsPlayEachOther(int numOfTeams, int numOfGames, int year) {
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
        Conference conf = setupConference(numOfTeams, numOfGames,
                "Conference " + numOfTeams + " teams " + numOfGames + " games");
        ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);

        List<Game> firstSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);
        List<Game> secondSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);
        List<Game> thirdSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);
        List<Game> fourthSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);
        List<Game> fifthSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);
        List<Game> sixthSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);

        // try {
        // scheduler.generateConferenceSchedule(conf, gameRepository);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // Verify each school plays one another each other at least once in one of the 5
        // Lists above
        // Verify each school plays one another at least once in one of the 5 schedules
        List<School> schools = conf.getSchools();
        boolean allSchoolsPlayedEachOther = true;

        for (int i = 0; i < schools.size(); i++) {
            School school1 = schools.get(i);
            for (int j = i + 1; j < schools.size(); j++) {
                School school2 = schools.get(j);
                boolean schoolsPlayedEachOther = false;
                for (List<Game> schedule : Arrays.asList(firstSeasonSchedule, secondSeasonSchedule,
                        thirdSeasonSchedule, fourthSeasonSchedule, fifthSeasonSchedule, sixthSeasonSchedule)) {
                    if (schedule.stream()
                            .anyMatch(g -> (g.getHomeTeam().equals(school1) && g.getAwayTeam().equals(school2)) ||
                                    (g.getHomeTeam().equals(school2) && g.getAwayTeam().equals(school1)))) {
                        schoolsPlayedEachOther = true;
                        break;
                    }
                }
                if (!schoolsPlayedEachOther) {
                    allSchoolsPlayedEachOther = false;
                    break;
                }

            }
        }

        assertTrue(allSchoolsPlayedEachOther,
                "Each school should play one another at least once in one of the 6 schedules");
    }

    private List<Game> getCopyOfSeasonSchedule(Conference conf, ConferenceScheduler scheduler) {
        try {
            scheduler.generateConferenceSchedule(conf, gameRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Game> firstSeasonSchedule = List.copyOf(gameRepository.findAll());
        gameRepository.findAll().clear();
        return firstSeasonSchedule;
    }

    private static Stream<Arguments> provideConferenceParameters() {
        List<Integer> numOfTeamsList = Arrays.asList(12, 14);
        List<Integer> numOfGamesList = Arrays.asList(8, 9);
        List<Integer> yearsList = IntStream.rangeClosed(0, 10).boxed().collect(Collectors.toList());

        List<Arguments> argumentsList = new ArrayList<>();
        for (int numOfTeams : numOfTeamsList) {
            for (int numOfGames : numOfGamesList) {
                for (int year : yearsList) {
                    argumentsList.add(Arguments.of(numOfTeams, numOfGames, year));
                }
            }
        }
        // separate logic for 11 or fewer teams because numOfGames is irrelevant
        List<Integer> numOfTeamsNoDivsList = Arrays.asList(8, 9, 10, 11);
        for (int numOfTeams : numOfTeamsNoDivsList) {
            for (int year : yearsList) {
                argumentsList.add(Arguments.of(numOfTeams, 0, year));
            }
        }

        return argumentsList.stream();
    }

    Conference setupConference(int numOfTeams, int numOfGames, String conferenceName) {
        Conference conf = new Conference();
        conf.setName(conferenceName);
        conf.setNumOfConfGames(numOfGames);
        conf.setClassification(NCAADivision.FBS);

        List<Division> divisions = new ArrayList<>();
        List<School> schools = new ArrayList<>();

        for (int i = 1; i <= numOfTeams; i++) {
            School school = new School.Builder().withTgid(i).withName(String.valueOf((char) ('A' + i - 1)))
                    .withConference(conf).build();
            schools.add(school);
        }

        for (int i = 0; i < 2; i++) {
            Division division = new Division();
            division.setDivisionId(i);
            division.setName("Div " + (char) ('A' + i));
            division.setSchools(new ArrayList<>(schools.subList(i * (numOfTeams / 2), (i + 1) * (numOfTeams / 2))));
            divisions.add(division);
        }

        conf.setSchools(schools);
        conf.setDivisions(divisions);

        for (Division division : divisions) {
            division.setConference(conf);
        }

        return conf;
    }
}
