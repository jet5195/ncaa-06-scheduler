package com.robotdebris.ncaaps2scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
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

	@Test
	public void testDefaultConferences() throws Exception {
		verifyConferenceGameCounts("Default_06_07_Conferences.xlsx");
	}

	private void verifyConferenceGameCounts(String fileName) throws Exception {
		// setup crap
		MockMultipartFile file = createMockMultipartFile(fileName);
		scheduleService.setAlignmentFile(file);

		for (int i = 1980; i <= 2050; i++) {
			testConfScheduleByYear(i);
		}
	}

	private void testConfScheduleByYear(int year) throws Exception {
		System.out.println("Setting schedule for year " + year);
		gameRepository.removeAll();
		// Act - perform the scheduling
		scheduleService.addAllConferenceGames();
		gameRepository.setYear(year);
		// Assert - for each conference all schools have correct number of games
		List<Conference> conferenceList = conferenceRepository.findByNCAADivision(NCAADivision.FBS);
		for (Conference conf : conferenceList) {
			// TODO: Revisit this logic if you allow non round robin schedules for 11 or
			// fewer confs
			int numOfConfGames = conf.getNumOfConfGames() > 0 ? conf.getNumOfConfGames() : conf.getNumOfSchools() - 1;
			List<School> schools = schoolRepository.findByConference(conf);
			for (School school : schools) {
				List<Game> schedule = gameRepository.findGamesByTeam(school);
				System.out.println("Testing " + conf + " and school " + school + ". " + numOfConfGames + " expected, "
						+ schedule.size() + " actual.");
				assertEquals(numOfConfGames, schedule.size());
			}
		}
	}

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
