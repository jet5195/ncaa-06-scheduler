package com.robotdebris.ncaaps2scheduler.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;

@Repository
public interface GameRepository {

	List<Game> findAll();

	Game findById();

	void saveAll(List<Game> seasonSchedule);

	void saveGame(Game game);

	void removeGame(Game game);

	List<Game> findGamesByTeam(School school);

}
