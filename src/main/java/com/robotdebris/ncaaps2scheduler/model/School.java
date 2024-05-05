package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.robotdebris.ncaaps2scheduler.serializer.SchoolIdDeserializer;
import com.robotdebris.ncaaps2scheduler.serializer.SchoolIdSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;
import java.util.Objects;

@Entity
public class School implements Comparable<School> {

    // TEAM tscs = conference ranking
//0 = conf champ
//TBRK = bcs bowl ranking
//tmrk = media poll rank
    @Id
    private int tgid;
    private String name;
    private String nickname;
    private String conferenceName;
    @JsonBackReference
    private Conference conference;
    @JsonBackReference
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

    public School() {
    }

    public int getTgid() {
        return tgid;
    }

    public void setTgid(int tgid) {
        this.tgid = tgid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConferenceName() {
        return conferenceName;
    }

    public void setConferenceName(String conferenceName) {
        this.conferenceName = conferenceName;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
        this.conferenceName = conference.getName();
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAltColor() {
        return altColor;
    }

    public void setAltColor(String altColor) {
        this.altColor = altColor;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<School> getRivals() {
        return rivals;
    }

    public void setRivals(List<School> rivals) {
        this.rivals = rivals;
    }

    public boolean isUserTeam() {
        return userTeam;
    }

    public void setUserTeam(boolean userTeam) {
        this.userTeam = userTeam;
    }

    public School getxDivRival() {
        return xDivRival;
    }

    public void setxDivRival(School xDivRival) {
        this.xDivRival = xDivRival;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getStadiumCapacity() {
        return stadiumCapacity;
    }

    public void setStadiumCapacity(double stadiumCapacity) {
        this.stadiumCapacity = stadiumCapacity;
    }

    /**
     * Checks to see if this school is in the same conference as another
     *
     * @param school the school you are checking against
     * @return true if in the same conference, false if else
     */
    public boolean isInConference(School school) {
        if (this.getConference() == null || this.getConference().getName().equalsIgnoreCase("Independent")) {
            return false;
        } else if (school.getConference() == null) {
            return false;
        } else
            return this.getConference().getName().equalsIgnoreCase(school.getConference().getName());
    }

    /**
     * Prints the schedule of a school
     */
//	public void printSchedule() {
//		int i = 0;
//		int lastWeek = -1;
//		while (i < this.getSchedule().size()) {
//			int nextWeek = 100;// random high number
//			for (int j = 0; j < this.getSchedule().size(); j++) {
//				if (this.getSchedule().get(j).getWeek() < nextWeek && this.getSchedule().get(j).getWeek() > lastWeek) {
//					nextWeek = this.getSchedule().get(j).getWeek();
//				}
//			}
//
//			System.out.print(i + 1 + ". ");
//			Game game = this.getSchedule().getGame(nextWeek);
//			System.out.print(this);
//			if (this.getTgid() == game.getHomeTeam().getTgid()) {
//				System.out.print(" vs " + game.getAwayTeam());
//			} else {
//				System.out.print(" at " + game.getHomeTeam());
//			}
//			System.out.println(" (week " + (nextWeek + 1) + ")");
//			i++;
//			lastWeek = nextWeek;
//		}
//	}

    /**
     * Returns true if opponent is a rival, false if else
     *
     * @param opponent the opponent
     * @return true if opponent is a rival, false if else
     */
    public boolean isRival(School opponent) {
        for (int i = 0; this.getRivals() != null && i < this.getRivals().size(); i++) {
            if (this.getRivals().get(i).getName().equals(opponent.getName())) {
                return true;
            }
        }
        for (int i = 0; this.getRivals() != null && i < opponent.getRivals().size(); i++) {
            if (opponent.getRivals().get(i).getName().equals(this.getName())) {
                return true;
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
        this.division = division;
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
            return school;
        }
    }
}
