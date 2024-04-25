package com.robotdebris.ncaaps2scheduler;

import java.util.List;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;

public class NoWeeksAvailableException extends RuntimeException {

	public NoWeeksAvailableException(School s1) {

		super("No available weeks found for scheduling " + s1.getName()
				+ " . Make sure you removed all non-conference games before" + " setting conference schedule.");
	}

	public NoWeeksAvailableException(Conference c1) {

		super("No available weeks found for scheduling " + c1.getName()
				+ " . Make sure you removed all non-conference games before" + " setting conference schedule.");
	}

	public NoWeeksAvailableException(School s1, School s2, List<Game> s1Schedule, List<Game> s2Schedule) {
		super("No available weeks found for scheduling " + s1 + " vs " + s2
				+ ". Make sure you removed all non-conference games before" + " setting conference schedule ." + s1
				+ "schedule = " + s1Schedule + "./n " + s2 + " schedule " + s2Schedule);
	}
}