package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.robotdebris.ncaaps2scheduler.configuration.AppConstants;
import com.robotdebris.ncaaps2scheduler.serializer.SchoolIdDeserializer;
import com.robotdebris.ncaaps2scheduler.serializer.SchoolIdSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter

@NoArgsConstructor
@Entity
public class School implements Comparable<School> {

    // TEAM tscs = conference ranking
    // 0 = conf champ
    // TBRK = bcs bowl ranking
    // tmrk = media poll rank
    @Id
    //TGID
    private int tgid;
    private String name;
    //TDNA
    private String nickname;
    //CGID
    private Integer conferenceId;
    @JsonBackReference("conference-schools-ref")
    private Conference conference;
    //DGID
    private Integer divisionId;
    @JsonBackReference("division-schools-ref")
    private Division division;
    // data we can pull from collegefootballdata api
    private String color;
    private String altColor;
    private String logo;
    private double latitude;
    private double longitude;
    private String abbreviation;
    private String stadiumName;
    private String state;
    private String city;
    private double stadiumCapacity;
    @JsonSerialize(using = SchoolIdSerializer.class)
    @JsonDeserialize(contentUsing = SchoolIdDeserializer.class)
    private List<School> rivals;
    private boolean userTeam;
    @JsonSerialize(using = SchoolIdSerializer.class)
    @JsonDeserialize(using = SchoolIdDeserializer.class)
    private School xDivRival;

    private int prestige;

    private int stadiumId;

    public void addRival(School school) {
        if (this.rivals == null) {
            this.rivals = new ArrayList<>();
        }
        this.rivals.add(school);
    }

    /**
     * Checks to see if this school is in the same conference as another
     *
     * @param school the school you are checking against
     * @return true if in the same conference, false if else
     */
    public boolean isInConference(School school) {
        if (this.getConference() == null || AppConstants.INDEPENDENT_STRINGS.contains(this.getConference().getName())) {
            return false;
        } else if (school.getConference() == null) {
            return false;
        } else
            return this.getConference().getName().equalsIgnoreCase(school.getConference().getName());
    }

    /**
     * Prints the schedule of a school
     */
    // public void printSchedule() {
    // int i = 0;
    // int lastWeek = -1;
    // while (i < this.getSchedule().size()) {
    // int nextWeek = 100;// random high number
    // for (int j = 0; j < this.getSchedule().size(); j++) {
    // if (this.getSchedule().get(j).getWeek() < nextWeek &&
    // this.getSchedule().get(j).getWeek() > lastWeek) {
    // nextWeek = this.getSchedule().get(j).getWeek();
    // }
    // }
    //
    // System.out.print(i + 1 + ". ");
    // Game game = this.getSchedule().getGame(nextWeek);
    // System.out.print(this);
    // if (this.getTgid() == game.getHomeTeam().getTgid()) {
    // System.out.print(" vs " + game.getAwayTeam());
    // } else {
    // System.out.print(" at " + game.getHomeTeam());
    // }
    // System.out.println(" (week " + (nextWeek + 1) + ")");
    // i++;
    // lastWeek = nextWeek;
    // }
    // }

    /**
     * Returns true if the opponent is a rival, false otherwise.
     *
     * @param opponent the opponent
     * @return true if the opponent is a rival, false otherwise
     */
    public boolean isRival(School opponent) {
        if (this.getRivals() != null) {
            for (School rival : this.getRivals()) {
                if (rival.getName().equals(opponent.getName())) {
                    return true;
                }
            }
        }

        if (opponent.getRivals() != null) {
            for (School rival : opponent.getRivals()) {
                if (rival.getName().equals(this.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public void updateAlignment(Conference conference, Division division, School xDivRival) {
        this.setConference(conference);
        this.setDivision(division);
        this.xDivRival = xDivRival;
    }

    @Override
    public int compareTo(School o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true; // Same reference, so they are equal
        }
        if (o == null || getClass() != o.getClass()) {
            return false; // Different classes or null, not equal
        }
        School otherTeam = (School) o;
        return tgid == otherTeam.tgid; // Compare teamIds
    }

    @Override
    public int hashCode() {
        return Objects.hash(tgid);
    }

    // The builder static inner class
    public static class Builder {

        private int tgid;
        private String name;
        private String nickname;
        private String state;
        private Conference conference;
        private Division division;
        private String color;
        private String altColor;
        private String logo;
        private List<School> rivals;
        private boolean userTeam;
        private School xDivRival;
        private double latitude;
        private double longitude;
        private String abbreviation;
        private String stadiumName;
        private String city;
        private double stadiumCapacity;
        // Other fields...

        public Builder() {
            // Initialize with default values if necessary
        }

        public Builder withTgid(int tgid) {
            this.tgid = tgid;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public Builder withConference(Conference conference) {
            this.conference = conference;
            return this;
        }

        public Builder withDivision(Division division) {
            this.division = division;
            return this;
        }

        public Builder withColor(String color) {
            this.color = color;
            return this;
        }

        public Builder withAltColor(String altColor) {
            this.altColor = altColor;
            return this;
        }

        public Builder withLogo(String logo) {
            this.logo = logo;
            return this;
        }

        public Builder withRivals(List<School> rivals) {
            this.rivals = rivals;
            return this;
        }

        public Builder isUserTeam(boolean userTeam) {
            this.userTeam = userTeam;
            return this;
        }

        public Builder withXDivRival(School xDivRival) {
            this.xDivRival = xDivRival;
            return this;
        }

        public Builder withLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withAbbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
            return this;
        }

        public Builder withStadiumName(String stadiumName) {
            this.stadiumName = stadiumName;
            return this;
        }

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withStadiumCapacity(double stadiumCapacity) {
            this.stadiumCapacity = stadiumCapacity;
            return this;
        }

        public School build() {
            School school = new School();
            school.tgid = this.tgid;
            school.name = this.name;
            school.nickname = this.nickname;
            school.state = this.state;
            school.conference = this.conference;
            school.division = this.division;
            school.color = this.color;
            school.altColor = this.altColor;
            school.logo = this.logo;
            school.rivals = this.rivals;
            school.userTeam = this.userTeam;
            school.xDivRival = this.xDivRival;
            school.latitude = this.latitude;
            school.longitude = this.longitude;
            school.abbreviation = this.abbreviation;
            school.stadiumName = this.stadiumName;
            school.city = this.city;
            school.stadiumCapacity = this.stadiumCapacity;
            return school;
        }
    }
}
