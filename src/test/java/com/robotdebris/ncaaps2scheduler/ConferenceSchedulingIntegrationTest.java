package com.robotdebris.ncaaps2scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
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
		MockMultipartFile file = createMockMultipartFile("Default_06_07_Conferences.xlsx");
		scheduleService.setAlignmentFile(file);
		gameRepository.removeAll();
	}

	@ParameterizedTest
	@MethodSource("provideYears")
	public void verifyConferenceGameCounts(int year) throws Exception {
		gameRepository.setYear(year);
		scheduleService.addAllConferenceGames();
		// Assert
		List<Conference> conferences = conferenceRepository.findByNCAADivision(NCAADivision.FBS);
		for (Conference conf : conferences) {
			verifyConferenceGamesForYear(conf, year);
		}
	}

	private void verifyConferenceGamesForYear(Conference conf, int year) {
		int expectedGames = calculateExpectedGames(conf);
		List<School> schools = schoolRepository.findByConference(conf);

		for (School school : schools) {
			List<Game> schedule = gameRepository.findGamesByTeam(school);
			String message = String.format(
					"For conference '%s' and school '%s' in year %d: expected %d games, found %d.", conf.getName(),
					school.getName(), year, expectedGames, schedule.size());
			assertThat(schedule.size()).as(message).isEqualTo(expectedGames);
		}
	}

	private int calculateExpectedGames(Conference conf) {
		return conf.getNumOfConfGames() > 0 ? conf.getNumOfConfGames() : conf.getNumOfSchools() - 1;
	}

	public static IntStream provideYears() {
		return IntStream.rangeClosed(2013, 2015);
	}

	/////

//	@Test
//	public void testDefaultConferences() throws Exception {
//		verifyConferenceGameCounts("Default_06_07_Conferences.xlsx");
//	}
//
//	private void verifyConferenceGameCounts(String fileName) throws Exception {
//		MockMultipartFile file = createMockMultipartFile(fileName);
//		scheduleService.setAlignmentFile(file);
//
//		for (int i = 1980; i <= 2050; i++) {
//			testConfScheduleByYear(i);
//		}
//	}
//
//	private void testConfScheduleByYear(int year) throws Exception {
//		System.out.println("Setting schedule for year " + year);
//		gameRepository.removeAll();
//		// Act - perform the scheduling
//		scheduleService.addAllConferenceGames();
//		gameRepository.setYear(year);
//		// Assert - for each conference all schools have correct number of games
//		List<Conference> conferenceList = conferenceRepository.findByNCAADivision(NCAADivision.FBS);
//		for (Conference conf : conferenceList) {
//			// TODO: Revisit this logic if you allow non round robin schedules for 11 or
//			// fewer confs
//			int numOfConfGames = conf.getNumOfConfGames() > 0 ? conf.getNumOfConfGames() : conf.getNumOfSchools() - 1;
//			List<School> schools = schoolRepository.findByConference(conf);
//			for (School school : schools) {
//				List<Game> schedule = gameRepository.findGamesByTeam(school);
//				System.out.println("Testing " + conf + " and school " + school + ". " + numOfConfGames + " expected, "
//						+ schedule.size() + " actual.");
//				assertEquals(numOfConfGames, schedule.size());
//			}
//		}
//	}

	private MockMultipartFile createMockMultipartFile(String fileName) throws IOException, URISyntaxException {
		// Load the file from the classpath
		Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
		byte[] content = Files.readAllBytes(path);

		// Create a MockMultipartFile object
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", // Parameter name used in the form
				fileName, // Filename
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Content type
				content // File content
		);

		return mockMultipartFile;
	}

}
