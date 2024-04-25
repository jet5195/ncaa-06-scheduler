package com.robotdebris.ncaaps2scheduler.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robotdebris.ncaaps2scheduler.model.PlayoffGame;
import com.robotdebris.ncaaps2scheduler.model.PlayoffSchool;
import com.robotdebris.ncaaps2scheduler.service.PlayoffService;

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
