package com.robotdebris.ncaaps2scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceScheduler;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@SpringBootTest
public class ConferenceSchedulerTest {
    @Autowired
    ConferenceSchedulerFactory conferenceSchedulerFactory;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    SchoolRepository SchoolRepository;

    @SpyBean
    ScheduleService scheduleService;

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void testConferenceSchedulingNumberOfHomeGames(Conference conf, int year) {
        int numOfGames = conf.getNumOfConfGames();
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
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
    public void testAlternateHomeLocationBackToBackSeasons(Conference conf, int year) {
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
        ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);

        List<Game> firstSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);
        gameRepository.setYear(++year);

        try {
            scheduler.generateConferenceSchedule(conf, gameRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Verify the home location alternates between years for each team
        for (Game game : firstSeasonSchedule) {
            // Check if the home location alternates between years

            Optional<Game> game2 = gameRepository.findGameByTeams(game.getAwayTeam(), game.getHomeTeam());
            if (game2.isPresent()) {
                if (game.getHomeTeam().equals(game2.get().getHomeTeam())) {
                    fail("Expected home location to alternate between years for school "
                            + game.getHomeTeam().getName());
                }
            }
        }
        assertTrue(true);

    }

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void testAllSchoolsPlayEachOther(Conference conf, int year) {
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
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
        gameRepository.setYear(++year);
        List<Game> seventhSeasonSchedule = getCopyOfSeasonSchedule(conf, scheduler);

        List<Game> allSchedules = Stream.of(firstSeasonSchedule, secondSeasonSchedule,
                thirdSeasonSchedule, fourthSeasonSchedule, fifthSeasonSchedule, sixthSeasonSchedule,
                seventhSeasonSchedule)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<School> schools = conf.getSchools();
        boolean allSchoolsPlayedEachOther = true;

        for (int i = 0; i < schools.size(); i++) {
            School school1 = schools.get(i);
            for (int j = i + 1; j < schools.size(); j++) {
                School school2 = schools.get(j);
                if (!allSchedules.stream()
                        .anyMatch(g -> (g.getHomeTeam().equals(school1) && g.getAwayTeam().equals(school2)) ||
                                (g.getHomeTeam().equals(school2) && g.getAwayTeam().equals(school1)))) {
                    allSchoolsPlayedEachOther = false;
                    System.out.println(school1 + " doesn't play " + school2);
                    fail(school1 + " doesn't play " + school2);
                    break;
                }
            }
        }

        assertTrue(allSchoolsPlayedEachOther,
                "Each school should play one another at least once in one of the 6 schedules");
    }

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @ParameterizedTest
    @MethodSource("provideConferenceParameters")
    public void printSchedules(Conference conf, int year) {
        // Mock only the specific method using the spy
        doAnswer(invocation -> {
            List<?> list = invocation.getArgument(0);
            if (list.isEmpty()) {
                return null; // Handle the empty list case
            }
            // Return the first item from the list if it's not empty
            return list.get(0);
        }).when(scheduleService).randomIntFromList(Mockito.anyList());
        // System.setOut(new PrintStream(outputStreamCaptor));
        gameRepository.findAll().clear();
        gameRepository.setYear(year);
        ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);
        try {
            scheduler.generateConferenceSchedule(conf, gameRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }

        School schoolA = new School.Builder().withTgid(1).withName("A").build();
        List<Game> schoolASchedule = gameRepository.findGamesByTeam(schoolA);
        // Print the table for the first team in the schedule
        System.out.println(year + " Schedule for " + schoolA.getName() + ":");
        System.out.println("--------------------------------------------------");
        System.out.println("--------------------------------------------------");
        int i = 1;
        for (Game game : schoolASchedule) {
            System.out.print(i++ + ". ");
            if (game.getHomeTeam().equals(schoolA)) {
                System.out.println("vs \t" + game.getAwayTeam().getName());
            } else {
                System.out.println("@ \t" + game.getHomeTeam().getName());
            }
        }
        assertTrue(true);
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
                    Conference confXDivRivals = setupConference(numOfTeams, numOfGames, true);
                    Conference conf = setupConference(numOfTeams, numOfGames, false);
                    argumentsList.add(Arguments.of(confXDivRivals, year));
                    argumentsList.add(Arguments.of(conf, year));
                }
            }
        }
        // separate logic for 11 or fewer teams because numOfGames is irrelevant
        List<Integer> numOfTeamsNoDivsList = Arrays.asList(8, 9, 10, 11);
        for (int numOfTeams : numOfTeamsNoDivsList) {
            for (int year : yearsList) {
                Conference conf = setupConference(numOfTeams, 0, false);
                argumentsList.add(Arguments.of(conf, year));
            }
        }

        return argumentsList.stream();
    }

    static Conference setupConference(int numOfTeams, int numOfGames, boolean xDivRivals) {
        Conference conf = new Conference();
        conf.setName("Conference " + numOfTeams + " teams " + numOfGames + " games " + xDivRivals + " xDivRivals");
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

        if (xDivRivals) {
            setupXDivRivals(conf);
        }

        return conf;
    }

    private static void setupXDivRivals(Conference conf) {
        if (!conf.getDivisions().isEmpty()) {
            for (int i = 0; i < conf.getDivisions().getFirst().getSchools().size(); i++) {
                School school = conf.getDivisions().getFirst().getSchools().get(i);
                School xDivRival = conf.getDivisions().get(1).getSchools().get(i);
                school.setxDivRival(xDivRival);
                xDivRival.setxDivRival(school);
            }
        }
    }
}
