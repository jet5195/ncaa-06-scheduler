package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConferenceSchedulerFactory {

    private final GameRepository gameRepository;
    private final ScheduleService scheduleService;

    @Autowired
    public ConferenceSchedulerFactory(GameRepository gameRepository, ScheduleService scheduleService) {
        this.gameRepository = gameRepository;
        this.scheduleService = scheduleService;
    }

    public static ConferenceScheduler getScheduler(Conference conf) {
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
                return new TenOrFewerTeamConferenceScheduler();
            case 11:
                return new ElevenTeamConferenceScheduler();
            case 12:
                return new TwelveTeamConferenceScheduler();
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
