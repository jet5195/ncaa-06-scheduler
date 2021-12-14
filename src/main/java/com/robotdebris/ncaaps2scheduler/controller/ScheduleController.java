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
	public int removeAllOocGames(){
		return scheduleService.removeAllOocGames();
	}

	@PostMapping(value = "remove-all-ooc-games-but-rivalry")
	public int removeAllOocNonRivalGames(){
		return scheduleService.removeAllOocNonRivalGames();
	}

	@PostMapping(value = "remove-all-fcs-games")
	//return count of removed games
	public int removeAllFcsGames(){
		return scheduleService.removeAllFcsGames();
	}

	//Change this to use a RequestGame object that doesn't exist yet
	@PostMapping(value = "add-game")
	public void addGame(@RequestBody AddGameRequest addGameRequest){
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
	
	@PostMapping(value = "save-to-file")
	public void saveToFile() {
		scheduleService.saveToFile();
	}
	
	@GetMapping(value = "download")
	public void downloadSchedule(HttpServletResponse response) throws IOException {
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=new_sched.xlsx");
        ByteArrayInputStream stream = scheduleService.downloadSchedule();
        IOUtils.copy(stream, response.getOutputStream());
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
	public SeasonSchedule getBowlGames() {
		return scheduleService.getBowlGames();
	}
	
	@GetMapping(value = "week/{week}/{gameNumber}")
	public Game getGame(@PathVariable("week") int week, @PathVariable("gameNumber") int gameNumber) {
		return scheduleService.getGame(week, gameNumber);
	}
	
//	@PostMapping(value = "game")
//	public void saveGame(@RequestBody Game game) {
//		scheduleService.saveGame(game);
//	}
}
