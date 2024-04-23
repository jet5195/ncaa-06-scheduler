package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;

@Component
public class TenOrFewerTeamConferenceScheduler extends AbstractConferenceScheduler {

	@Override
	public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) {
		try {
			scheduleRoundRobinConfGames(conference);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
