package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolService schoolService;

    @Test
    public void whenFindTooFewGames_thenCorrectSchoolsReturned() {
        // Arrange
        School schoolWithEnoughGames = createSchool("School A", "FBS", 12);
        School schoolWithFewGames = createSchool("School B", "FBS", 11);
        School schoolNotInFBS = createSchool("School C", "FCS", 10);
        List<School> schools = Arrays.asList(schoolWithEnoughGames, schoolWithFewGames, schoolNotInFBS);
        when(schoolRepository.findAll()).thenReturn(schools);

        // Act
        List<School> schoolsWithTooFewGames = schoolService.findTooFewGames();

        // Assert
        assertThat(schoolsWithTooFewGames)
                .containsExactlyInAnyOrder(schoolWithFewGames)
                .doesNotContain(schoolWithEnoughGames, schoolNotInFBS);
    }

    @Test
    public void whenFindTooManyGames_thenCorrectSchoolsReturned() {
        // Arrange
        School schoolWithTooManyGames = createSchool("School D", "FBS", 13);
        School schoolWithExactGames = createSchool("School E", "FBS", 12);
        School schoolNotInFBSWithTooManyGames = createSchool("School F", "FCS", 13);
        List<School> schools = Arrays.asList(schoolWithTooManyGames, schoolWithExactGames, schoolNotInFBSWithTooManyGames);
        when(schoolRepository.findAll()).thenReturn(schools);

        // Act
        List<School> schoolsWithTooManyGames = schoolService.findTooManyGames();

        // Assert
        assertThat(schoolsWithTooManyGames)
                .containsExactlyInAnyOrder(schoolWithTooManyGames)
                .doesNotContain(schoolWithExactGames, schoolNotInFBSWithTooManyGames);

        // Verify
        verify(schoolRepository).findAll();
    }

    private School createSchool(String name, String division, int numberOfGames) {
        School school = new School();
        school.setName(name);
        school.setNcaaDivision(division);
        school.setSchedule(createSchedule(numberOfGames));
        return school;
    }

    private SchoolSchedule createSchedule(int numberOfGames) {
        SchoolSchedule schedule = new SchoolSchedule();
        for (int i = 0; i < numberOfGames; i++) {
            schedule.add(new Game());
        }
        return schedule;
    }
}
