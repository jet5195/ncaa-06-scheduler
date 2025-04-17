package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.playoff.PlayoffGame;
import com.robotdebris.ncaaps2scheduler.model.playoff.PlayoffSchool;
import com.robotdebris.ncaaps2scheduler.service.PlayoffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("playoffs")
public class PlayoffController {

    @Autowired
    private PlayoffService playoffService;

    @GetMapping(value = "/schools")
    public List<PlayoffSchool> getPlayoffSchools() {
        return playoffService.getPlayoffTeams();
    }

    @PutMapping(value = "/schools")
    public void setPlayoffTeams12(@RequestBody List<Integer> tgids) {
        playoffService.setPlayoffTeams(tgids);
    }

    @GetMapping(value = "/games")
    public List<PlayoffGame> getPlayoffGames() {
        return playoffService.getPlayoffGamesAndPlayoffSchools();
    }

    @PostMapping(value = "/schedule-next-round")
    public void scheduleNextRound() {
        playoffService.scheduleNextRound();
    }
}
