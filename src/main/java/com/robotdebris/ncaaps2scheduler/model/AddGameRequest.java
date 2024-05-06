package com.robotdebris.ncaaps2scheduler.model;

public class AddGameRequest {

	public AddGameRequest(int awayId, int homeId, int week, int time, DayOfWeek day, GameResult gameResult) {
		this.awayId = awayId;
		this.homeId = homeId;
		this.week = week;
		this.time = time;
		this.day = day;
		this.gameResult = gameResult;
	}

	private int awayId;

	private int homeId;

	private int week;

	private int time;

	private DayOfWeek day;

	private GameResult gameResult;

	public int getAwayId() {
		return awayId;
	}

	public void setAwayId(int awayId) {
		this.awayId = awayId;
	}

	public int getHomeId() {
		return homeId;
	}

	public void setHomeId(int homeId) {
		this.homeId = homeId;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public GameResult getGameResult() {
		return gameResult;
	}

	public void setGameResult(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	public DayOfWeek getDay() {
		return day;
	}

	public void setDay(DayOfWeek day) {
		this.day = day;
	}

}
