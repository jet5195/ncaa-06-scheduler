package com.robotdebris.ncaaps2scheduler.model;

import java.util.LinkedList;

public class SchoolSchedule extends LinkedList<Game> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6360083104312942837L;

	/**
	 * Finds a game in a school's schedule for a given week
	 * 
	 * @param week the week to search for
	 * @return a game of the given week, null if no game is found
	 */
	public Game getGame(int week) {
		for (Game game : this) {
			if (game.getWeek() == week) {
				return game;
			}
		}
		return null;
	}

	public boolean isSchoolScheduleFull() {
		return this.size() >= 12;
	}
}