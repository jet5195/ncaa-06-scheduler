package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @MockBean
    private SchoolRepository schoolRepository;

    @MockBean
    private GameRepository gameRepository;

    @Test
    public void whenFindTooFewGames_thenReturnSchoolsWithLessThanTwelveGames() {
        // Arrange
        School schoolWithFewGames = fbsSchool(1);
        List<Game> fewGames = Arrays.asList(new Game[11]); // Mocking a list with 11 games

        School schoolWithEnoughGames = fbsSchool(2);
        List<Game> enoughGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

        School fcsSchool = new School.Builder().withTgid(3).build();
        fcsSchool.setConference(Conference.blankConference);
        List<Game> fcsGames = Arrays.asList(new Game[11]); // Must be excluded even below the threshold.

        when(schoolRepository.findAll()).thenReturn(Arrays.asList(schoolWithFewGames, schoolWithEnoughGames, fcsSchool));
        when(gameRepository.findGamesByTeam(schoolWithFewGames)).thenReturn(fewGames);
        when(gameRepository.findGamesByTeam(schoolWithEnoughGames)).thenReturn(enoughGames);
        when(gameRepository.findGamesByTeam(fcsSchool)).thenReturn(fcsGames);

        // Act
        List<School> schoolsWithTooFewGames = scheduleService.findTooFewGames();

        // Assert
        assertEquals(1, schoolsWithTooFewGames.size());
        assertTrue(schoolsWithTooFewGames.contains(schoolWithFewGames));
        assertFalse(schoolsWithTooFewGames.contains(schoolWithEnoughGames));
        assertFalse(schoolsWithTooFewGames.contains(fcsSchool));
    }

    @Test
    public void whenFindTooManyGames_thenReturnSchoolsWithMoreThanTwelveGames() {
        // Arrange
        School schoolWithTooManyGames = fbsSchool(1);
        List<Game> fewGames = Arrays.asList(new Game[13]); // Only the count matters to this query.

        School schoolWithEnoughGames = fbsSchool(2);
        List<Game> enoughGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

        School fcsSchool = new School.Builder().withTgid(3).build();
        fcsSchool.setConference(Conference.blankConference);
        List<Game> fcsGames = Arrays.asList(new Game[13]); // Must be excluded even above the threshold.

        when(schoolRepository.findAll()).thenReturn(Arrays.asList(schoolWithTooManyGames, schoolWithEnoughGames, fcsSchool));
        when(gameRepository.findGamesByTeam(schoolWithTooManyGames)).thenReturn(fewGames);
        when(gameRepository.findGamesByTeam(schoolWithEnoughGames)).thenReturn(enoughGames);
        when(gameRepository.findGamesByTeam(fcsSchool)).thenReturn(fcsGames);

        // Act
        List<School> schoolsWithManyFewGames = scheduleService.findTooManyGames();

        // Assert
        assertEquals(1, schoolsWithManyFewGames.size());
        assertTrue(schoolsWithManyFewGames.contains(schoolWithTooManyGames));
        assertFalse(schoolsWithManyFewGames.contains(schoolWithEnoughGames));
        assertFalse(schoolsWithManyFewGames.contains(fcsSchool));
    }

    @Test
    public void whenGetGameBySchoolAndWeek_thenReturnCorrectGame() {
        // Arrange
        School testSchool = new School.Builder().withTgid(1).build();
        Game expectedGame = new Game(); // Set up the expected game with the correct week
        expectedGame.setWeek(3); // Assuming week 3 is the week we're testing for
        List<Game> games = Arrays.asList(new Game(), new Game(), expectedGame);

        when(gameRepository.findGamesByTeam(testSchool)).thenReturn(games);

        // Act
        Game actualGame = scheduleService.getGameBySchoolAndWeek(testSchool, 3);

        // Assert
        assertEquals(expectedGame, actualGame, "The retrieved game should match the expected game for the given week.");
    }

    @Test
    public void whenGetNumOfAwayConferenceGamesForSchool_thenReturnCorrectCount() {
        // Arrange

        School testSchool = mock(School.class); // Mock the test school
        School conferenceSchool = mock(School.class); // Mock the conference school
        School nonConferenceSchool = mock(School.class);
        Game awayConferenceGame = mock(Game.class); // Mock the game

        when(awayConferenceGame.getAwayTeam()).thenReturn(testSchool);
        when(awayConferenceGame.isConferenceGame()).thenReturn(true);
        when(awayConferenceGame.getHomeTeam()).thenReturn(conferenceSchool);
        when(conferenceSchool.isInConference(testSchool)).thenReturn(true);

        awayConferenceGame.setAwayTeam(testSchool);
        awayConferenceGame.setHomeTeam(conferenceSchool);

        Game homeConferenceGame = new Game();
        homeConferenceGame.setConferenceGame(true);
        homeConferenceGame.setAwayTeam(conferenceSchool);
        homeConferenceGame.setHomeTeam(testSchool);

        Game awayNonConferenceGame = new Game();
        awayNonConferenceGame.setAwayTeam(testSchool);
        awayNonConferenceGame.setHomeTeam(nonConferenceSchool);

        when(gameRepository.findGamesByTeam(testSchool)).thenReturn(Arrays.asList(awayConferenceGame, homeConferenceGame, awayNonConferenceGame));
        when(awayConferenceGame.getHomeTeam().isInConference(awayConferenceGame.getAwayTeam())).thenReturn(true);
        when(homeConferenceGame.getHomeTeam().isInConference(homeConferenceGame.getAwayTeam())).thenReturn(true);
        when(awayNonConferenceGame.getHomeTeam().isInConference(awayNonConferenceGame.getAwayTeam())).thenReturn(false);

        // Act
        int numOfAwayConferenceGames = scheduleService.getNumOfAwayConferenceGamesForSchool(testSchool);

        // Assert
        assertEquals(1, numOfAwayConferenceGames, "The count of away conference games should be 1.");
    }

    // These tests characterize the existing 12-game policy, not every historical season.
    private static School fbsSchool(int id) {
        Conference conference = new Conference();
        conference.setClassification(NCAADivision.FBS);
        return new School.Builder().withTgid(id).withConference(conference).build();
    }
}
