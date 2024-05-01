package com.robotdebris.ncaaps2scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.ConferenceRepository;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.util.TestUtil;

@SpringBootTest(properties = "spring.profiles.active=test")
public class NonConferenceSchedulingIntegrationTest {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ConferenceRepository conferenceRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    public void setUp() throws Exception {
        gameRepository.removeAll();
    }

    @ParameterizedTest
    @MethodSource("provideYears")
    public void verifyNonConferenceGameCounts(int year) throws Exception {
        MockMultipartFile file = TestUtil.createMockMultipartFile("Default_06_07_Conferences.xlsx");
        scheduleService.setAlignmentFile(file);
        gameRepository.setYear(year);
        scheduleService.addAllConferenceGames();
        // Assert
        scheduleService.autoAddGames(false);
        List<School> schools = schoolRepository.findByNCAADivision(NCAADivision.FBS);
        for (School school : schools) {
            verifyNonConfGamesForYear(school);
        }
    }

    private void verifyNonConfGamesForYear(School school) {
        List<Game> schedule = gameRepository.findGamesByTeam(school);
        String message = "For school '%s' in year %d: expected %d games, found %d.".formatted(school.getName(),
                gameRepository.getYear(), 12, schedule.size());
        assertThat(schedule.size()).as(message).isEqualTo(12);
    }

    public static IntStream provideYears() {
        return IntStream.rangeClosed(2000, 2010);
    }

}
