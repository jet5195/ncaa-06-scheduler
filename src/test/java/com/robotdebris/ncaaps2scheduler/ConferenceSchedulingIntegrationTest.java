package com.robotdebris.ncaaps2scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@SpringBootTest
public class ConferenceSchedulingIntegrationTest {
	@Autowired
	private ScheduleService scheduleService; // Your scheduling service

	@Autowired
	private GameRepository gameRepository; // Your game repository

	@Test
	public void testConferenceGameScheduling() throws Exception {
		// setup crap
		MockMultipartFile file = createMockMultipartFile("Default_06_Conferences.xlsx");
		scheduleService.setAlignmentFile(file);

		// Act - perform the scheduling
		scheduleService.addAllConferenceGames();

		// Assert - verify the results
		List<Game> scheduledGames = gameRepository.findAll();
		assertNotNull(scheduledGames, "Scheduled games should not be null");
		assertFalse(scheduledGames.isEmpty(), "Scheduled games should not be empty");
		// Add more assertions to verify the schedule is as expected

		// Clean up - remove test data if necessary
	}

	public MockMultipartFile createMockMultipartFile(String fileName) throws IOException, URISyntaxException {
		// Load the file from the classpath
		Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
		byte[] content = Files.readAllBytes(path);

		// Create a MockMultipartFile object
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", // Parameter name used in the form
				"Default_06_Conferences.xlsx", // Filename
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Content type
				content // File content
		);

		return mockMultipartFile;
	}

}
