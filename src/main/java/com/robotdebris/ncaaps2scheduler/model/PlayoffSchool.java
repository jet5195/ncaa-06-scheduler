package com.robotdebris.ncaaps2scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

//@AllArgsConstructor
//@NoArgsConstructor
//@Component
public class PlayoffSchool extends School {
    @Getter
    @Setter
    private int seed;

    public PlayoffSchool(School school, int seed) {
        super(school.getTgid(), school.getName(), school.getNickname(), school.getState(), school.getConference(), school.getDivision(), school.getNcaaDivision(), school.getColor(), school.getAltColor(), school.getLogo(), school.getRivals(), school.isUserTeam(), school.getSchedule(), school.getXDivRival());
        this.seed = seed;
    }
}
