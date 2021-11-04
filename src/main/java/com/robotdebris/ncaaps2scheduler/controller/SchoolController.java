package com.robotdebris.ncaaps2scheduler.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import com.robotdebris.ncaaps2scheduler.model.*;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schools")
public class SchoolController {

	@Autowired
	private ScheduleService scheduleService;
	
	@GetMapping
	public SchoolList getAllSchools() {
		SchoolList schoolList = scheduleService.getSchoolList();
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
	public SchoolList getSchoolRivals(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school.getRivals();
	}

	@GetMapping(value = "{tgid}/schedule/week/{week}/available-opponents")
	public SchoolList getAvailableOpponents(@PathVariable int tgid, @PathVariable int week) {
		SchoolList availableOpponents = scheduleService.getAvailableOpponents(tgid, week);
		return availableOpponents;
	}

	@GetMapping(value = "{tgid}/schedule/week/{week}/available-rivals")
	public SchoolList getAvailableRivals(@PathVariable int tgid, @PathVariable int week) {
		SchoolList availableRivals = scheduleService.getAvailableRivals(tgid, week);
		return availableRivals;
	}

	@PostMapping(value = "{tgid}/schedule/week/{week}/remove-game")
	public void removeGame(@PathVariable int tgid, @PathVariable int week){
		scheduleService.removeGame(tgid, week);
	}
	
	@GetMapping(value = "{tgid}/schedule/empty-weeks")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid){
		School school = scheduleService.searchSchoolByTgid(tgid);
		return scheduleService.findEmptyWeeks(school);
	}

	@GetMapping(value = "{tgid}/schedule/empty-weeks/{tgid2}")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid, @PathVariable int tgid2){
		return scheduleService.getEmptyWeeks(tgid, tgid2);
	}

}
