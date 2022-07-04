package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.*;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("teams")
public class TeamController {

	@Autowired
	private ScheduleService scheduleService;
	
	@GetMapping
	public SchoolList getAllTeams() {
		SchoolList schoolList = scheduleService.getSchoolList();
		return schoolList;
	}
	
//	@GetMapping(value = "{tgid}")
//	public Team getTeamByTgid(@PathVariable int tgid) {
//		School school = scheduleService.searchSchoolByTgid(tgid);
//		return school;
//	}

	@PutMapping(value = "{tgid}")
	public void saveTeam(@RequestBody School team) {

	}

}
