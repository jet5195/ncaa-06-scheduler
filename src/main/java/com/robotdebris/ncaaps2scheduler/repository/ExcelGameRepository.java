package com.robotdebris.ncaaps2scheduler.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;

public class ExcelGameRepository implements GameRepository {

	List<Game> seasonSchedule = new ArrayList<>();

	@Override
	public List<Game> findAll() {
		return seasonSchedule;
	}

	@Override
	public Game findById() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveAll(List<Game> seasonSchedule) {
		this.seasonSchedule = seasonSchedule;
	}

	@Override
	public void saveGame(Game game) {
		// TODO: figure out how to handle adding vs replacing
		seasonSchedule.add(game);
	}

	@Override
	public void removeGame(Game game) {
		seasonSchedule.remove(game);
	}

	@Override
	public List<Game> findGamesByTeam(School school) {
		return seasonSchedule.stream().filter(game -> game.involvesTeam(school))
				.sorted(Comparator.comparing(Game::getWeek)).collect(Collectors.toList());
	}
}
