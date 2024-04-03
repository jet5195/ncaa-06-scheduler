package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;

public interface ConferenceScheduler {
	void generateConferenceSchedule(Conference conference);
}
