package com.robotdebris.ncaaps2scheduler;

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
        School schoolWithFewGames = new School.Builder().withTgid(1).withNCAADivision(NCAADivision.FBS).build();
        List<Game> fewGames = Arrays.asList(new Game[11]); // Mocking a list with 11 games

        School schoolWithEnoughGames = new School.Builder().withTgid(2).withNCAADivision(NCAADivision.FBS).build();
        List<Game> enoughGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

        School fcsSchool = new School.Builder().withTgid(2).withNCAADivision(NCAADivision.FCS).build();
        List<Game> fcsGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

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
    }

    @Test
    public void whenFindTooManyGames_thenReturnSchoolsWithMoreThanTwelveGames() {
        // Arrange
        School schoolWithTooManyGames = new School.Builder().withTgid(1).withNCAADivision(NCAADivision.FBS).build();
        List<Game> fewGames = Arrays.asList(new Game[13]); // Mocking a list with 11 games

        School schoolWithEnoughGames = new School.Builder().withTgid(2).withNCAADivision(NCAADivision.FBS).build();
        List<Game> enoughGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

        School fcsSchool = new School.Builder().withTgid(2).withNCAADivision(NCAADivision.FCS).build();
        List<Game> fcsGames = Arrays.asList(new Game[12]); // Mocking a list with 12 games

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
    }
}
