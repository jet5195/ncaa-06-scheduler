package com.robotdebris.ncaaps2scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class Bowl {

    @Getter
    @Setter
    private int conference1Id;//bci1
    @Getter
    @Setter
    private int conference1Rank;//bcr1
    @Getter
    @Setter
    private int conference2Id;//bci2
    @Getter
    @Setter
    private int conference2Rank;//bcr2
    @Getter
    @Setter
    private int bmfd;
    @Getter
    @Setter
    private int stadiumId;//sgid, I think I'm just manually calculating this for all reg. season games
    @Getter
    @Setter
    private int trophyId;//utid
    @Getter
    @Setter
    private int time;//gtod 750 = 12:30 pm, 930 = 3:30 pm, 1080 = 6:00 pm, 1200 = 8:00 pm,
    @Getter
    @Setter
    private String bowlName; //bnme
    @Getter
    @Setter
    private int gameNumber; //sgnm, must be unique per week, highest num I've seen is 55
    @Getter
    @Setter
    private int bowlMonth; //bmon
    @Getter
    @Setter
    private int week; //sewn
    @Getter
    @Setter
    private int bowlLogo; //blgo?? not sure
    @Getter
    @Setter
    private int bplo;
    @Getter
    @Setter
    private int bowlIndex; //bidx
    @Getter
    @Setter
    private int day; //bday

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
