package com.robotdebris.ncaaps2scheduler.controller;

import java.util.ArrayList;
import java.util.List;

import com.robotdebris.ncaaps2scheduler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

	@GetMapping(value = "school/{id}/availableOpponents/{week}")
	public SchoolList getAvailableOpponents(@PathVariable int id, @PathVariable int week) {
		SchoolList availableOpponents = scheduleService.getAvailableOpponents(id, week);
		return availableOpponents;
	}

	@GetMapping(value = "school/{id}/availableRivals/{week}")
	public SchoolList getAvailableRivals(@PathVariable int id, @PathVariable int week) {
		SchoolList availableRivals = scheduleService.getAvailableRivals(id, week);
		return availableRivals;
	}

	@DeleteMapping(value = "school/{id}/removeGame/{week}")
	public void removeGame(@PathVariable int id, int week){
		scheduleService.removeGame(id, week);
	}

	@DeleteMapping(value = "removeAllOocGames")
	public void removeAllOocGames(){
		scheduleService.removeAllOocGames();
	}

	@DeleteMapping(value = "removeAllOocNonRivalGames")
	public void removeAllOocNonRivalGames(){
		scheduleService.removeAllOocNonRivalGames();
	}

	@DeleteMapping(value = "removeAllFcsGames")
	public void removeAllFcsGames(){
		scheduleService.removeAllFcsGames();
	}

	@GetMapping(value = "school/{id}/findemptyweeks")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int id){
		School school = scheduleService.searchByTgid(id);
		return scheduleService.findEmptyWeeks(school);
	}

	@GetMapping(value = "school/{id}/findemptyweeks/{id2}")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int id, @PathVariable int id2){
		return scheduleService.getEmptyWeeks(id, id2);
	}

	//Change this to use a RequestGame object that doesn't exist yet
	@PostMapping(value = "addGame")
	public void addGame(@RequestBody AddGameRequest addGameRequest){
		scheduleService.addGame(addGameRequest.getAwayId(), addGameRequest.getHomeId(), addGameRequest.getWeek());
	}
}
