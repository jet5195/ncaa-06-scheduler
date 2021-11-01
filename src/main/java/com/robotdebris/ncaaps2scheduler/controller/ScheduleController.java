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
public class ScheduleController {

	@Autowired
	private ScheduleService scheduleService;

	@GetMapping(value = "schedule/year")
	public int getYear() {
		return scheduleService.getYear();
	}

	@PutMapping(value = "schedule/year/{year}")
	public void setYear(@PathVariable int year) {
		scheduleService.setYear(year);
	}

	@GetMapping(value = "/schools")
	public SchoolList getAllSchools() {
		SchoolList schoolList = scheduleService.getSchoolList();
		return schoolList;
	}

	@GetMapping(value = "/schools/{tgid}")
	public School getSchoolByTgid(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school;
	}
	
//	@GetMapping(value = "/schools/{name}")
//	public School getSchoolByName(@PathVariable String name) {
//		School school = scheduleService.searchSchoolByName(name);
//		return school;
//	}

	@GetMapping(value = "schools/{tgid}/schedule")
	public SchoolSchedule getSchoolSchedule(@PathVariable Integer tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school.getSchedule();
	}

	@GetMapping(value = "schools/{tgid}/rivals")
	public SchoolList getSchoolRivals(@PathVariable int tgid) {
		School school = scheduleService.searchSchoolByTgid(tgid);
		return school.getRivals();
	}

	@GetMapping(value = "schools/{tgid}/week/{week}/available-opponents")
	public SchoolList getAvailableOpponents(@PathVariable int tgid, @PathVariable int week) {
		SchoolList availableOpponents = scheduleService.getAvailableOpponents(tgid, week);
		return availableOpponents;
	}

	@GetMapping(value = "schools/{tgid}/week/{week}/available-rivals")
	public SchoolList getAvailableRivals(@PathVariable int tgid, @PathVariable int week) {
		SchoolList availableRivals = scheduleService.getAvailableRivals(tgid, week);
		return availableRivals;
	}

	@DeleteMapping(value = "schools/{tgid}/week/{week}/remove-game")
	public void removeGame(@PathVariable int tgid, @PathVariable int week){
		scheduleService.removeGame(tgid, week);
	}

	@DeleteMapping(value = "schedule/remove-all-ooc-games")
	public int removeAllOocGames(){
		return scheduleService.removeAllOocGames();
	}

	@DeleteMapping(value = "schedule/remove-all-ooc-games-but-rivalry")
	public int removeAllOocNonRivalGames(){
		return scheduleService.removeAllOocNonRivalGames();
	}

	@DeleteMapping(value = "schedule/remove-all-fcs-games")
	//return count of removed games
	public int removeAllFcsGames(){
		return scheduleService.removeAllFcsGames();
	}

	@GetMapping(value = "schools/{tgid}/empty-weeks")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid){
		School school = scheduleService.searchSchoolByTgid(tgid);
		return scheduleService.findEmptyWeeks(school);
	}

	@GetMapping(value = "schools/{tgid}/empty-weeks/{tgid2}")
	public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid, @PathVariable int tgid2){
		return scheduleService.getEmptyWeeks(tgid, tgid2);
	}

	//Change this to use a RequestGame object that doesn't exist yet
	@PostMapping(value = "schedule/add-game")
	public void addGame(@RequestBody AddGameRequest addGameRequest){
		scheduleService.addGame(addGameRequest.getAwayId(), addGameRequest.getHomeId(), addGameRequest.getWeek());
	}
	
	@GetMapping(value = "schools/{id}/suggest-game")
	public SuggestedGameResponse getSuggestedGame(@PathVariable int id) {
		return scheduleService.getSuggestedGame(id);
	}
	
	@PutMapping(value = "schedule/auto-add-games")
	public int autoAddGames() {
		return scheduleService.autoAddGames(false);
	}
	
	@PutMapping(value = "schedule/auto-add-games-aggressive")
	public int autoAddGamesAgressive() {
		return scheduleService.autoAddGames(true);
	}
	
	@PutMapping(value = "schedule/auto-add-games-rivals")
	public int autoAddRivalries() {
		return scheduleService.autoAddRivalries();
	}
	
	@PutMapping(value = "schedule/auto-add-games-random")
	public int autoAddGamesRandomly() {
		return scheduleService.autoAddRandomly();
	}
	
	@PutMapping(value = "schedule/remove-all-games")
	public int removeAllGames() {
		return scheduleService.removeAllGames();
	}
	
	@PutMapping(value = "conferences/{name}/add-games")
	public int autoAddConferenceGames(@PathVariable String name) {
		return scheduleService.autoAddConferenceGames(name, 0);
	}

	@PutMapping(value = "conferences/{name}/remove-games")
	public int removeConferenceGames(@PathVariable String name) {
		return scheduleService.removeConferenceGames(name);
	}
	
	@PutMapping(value = "schedule/fix")
	public int fixSchedule() {
		return scheduleService.fixSchedule();
	}
	
	@PostMapping(value = "schedule/save-to-file")
	public void saveToFile() {
		scheduleService.saveToFile();
	}
	
	@GetMapping(value = "schedule/download")
	public void downloadSchedule(HttpServletResponse response) throws IOException {
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=new_sched.xlsx");
        ByteArrayInputStream stream = scheduleService.downloadSchedule();
        IOUtils.copy(stream, response.getOutputStream());
	}
	
	@GetMapping(value = "/conferences")
	public ConferenceList getAllConferences() {
		ConferenceList conferenceList = scheduleService.getConferenceList();
		return conferenceList;
	}
	
	@GetMapping(value = "/conferences/{name}")
	public Conference getConferenceByName(@PathVariable String name) {
		Conference conference = scheduleService.searchConferenceByName(name);
		return conference;
	}
	
	@GetMapping(value = "/conferences/{name}/schools")
	public SchoolList getSchoolsByConference(@PathVariable String name) {
		SchoolList schools = scheduleService.getSchoolsByConference(name);
		return schools;
	}
	
	@PostMapping(value = "/schedule/set-by-file")
	public void setScheduleFile(@RequestParam("file") MultipartFile scheduleFile) throws IOException {
		scheduleService.setScheduleFile(scheduleFile);
		
	}
	
	@PostMapping(value = "/conferences/set-by-file")
	public void setAlignmentFile(@RequestParam("file") MultipartFile alignmentFile) throws IOException {
		scheduleService.setAlignmentFile(alignmentFile);
	}

	@GetMapping(value = "schedule/week/{week}")
	public ArrayList<Game> getScheduleByWeek(@PathVariable("week") int week) {
		return scheduleService.getScheduleByWeek(week);
	}

	@GetMapping(value = "schedule/bowl-games")
	public SeasonSchedule getBowlGames() {
		return scheduleService.getBowlGames();
	}
}
