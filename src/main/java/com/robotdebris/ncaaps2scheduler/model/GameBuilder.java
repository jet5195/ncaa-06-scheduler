package com.robotdebris.ncaaps2scheduler.model;

public class GameBuilder {

	private School awayTeam;
	private School homeTeam;
	private GameResult gameResult;
	private int time;
	private DayOfWeek day;
	private int conferenceGame;
	private int week;
	private int weight;
	private int userGame;
	private int gameNumber;

	private static final int DEFAULT_TIME = 450; // Example default time
	private static final DayOfWeek DEFAULT_DAY = DayOfWeek.SATURDAY; // Example default day

	public GameBuilder setAwayTeam(School awayTeam) {
		this.awayTeam = awayTeam;
		return this;
	}

	public GameBuilder setHomeTeam(School homeTeam) {
		this.homeTeam = homeTeam;
		return this;
	}

	public GameBuilder setGameResult(GameResult gameResult) {
		this.gameResult = gameResult;
		return this;
	}

	public GameBuilder setTime(int time) {
		this.time = time;
		return this;
	}

	public GameBuilder setDay(DayOfWeek day) {
		this.day = day;
		return this;
	}

	public GameBuilder setConferenceGame(int conferenceGame) {
		this.conferenceGame = conferenceGame;
		return this;
	}

	public GameBuilder setWeek(int week) {
		this.week = week;
		return this;
	}

	public GameBuilder setWeight(int weight) {
		this.weight = weight;
		return this;
	}

	public GameBuilder setUserGame(int userGame) {
		this.userGame = userGame;
		return this;
	}

	public GameBuilder setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
		return this;
	}

	public GameBuilder setTeamsWithYearlyRotation(School school1, School school2, int year) {
		if (year % 2 == 0) {
			this.awayTeam = school1;
			this.homeTeam = school2;
		} else {
			this.homeTeam = school1;
			this.awayTeam = school2;
		}
		return this;
	}

	/**
	 * Sets the home and away teams intelligently based on certain conditions. If
	 * the teams are rivals or are both in power conferences (or both not in power
	 * conferences), the home team is randomly chosen. Otherwise, the home team is
	 * given to the power conference strength.
	 *
	 * @param s1 The first school.
	 * @param s2 The second school.
	 * @return The updated GameBuilder instance.
	 */
	public GameBuilder setTeamsWithRandomHomeIntelligently(School s1, School s2) {
		boolean shouldAllowEitherHome = s1.isRival(s2)
				|| s1.getConference().isPowerConf() == s2.getConference().isPowerConf();
		int random = (int) (Math.random() * 2) + 1; // Random value: 1 or 2

		if (shouldAllowEitherHome) {
			if (random == 1) {
				this.setHomeTeam(s2);
				this.setAwayTeam(s1);
			} else {
				this.setHomeTeam(s1);
				this.setAwayTeam(s2);
			}
		} else {
			if (s1.getConference().isPowerConf()) {
				this.setHomeTeam(s1);
				this.setAwayTeam(s2);
			} else {
				this.setHomeTeam(s2);
				this.setAwayTeam(s1);
			}
		}
		return this;
	}

	public Game build() {
		// Handle default values
		if (this.time == 0) { // Assuming 0 is the uninitialized value for time
			this.time = DEFAULT_TIME;
		}
		if (this.day == null) {
			this.day = DEFAULT_DAY;
		}
		// Create the Game object with the builder's field values
		return new Game(gameResult, time, awayTeam, homeTeam, gameNumber, week, day, userGame, conferenceGame);
		// TODO: either remove weight from this class or add to
		// constructor above
	}

}
