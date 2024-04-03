package com.robotdebris.ncaaps2scheduler.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.model.AddGameRequest;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schedule")
public class ScheduleController {

	@Autowired
	private ScheduleService scheduleService;

	@GetMapping(value = "year")
	public int getYear() {
		return scheduleService.getYear();
	}

	@PostMapping(value = "year/{year}")
	public void setYear(@PathVariable int year) {
		scheduleService.setYear(year);
	}

	@PostMapping(value = "remove-all-ooc-games")
	public int removeAllOocGames() {
		return scheduleService.removeAllOocGames();
	}

	@PostMapping(value = "remove-all-ooc-games-but-rivalry")
	public int removeAllOocNonRivalGames() {
		return scheduleService.removeAllOocNonRivalGames();
	}

	@PostMapping(value = "remove-all-fcs-games")
	// return count of removed games
	public int removeAllFcsGames() {
		return scheduleService.removeAllFcsGames();
	}

	// Change this to use a RequestGame object that doesn't exist yet
	@PostMapping(value = "add-game")
	public void addGame(@RequestBody AddGameRequest addGameRequest) {
		scheduleService.addGame(addGameRequest);
	}

	@PostMapping(value = "auto-add-games")
	public int autoAddGames() {
		return scheduleService.autoAddGames(false);
	}

	@PostMapping(value = "auto-add-games-aggressive")
	public int autoAddGamesAgressive() {
		return scheduleService.autoAddGames(true);
	}

	@PostMapping(value = "auto-add-games-rivals")
	public int autoAddRivalries() {
		return scheduleService.autoAddRivalries();
	}

	@PostMapping(value = "auto-add-games-random")
	public int autoAddGamesRandomly() {
		return scheduleService.autoAddRandomly();
	}

	@PostMapping(value = "remove-all-games")
	public int removeAllGames() {
		return scheduleService.removeAllGames();
	}

	@PostMapping(value = "fix")
	public int fixSchedule() {
		return scheduleService.fixSchedule();
	}

	@GetMapping(value = "download")
	public void downloadSchedule(HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=new_sched.csv");
		scheduleService.downloadSchedule(response.getWriter());
	}

	@PostMapping(value = "set-by-file")
	public void setScheduleFile(@RequestParam("file") MultipartFile scheduleFile) throws IOException {
		scheduleService.setScheduleFile(scheduleFile);
	}

	@GetMapping(value = "week/{week}")
	public ArrayList<Game> getScheduleByWeek(@PathVariable("week") int week) {
		return scheduleService.getScheduleByWeek(week);
	}

	@GetMapping(value = "bowl-games")
	public List<Game> getBowlGames() {
		return scheduleService.getBowlGames();
	}

	@GetMapping(value = "week/{week}/{gameNumber}")
	public Game getGame(@PathVariable("week") int week, @PathVariable("gameNumber") int gameNumber) {
		return scheduleService.getGame(week, gameNumber);
	}

	@PostMapping(value = "remove-conference-games")
	public void removeAllConferenceGames() {
		scheduleService.removeAllConferenceGames();
	}

	@PostMapping(value = "add-conference-games")
	public void addAllConferenceGames() throws Exception {
		scheduleService.addAllConferenceGames();
	}

	@GetMapping()
	public List<Game> getSchedule() {
		return scheduleService.getSeasonSchedule();
	}

	@PutMapping()
	public void setSchedule(List<Game> seasonSchedule) {
		scheduleService.setSeasonSchedule(seasonSchedule);
	}

	@PostMapping(value = "game/{week}/{gameNumber}")
	public void saveGame(@RequestBody AddGameRequest addGameRequest, @PathVariable int week,
			@PathVariable int gameNumber) {
		scheduleService.saveGame(addGameRequest, week, gameNumber);
	}

	@PostMapping(value = "swap-schedule/{tgid1}/{tgid2}")
	public void swapSchedule(@PathVariable int tgid1, @PathVariable int tgid2) {
		scheduleService.swapSchedule(tgid1, tgid2);
	}
}
