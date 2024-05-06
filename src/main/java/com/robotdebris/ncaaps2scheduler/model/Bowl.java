package com.robotdebris.ncaaps2scheduler.model;

import org.springframework.stereotype.Component;

@Component
public class Bowl {

    public Bowl() {
    }

    public Bowl(int conference1Id, int conference1Rank, int conference2Id, int conference2Rank, int bmfd, int stadiumId, int trophyId, int time, String bowlName, int gameNumber, int bowlMonth, int week, int bowlLogo, int bplo, int bowlIndex, int day) {
        this.conference1Id = conference1Id;
        this.conference1Rank = conference1Rank;
        this.conference2Id = conference2Id;
        this.conference2Rank = conference2Rank;
        this.bmfd = bmfd;
        this.stadiumId = stadiumId;
        this.trophyId = trophyId;
        this.time = time;
        this.bowlName = bowlName;
        this.gameNumber = gameNumber;
        this.bowlMonth = bowlMonth;
        this.week = week;
        this.bowlLogo = bowlLogo;
        this.bplo = bplo;
        this.bowlIndex = bowlIndex;
        this.day = day;
    }

    private int conference1Id;//bci1
    private int conference1Rank;//bcr1
    private int conference2Id;//bci2
    private int conference2Rank;//bcr2
    private int bmfd;
    private int stadiumId;//sgid, I think I'm just manually calculating this for all reg. season games
    private int trophyId;//utid
    private int time;//gtod 750 = 12:30 pm, 930 = 3:30 pm, 1080 = 6:00 pm, 1200 = 8:00 pm,
    private String bowlName; //bnme
    private int gameNumber; //sgnm, must be unique per week, highest num I've seen is 55
    private int bowlMonth; //bmon
    private int week; //sewn
    private int bowlLogo; //blgo?? not sure
    private int bplo;
    private int bowlIndex; //bidx
    private int day; //bday

    public int getConference1Id() {
        return conference1Id;
    }

    public void setConference1Id(int conference1Id) {
        this.conference1Id = conference1Id;
    }

    public int getConference1Rank() {
        return conference1Rank;
    }

    public void setConference1Rank(int conference1Rank) {
        this.conference1Rank = conference1Rank;
    }

    public int getConference2Id() {
        return conference2Id;
    }

    public void setConference2Id(int conference2Id) {
        this.conference2Id = conference2Id;
    }

    public int getConference2Rank() {
        return conference2Rank;
    }

    public void setConference2Rank(int conference2Rank) {
        this.conference2Rank = conference2Rank;
    }

    public int getBmfd() {
        return bmfd;
    }

    public void setBmfd(int bmfd) {
        this.bmfd = bmfd;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public int getTrophyId() {
        return trophyId;
    }

    public void setTrophyId(int trophyId) {
        this.trophyId = trophyId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getBowlName() {
        return bowlName;
    }

    public void setBowlName(String bowlName) {
        this.bowlName = bowlName;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    public int getBowlMonth() {
        return bowlMonth;
    }

    public void setBowlMonth(int bowlMonth) {
        this.bowlMonth = bowlMonth;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getBowlLogo() {
        return bowlLogo;
    }

    public void setBowlLogo(int bowlLogo) {
        this.bowlLogo = bowlLogo;
    }

    public int getBplo() {
        return bplo;
    }

    public void setBplo(int bplo) {
        this.bplo = bplo;
    }

    public int getBowlIndex() {
        return bowlIndex;
    }

    public void setBowlIndex(int bowlIndex) {
        this.bowlIndex = bowlIndex;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public boolean equals(Object obj) {
        // Check if the obj parameter is null or not an instance of MyClass
        if (obj == null || !(obj instanceof Bowl)) {
            return false;
        }
        Bowl otherBowl = (Bowl) obj;
        if (this.getConference1Id() == otherBowl.getConference1Id() && this.getConference2Id() == otherBowl.getConference2Id()
                && this.getConference1Rank() == otherBowl.getConference1Rank() && this.getConference2Rank() == otherBowl.getConference2Rank()
                && this.getBmfd() == otherBowl.getBmfd() && this.getStadiumId() == otherBowl.getStadiumId() && this.getTrophyId() == otherBowl.getTrophyId()
                && this.getTime() == otherBowl.getTime() && this.getBowlName().equals(otherBowl.getBowlName()) && this.getGameNumber() == otherBowl.getGameNumber()
                && this.getBowlMonth() == otherBowl.getBowlMonth() && this.getWeek() == otherBowl.getWeek() && this.getBowlLogo() == otherBowl.getBowlLogo()
                && this.getBplo() == otherBowl.getBplo() && this.getBowlIndex() == otherBowl.getBowlIndex() && this.getDay() == otherBowl.getDay()) {
            return true;
        } else return false;
    }
}
