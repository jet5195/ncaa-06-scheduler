package com.robotdebris.ncaaps2scheduler.model;
//@AllArgsConstructor
//@NoArgsConstructor
//@Component
public class PlayoffGame extends Game {

	private int round;
//    @Getter
//    @Setter
//    private int bowlGameId;

	public PlayoffGame(Game game, int round) {
		super(game.getGameResult(), game.getTime(), game.getAwayTeam(), game.getHomeTeam(), game.getGameNumber(),
				game.getWeek(), game.getDay(), game.isUserGame(), game.isConferenceGame());
		this.round = round;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
}
