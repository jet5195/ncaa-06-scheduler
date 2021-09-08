package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class School {
    private int tgid;
    private String name;
    private String nickname;
    private String state;
    private Conference conference;
    private String division;
    private String ncaaDivision; //fbs or fcs
    private String color;
    private String altColor;
    private String logo;
    @JsonIgnore
    private SchoolList rivals;
    private boolean userTeam;
    @JsonIgnore
    private SchoolSchedule schedule = new SchoolSchedule();

    public School(int tgid, String name, String nickname, String state, Conference conference, String division, String ncaaDivision, String color, String altColor, String logo) {
        this.tgid = tgid;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.conference = conference;
        this.division = division;
        this.ncaaDivision = ncaaDivision;
        this.color = color;
        this.altColor = altColor;
        this.logo = logo;
    }
    
    public School(int tgid, String name, String nickname, String state, String color, String altColor, String logo) {
        this.tgid = tgid;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.color = color;
        this.altColor = altColor;
        this.logo = logo;
    }

    public SchoolList getRivals() {
        return this.rivals;
    }

    public void setRivals(SchoolList rivals) {
        this.rivals = rivals;
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

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public String getNcaaDivision() {
        return ncaaDivision;
    }

    public void setNcaaDivision(String ncaaDivision) {
        this.ncaaDivision = ncaaDivision;
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

    public boolean isUserTeam() {
        return userTeam;
    }

    public void setUserTeam(boolean userTeam) {
        this.userTeam = userTeam;
    }

    public void addGame(Game theGame) {
        this.schedule.add(theGame);
    }

    public SchoolSchedule getSchedule() {
        return schedule;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    /**
     * Searches a school's schedule for a game that is not in conference or a rivalry game
     * @return Game that is removable (not a rivalry and not a conference game)
     */
    public Game findRemovableGame() {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            Game theGame = this.getSchedule().get(i);
            //0 means non-con
            if (theGame.isRemovableGame()) {
                return theGame;
            }
        }
        return null;
    }

    /**
     * Checks to see if this school is in the same conference as another
     * @param school the school you are checking against
     * @return true if in the same conference, false if else
     */
    public boolean isInConference(School school) {
        if(this.getConference().equals("Independent")) {
            return false;

        } else return this.getConference().getName().equalsIgnoreCase(school.getConference().getName());
    }

    /**
     * Checks to see if this school plays an opponent
     * @param school the opponent
     * @return true if these schools do play, false if else
     */
    public boolean isOpponent(School school) {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            if (this.getSchedule().get(i).getHomeTeam() != null && this.getSchedule().get(i).getAwayTeam() != null) {
                if (this.getSchedule().get(i).getHomeTeam().getTgid() == school.getTgid() ||
                        this.getSchedule().get(i).getAwayTeam().getTgid() == school.getTgid()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if schools are not in the same conference and don't already play one another
     * @param school the opponent
     * @return true if schools are not in the same conference and don't already play one another
     */
    public boolean isPossibleOpponent(School school) {
        return !this.isInConference(school) && !this.isOpponent(school);
    }

    /**
     * Prints the schedule of a school
     */
    public void printSchedule() {
        int i = 0;
        int lastWeek = -1;
        while (i < this.getSchedule().size()) {
            int nextWeek = 100;//random high number
            for (int j = 0; j < this.getSchedule().size(); j++) {
                if (this.getSchedule().get(j).getWeek() < nextWeek && this.getSchedule().get(j).getWeek() > lastWeek) {
                    nextWeek = this.getSchedule().get(j).getWeek();
                }
            }

            System.out.print(i + 1 + ". ");
            Game game = this.getSchedule().getGame(nextWeek);
            System.out.print(this);
            if (this.getTgid() == game.getHomeTeam().getTgid()) {
                System.out.print(" vs " + game.getAwayTeam());
            } else {
                System.out.print(" at " + game.getHomeTeam());
            }
            System.out.println(" (week " + (nextWeek + 1) + ")");
            i++;
            lastWeek = nextWeek;
        }
    }

    /**
     * Returns true if opponent is a rival, false if else
     * @param opponent the opponent
     * @return true if opponent is a rival, false if else
     */
    public boolean isRival(School opponent) {
        for (int i = 0; i < this.getRivals().size(); i++) {
            if (this.getRivals().get(i).getName().equals(opponent.getName())) {
                return true;
            }
        }
        for (int i = 0; i < opponent.getRivals().size(); i++) {
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

	public void updateAlignment(Conference conference, String division, String ncaaDivision) {
		this.conference = conference;
		this.division = division;
		this.ncaaDivision = ncaaDivision;
		
	}
}
