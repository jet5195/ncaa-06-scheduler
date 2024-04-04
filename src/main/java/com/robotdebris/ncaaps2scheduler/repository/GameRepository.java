package com.robotdebris.ncaaps2scheduler.repository;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository {

    List<Game> findAll();

    Game findById();

    void saveAll(List<Game> seasonSchedule);

    void saveGame(Game game);

    void removeGame(Game game);

    List<Game> findGamesByTeam(School school);

    int getYear();

    void setYear(int year);

}
