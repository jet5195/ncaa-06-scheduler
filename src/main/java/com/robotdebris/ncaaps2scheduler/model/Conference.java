package com.robotdebris.ncaaps2scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Conference implements Comparable<Conference> {

	@Id
	private Integer conferenceId;
	private String name;
	private String shortName;
	private String abbreviation;
	private NCAADivision classification;
	@JsonManagedReference("conference-divisions-ref")
	private List<Division> divisions;
	private boolean powerConf;
	private String logo;
	private Integer numOfConfGames;
	private int confGamesStartWeek;
	@JsonManagedReference("conference-schools-ref")
	private List<School> schools;

	public static Conference blankConference = new Conference(null, "", "", "", NCAADivision.FANTASY, false, 0, 0, "");

	public Conference() {

	}

	public Conference(Integer conferenceId, String conferenceName, String shortName, String abbreviation,
			NCAADivision classification, boolean powerConf, int numOfConfGames, int confGamesStartWeek, String logo) {
		this.conferenceId = conferenceId;
		this.name = conferenceName;
		this.shortName = shortName;
		this.abbreviation = abbreviation;
		this.classification = classification;
		this.powerConf = powerConf;
		this.numOfConfGames = numOfConfGames;
		this.confGamesStartWeek = confGamesStartWeek;
		this.logo = logo;
		this.divisions = new ArrayList<>();
	}

	public Integer getConferenceId() {
		return conferenceId;
	}

	public void setConferenceId(Integer conferenceId) {
		this.conferenceId = conferenceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public NCAADivision getClassification() {
		return classification;
	}

	public void setClassification(NCAADivision classification) {
		this.classification = classification;
	}

	public boolean isPowerConf() {
		return powerConf;
	}

	public void setPowerConf(boolean powerConf) {
		this.powerConf = powerConf;
	}

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
	}

	public List<School> getSchoolsByDivision(Division division) {
		List<School> divSchools = new ArrayList<>();
		for (School school : this.getSchools()) {
			if (school.getDivision().equals(division)) {
				divSchools.add(school);
			}
		}
		return divSchools;
	}

	public Integer getNumOfConfGames() {
		return numOfConfGames;
	}

	public void setNumOfConfGames(Integer numOfConfGames) {
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

	public List<Division> getDivisions() {
		return divisions;
	}

	public void setDivisions(List<Division> divisions) {
		this.divisions = divisions;
	}

	public boolean isFBS() {
		return this.classification.isFBS();
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public int compareTo(Conference o) {
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
		Conference otherConference = (Conference) o;
		return Objects.equals(conferenceId, otherConference.conferenceId); // Compare names using equals
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
