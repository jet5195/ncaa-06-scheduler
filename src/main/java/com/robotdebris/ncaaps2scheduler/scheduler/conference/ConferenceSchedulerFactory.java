package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;

@Component
public class ConferenceSchedulerFactory {

	@Autowired
	private ApplicationContext context;

	public ConferenceScheduler getScheduler(Conference conf) {
		int numSchools = conf.getNumOfSchools();
		switch (numSchools) {
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return context.getBean(TenOrFewerTeamConferenceScheduler.class);
		case 11:
			return context.getBean(TenOrFewerTeamConferenceScheduler.class);
		case 12:
			return context.getBean(TwelveTeamConferenceScheduler.class);
		case 13:
			return new ThirteenTeamConferenceScheduler();
		case 14:
			return new FourteenTeamConferenceScheduler();
		case 15:
			return new FifteenTeamConferenceScheduler();
		case 16:
			return new SixteenTeamConferenceScheduler();
		default:
			throw new IllegalArgumentException("Unsupported number of teams");
		}
	}
}
