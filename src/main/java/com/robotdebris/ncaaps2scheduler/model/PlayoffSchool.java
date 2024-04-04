package com.robotdebris.ncaaps2scheduler.model;

import lombok.Getter;
import lombok.Setter;

//@AllArgsConstructor
//@NoArgsConstructor
//@Component
public class PlayoffSchool extends School {
    @Getter
    @Setter
    private int seed;

    private PlayoffSchool(School school, int seed) {
        super();
    }

    public static PlayoffSchool createPlayoffSchool(School school, int seed) {
        return new PlayoffSchool(school, seed);
    }
}
