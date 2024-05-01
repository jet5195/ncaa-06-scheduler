package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.AddGameRequest;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schedule")
public class ScheduleController {

    @Autowired
    private SchoolService schoolService;

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
        return scheduleService.removeAllNonConferenceGames(true);
    }

    @PostMapping(value = "remove-all-ooc-games-but-rivalry")
    public int removeAllOocNonRivalGames() {
        return scheduleService.removeAllNonConferenceGames(false);
    }

    @PostMapping(value = "remove-all-fcs-games")
    // return count of removed games
    public int removeAllFcsGames() {
        return scheduleService.removeAllFcsGames();
    }

    @PostMapping(value = "add-game")
    public void addGame(@RequestBody AddGameRequest addGameRequest) {
        Game game = new GameBuilder().setAwayTeam(schoolService.schoolSearch(addGameRequest.getAwayId()))
                .setHomeTeam(schoolService.schoolSearch(addGameRequest.getHomeId())).setWeek(addGameRequest.getWeek())
                .setDay(addGameRequest.getDay()).setGameResult(addGameRequest.getGameResult()).build();
        scheduleService.addGame(game);
    }

    @PostMapping(value = "auto-add-games")
    public int autoAddGames() {
        int count = scheduleService.getSeasonSchedule().size();
        scheduleService.autoAddGames(false);
        return scheduleService.getSeasonSchedule().size() - count;
    }

    @PostMapping(value = "auto-add-games-aggressive")
    public int autoAddGamesAgressive() {
        int count = scheduleService.getSeasonSchedule().size();
        scheduleService.autoAddGames(true);
        return scheduleService.getSeasonSchedule().size() - count;
    }

    @PostMapping(value = "auto-add-games-rivals")
    public int autoAddRivalries() {
        int count = scheduleService.getSeasonSchedule().size();
        scheduleService.addRivalryGamesAll(schoolService.getAllSchools(), false);
        return scheduleService.getSeasonSchedule().size() - count;
    }

    @PostMapping(value = "auto-add-games-random")
    public int autoAddGamesRandomly() {
        int count = scheduleService.getSeasonSchedule().size();
//		scheduleService.addRandomGames(schoolService.getAllSchools(), scheduleService.findTooFewGames());
        scheduleService.addRandomNonConfGames();
        return scheduleService.getSeasonSchedule().size() - count;
    }

    @PostMapping(value = "remove-all-games")
    public void removeAllGames() {
        scheduleService.removeAllGames();
    }

    @PostMapping(value = "fix")
    public int fixSchedule() {
        int count = scheduleService.getSeasonSchedule().size();
        scheduleService.fixSchedule();
        return scheduleService.getSeasonSchedule().size() - count;
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
    public ArrayList<Game> getScheduleByWeek(@PathVariable int week) {
        return scheduleService.getScheduleByWeek(week);
    }

    @GetMapping(value = "bowl-games")
    public List<Game> getBowlGames() {
        return scheduleService.getBowlGames();
    }

    @GetMapping(value = "week/{week}/{gameNumber}")
    public Game getGame(@PathVariable int week, @PathVariable int gameNumber) {
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
