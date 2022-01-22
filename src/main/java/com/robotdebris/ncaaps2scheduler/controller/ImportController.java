package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolList;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.model.SuggestedGameResponse;
import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("import")
public class ImportController {

	@Autowired
	private ScheduleService scheduleService;

	@PostMapping(value = "schedule")
	public void importSchedule(@RequestParam("file") MultipartFile scheduleFile) throws IOException {
		scheduleService.setScheduleFile(scheduleFile);

	}

	@PostMapping(value = "players")
	public void importPlayers(@RequestParam("file") MultipartFile playersFile) throws IOException {
		scheduleService.setScheduleFile(playersFile);

	}

	@PostMapping(value = "transfers")
	public void importTransfers(@RequestParam("file") MultipartFile transfersFile) throws IOException {
		scheduleService.setScheduleFile(transfersFile);

	}

	@PostMapping(value = "conferences")
	public void importConferences(@RequestParam("file") MultipartFile conferenceFile) throws IOException {
		scheduleService.setAlignmentFile(conferenceFile);

	}

}
