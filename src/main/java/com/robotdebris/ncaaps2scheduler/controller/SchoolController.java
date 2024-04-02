package com.robotdebris.ncaaps2scheduler.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.model.SuggestedGameResponse;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schools")
public class SchoolController {

	@Autowired
	private ScheduleService scheduleService;

	@GetMapping
	public List<School> getAllSchools() {
		List<School> schoolList = scheduleService.getSchoolList();
		return schoolList;
	}

	@GetMapping(value = "{tgid}")
	public School getSchoolByTgid(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school;
	}

	@GetMapping(value = "{tgid}/suggest-game")
	public SuggestedGameResponse getSuggestedGame(@PathVariable int tgid) {
		return scheduleService.getSuggestedGame(tgid);
	}

	@GetMapping(value = "{tgid}/schedule")
	public SchoolSchedule getSchoolSchedule(@PathVariable Integer tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school.getSchedule();
	}

	@GetMapping(value = "{tgid}/rivals")
	public List<School> getSchoolRivals(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school.getRivals();
	}

	@GetMapping(value = "{tgid}/schedule/week/{week}/available-opponents")
	public List<School> getAvailableOpponents(@PathVariable int tgid, @PathVariable int week) {
		List<School> availableOpponents = scheduleService.getAvailableOpponents(tgid, week);
		return availableOpponents;
	}

	@GetMapping(value = "{tgid}/schedule/week/{week}/available-rivals")
	public List<School> getAvailableRivals(@PathVariable int tgid, @PathVariable int week) {
		List<School> availableRivals = scheduleService.getAvailableRivals(tgid, week);
		return availableRivals;
	}

	@PostMapping(value = "{tgid}/schedule/week/{week}/remove-game")
	public void removeGame(@PathVariable int tgid, @PathVariable int week) {
		scheduleService.removeGame(tgid, week);
	}

	@GetMapping(value = "{tgid}/schedule/empty-weeks")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return scheduleService.findEmptyWeeks(school);
	}

	@GetMapping(value = "{tgid}/schedule/empty-weeks/{tgid2}")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid, @PathVariable int tgid2) {
		return scheduleService.getEmptyWeeks(tgid, tgid2);
	}

}
