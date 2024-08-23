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
        int numSchools = conf.getSchools().size();
        return switch (numSchools) {
            case 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> context.getBean(TenOrFewerTeamConferenceScheduler.class);
            case 11 -> context.getBean(TenOrFewerTeamConferenceScheduler.class);
            case 12 -> context.getBean(TwelveTeamConferenceScheduler.class);
            case 13 -> new ThirteenTeamConferenceScheduler();
            case 14 -> context.getBean(FourteenTeamConferenceScheduler.class);
            case 15 -> new FifteenTeamConferenceScheduler();
            case 16 -> new SixteenTeamConferenceScheduler();
            default ->
                throw new IllegalArgumentException(conf.getName() + " unsupported number of teams: " + numSchools);
        };
    }
}
