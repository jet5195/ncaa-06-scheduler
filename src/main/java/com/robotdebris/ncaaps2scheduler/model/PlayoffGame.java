package com.robotdebris.ncaaps2scheduler.model;

import lombok.Getter;
import lombok.Setter;

//@AllArgsConstructor
//@NoArgsConstructor
//@Component
public class PlayoffGame extends Game {

	@Getter
	@Setter
	private int round;
//    @Getter
//    @Setter
//    private int bowlGameId;

	public PlayoffGame(Game game, int round) {
		super(game.getGameResult(), game.getTime(), game.getAwayTeam(), game.getHomeTeam(), game.getGameNumber(),
				game.getWeek(), game.getDay(), game.isUserGame(), game.isConferenceGame());
		this.round = round;
	}
}
