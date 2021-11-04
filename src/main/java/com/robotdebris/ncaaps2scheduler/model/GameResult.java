package com.robotdebris.ncaaps2scheduler.model;

public class GameResult {
	
	int awayScore = 0;
	int homeScore = 0;
	int ot = 0;
	
	public GameResult(int awayScore, int homeScore, int ot) {
		this.awayScore = awayScore;
		this.homeScore = homeScore;
		this.ot = ot;
	}
	public int getAwayScore() {
		return awayScore;
	}
	public void setAwayScore(int awayScore) {
		this.awayScore = awayScore;
	}
	public int getHomeScore() {
		return homeScore;
	}
	public void setHomeScore(int homeScore) {
		this.homeScore = homeScore;
	}
	public int getOt() {
		return ot;
	}
	public void setOt(int ot) {
		this.ot = ot;
	}
}
