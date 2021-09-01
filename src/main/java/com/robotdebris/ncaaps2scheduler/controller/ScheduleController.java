package com.robotdebris.ncaaps2scheduler.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolList;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class ScheduleController {

	@Autowired
	private ScheduleService scheduleService;

	@GetMapping(value = "/allschools")
	public SchoolList getAllSchools() {
		SchoolList schoolList = scheduleService.getSchoolList();
		return schoolList;
	}

	@GetMapping(value = "/searchSchoolByTgid/{tgid}")
	public School getSchoolByTgid(@PathVariable int tgid) {
		School school = scheduleService.searchByTgid(tgid);
		return school;
	}
	
	@GetMapping(value = "/searchSchoolByName/{name}")
	public School getSchoolByName(@PathVariable String name) {
		School school = scheduleService.searchByName(name);
		return school;
	}

	@GetMapping(value = "school/{id}/schedule")
	public SchoolSchedule getSchoolSchedule(@PathVariable Integer id) {
		School school = scheduleService.searchByTgid(id);
		return school.getSchedule();
	}

	@GetMapping(value = "school/{id}/rivals")
	public SchoolList getSchoolRivals(@PathVariable int id) {
		School school = scheduleService.searchByTgid(id);
		return school.getRivals();
	}

}
