package com.robotdebris.ncaaps2scheduler.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;

@Repository
public interface GameRepository {

	List<Game> findAll();

	Game findById();

	void saveAll(List<Game> seasonSchedule);

	void saveGame(Game game);

	void removeGame(Game game);

	void removeGames(List<Game> games);

	List<Game> findGamesByTeam(School school);

	int getYear();

	void setYear(int year);

	void removeAll();

	Optional<Game> findGameByTeams(School school1, School school2);

	Optional<Game> findByTeamAndWeek(School school, int week);

	List<Game> findConfGamesByConference(Conference conference);

}
