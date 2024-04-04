package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Conference {

    @Getter
    @Setter
    private int conferenceID;
    private String name;
    private ArrayList<String> divisions;
    private boolean powerConf;
    private boolean fbs;
    //	private String color;
//	private String altColor;
    private String logo;
    private int numOfConfGames;
    private int confGamesStartWeek;
    private int numOfSchools;
    @JsonIgnore
    private List<School> schools;

    public Conference() {

    }

    public Conference(String conferenceName, boolean powerConf, String division1, String division2, String logo,
                      int numOfConfGames, int confGamesStartWeek) {
        this.name = conferenceName;
        this.powerConf = powerConf;
        if (division1 != null && !division1.trim().isEmpty()) {
            divisions = new ArrayList<>();
            divisions.add(division1);
            divisions.add(division2);
        }
//		this.color = color;
//		this.altColor = altColor;
        this.logo = logo;
        this.numOfConfGames = numOfConfGames;
        this.confGamesStartWeek = confGamesStartWeek;
    }

    public Conference(String conferenceName, boolean powerConf, String division1, String division2, String logo,
                      int numOfConfGames, int confGamesStartWeek, int conferenceID) {
        this.name = conferenceName;
        this.powerConf = powerConf;
        if (division1 != null && !division1.trim().isEmpty()) {
            divisions = new ArrayList<>();
            divisions.add(division1);
            divisions.add(division2);
        }
        this.logo = logo;
        this.numOfConfGames = numOfConfGames;
        this.confGamesStartWeek = confGamesStartWeek;
        this.conferenceID = conferenceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getDivisions() {
        return divisions;
    }

    public void setDivisions(ArrayList<String> divisions) {
        this.divisions = divisions;
    }

    public void setDivision(ArrayList<String> divisions) {
        this.divisions = divisions;
    }

    public boolean isPowerConf() {
        return powerConf;
    }

    public void setPowerConf(boolean powerConf) {
        this.powerConf = powerConf;
    }

    //	public String getColor() {
//		return color;
//	}
//	public void setColor(String color) {
//		this.color = color;
//	}
//	public String getAltColor() {
//		return altColor;
//	}
//	public void setAltColor(String altColor) {
//		this.altColor = altColor;
//	}
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
        this.numOfSchools = schools.size();
        this.fbs = schools.get(0).getNcaaDivision().isFBS();
    }

    public List<School> getSchoolsByDivision(String division) {
        List<School> divSchools = new ArrayList<School>();
        for (School school : this.getSchools()) {
            if (school.getDivision().equalsIgnoreCase(division)) {
                divSchools.add(school);
            }
        }
        return divSchools;
    }

    public int getNumOfConfGames() {
        return numOfConfGames;
    }

    public void setNumOfConfGames(int numOfConfGames) {
        this.numOfConfGames = numOfConfGames;
    }

    // big 10 1
    // pac-12 2
    // sec 2
    // acc 3
    // big 12 3
    // c-usa 3
    // mwc 3
    // mac 4
    // sun belt 4
    // aac 4

    public int getConfGamesStartWeek() {
        return confGamesStartWeek;
    }

    public void setConfGamesStartWeek(int confGamesStartWeek) {
        this.confGamesStartWeek = confGamesStartWeek;
    }

    public int getNumOfSchools() {
        return numOfSchools;
    }

    public boolean isFbs() {
        return fbs;
    }

}
