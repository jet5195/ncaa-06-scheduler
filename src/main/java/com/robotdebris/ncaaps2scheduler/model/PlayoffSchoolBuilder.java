package com.robotdebris.ncaaps2scheduler.model;

public class PlayoffSchoolBuilder {
    private School school;
    private int seed;

    public PlayoffSchoolBuilder setSchool(School school) {
        this.school = school;
        return this;
    }

    public PlayoffSchoolBuilder setSeed(int seed) {
        this.seed = seed;
        return this;
    }

    public PlayoffSchool createPlayoffSchool() {
        return PlayoffSchool.createPlayoffSchool(school, seed);
    }
}