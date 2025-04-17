package com.robotdebris.ncaaps2scheduler.model.playoff;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Playoff {
    private List<PlayoffGame> playoffGames;

    public Playoff(List<PlayoffGame> playoffGames) {
        this.playoffGames = playoffGames;
    }

    public Playoff() {
    }

    public List<PlayoffGame> getPlayoffGames() {
        return playoffGames;
    }

    public void setPlayoffGames(List<PlayoffGame> playoffGames) {
        this.playoffGames = playoffGames;
    }
}
