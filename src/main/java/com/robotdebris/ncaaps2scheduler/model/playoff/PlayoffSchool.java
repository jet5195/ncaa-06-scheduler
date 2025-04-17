package com.robotdebris.ncaaps2scheduler.model.playoff;

import com.robotdebris.ncaaps2scheduler.model.School;

public class PlayoffSchool extends School {
    private int seed;

    private PlayoffSchool(School school, int seed) {
        super();
    }

    public static PlayoffSchool createPlayoffSchool(School school, int seed) {
        return new PlayoffSchool(school, seed);
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
}
