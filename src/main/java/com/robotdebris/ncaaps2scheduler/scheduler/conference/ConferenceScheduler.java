package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;

public interface ConferenceScheduler {

    void generateConferenceSchedule(Conference conference, GameRepository gameRepository);
}
