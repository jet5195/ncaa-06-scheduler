package com.robotdebris.ncaaps2scheduler.model;

public class SuggestedGameResponse {
	
	private int week;
	
	private School opponent;
	
	private boolean homeGame;
	
	public SuggestedGameResponse(int week, School opponent, boolean homeGame) {
		this.week = week;
		this.opponent = opponent;
		this.homeGame = homeGame;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public School getOpponent() {
		return opponent;
	}

	public void setOpponent(School opponent) {
		this.opponent = opponent;
	}

	public boolean isHomeGame() {
		return homeGame;
	}

	public void setHomeGame(boolean homeGame) {
		this.homeGame = homeGame;
	}
	
	
	
}
