package com.robotdebris.ncaaps2scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.ConferenceRepository;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.util.TestUtil;

@SpringBootTest(properties = "spring.profiles.active=test")
public class ConferenceSchedulingIntegrationTest {
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
	public void verifyConferenceGameCountsForDefaultConferences(int year) throws Exception {
		MockMultipartFile file = TestUtil.createMockMultipartFile("Default_06_07_Conferences.xlsx");
		scheduleService.setAlignmentFile(file);
		gameRepository.setYear(year);
		scheduleService.addAllConferenceGames();
		// Assert
		List<Conference> conferences = conferenceRepository.findByNCAADivision(NCAADivision.FBS);
		for (Conference conf : conferences) {
			verifyConferenceGamesForYear(conf, year);
		}
	}

	@ParameterizedTest
	@MethodSource("provideYears")
	public void verifyConferenceGameCountsForRobotDebrisConferences(int year) throws Exception {
		MockMultipartFile file = TestUtil.createMockMultipartFile("RobotDebris_Custom_Conf_V2.xlsx");
		scheduleService.setAlignmentFile(file);
		gameRepository.setYear(year);
		scheduleService.addAllConferenceGames();
		// Assert
		List<Conference> conferences = conferenceRepository.findByNCAADivision(NCAADivision.FBS);
		for (Conference conf : conferences) {
			verifyConferenceGamesForYear(conf, year);
		}
	}



	@Test
	public void verifyConferenceGamesForTeamAndYears() throws Exception {
		MockMultipartFile file = TestUtil.createMockMultipartFile("Default_06_07_Conferences.xlsx");
		scheduleService.setAlignmentFile(file);
		School school = schoolRepository.findByName("Boston College");
		List<Integer> years = List.of(2005, 2006, 2007, 2008, 2009);
		for (int year : years) {
			gameRepository.removeAll();
			gameRepository.setYear(year);
			scheduleService.autoAddConferenceGames("ACC");
			List<Game> schedule = gameRepository.findGamesByTeam(school);

			System.out.println(year);
			System.out.println(schedule);

			String message = "For school '%s' in year %d: expected %d games, found %d.".formatted(school.getName(),
                    year, 8, schedule.size());
			assertThat(schedule.size()).as(message).isEqualTo(8);
		}
	}

	private void verifyConferenceGamesForYear(Conference conf, int year) {
		int expectedGames = calculateExpectedGames(conf);
		List<School> schools = schoolRepository.findByConference(conf);

		for (School school : schools) {
			List<Game> schedule = gameRepository.findGamesByTeam(school);
			String message = 
                    "For conference '%s' and school '%s' in year %d: expected %d games, found %d.".formatted(conf.getName(),
                    school.getName(), year, expectedGames, schedule.size());
			assertThat(schedule.size()).as(message).isEqualTo(expectedGames);
		}
	}

	private int calculateExpectedGames(Conference conf) {
		return conf.getNumOfConfGames() > 0 ? conf.getNumOfConfGames() : conf.getNumOfSchools() - 1;
	}

	public static IntStream provideYears() {
		return IntStream.rangeClosed(2000, 2010);
	}

	// private MockMultipartFile createMockMultipartFile(String fileName) throws IOException, URISyntaxException {
	// 	// Load the file from the classpath
	// 	Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
	// 	byte[] content = Files.readAllBytes(path);

	// 	// Create a MockMultipartFile object
	// 	MockMultipartFile mockMultipartFile = new MockMultipartFile("file", // Parameter name used in the form
	// 			fileName, // Filename
	// 			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Content type
	// 			content // File content
	// 	);

	// 	return mockMultipartFile;
	// }

}
