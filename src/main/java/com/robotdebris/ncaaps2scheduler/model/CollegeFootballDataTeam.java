package com.robotdebris.ncaaps2scheduler.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollegeFootballDataTeam {
	private int id;
	private String school;
	private String mascot;
	private String abbreviation;
	@JsonProperty("alt_name1")
	private String altName1;
	@JsonProperty("alt_name2")
	private String altName2;
	@JsonProperty("alt_name3")
	private String altName3;
	private String classification;
	private String conference;
	private String division;
	private String color;
	@JsonProperty("alt_color")
	private String altColor;
	private List<String> logos;
	private String twitter;
	private Location location;

	// Nested Location class
	public static class Location {
		@JsonProperty("venue_id")
		private int venueId;
		private String name;
		private String city;
		private String state;
		private String zip;
		@JsonProperty("country_code")
		private String countryCode;
		private String timezone;
		private double latitude;
		private double longitude;
		private double elevation;
		private int capacity;
		@JsonProperty("year_constructed")
		private int yearConstructed;
		private boolean grass;
		private boolean dome;

		public int getVenueId() {
			return venueId;
		}

		public void setVenueId(int venueId) {
			this.venueId = venueId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getZip() {
			return zip;
		}

		public void setZip(String zip) {
			this.zip = zip;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getTimezone() {
			return timezone;
		}

		public void setTimezone(String timezone) {
			this.timezone = timezone;
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

		public double getElevation() {
			return elevation;
		}

		public void setElevation(double elevation) {
			this.elevation = elevation;
		}

		public int getCapacity() {
			return capacity;
		}

		public void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		public int getYearConstructed() {
			return yearConstructed;
		}

		public void setYearConstructed(int yearConstructed) {
			this.yearConstructed = yearConstructed;
		}

		public boolean isGrass() {
			return grass;
		}

		public void setGrass(boolean grass) {
			this.grass = grass;
		}

		public boolean isDome() {
			return dome;
		}

		public void setDome(boolean dome) {
			this.dome = dome;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getMascot() {
		return mascot;
	}

	public void setMascot(String mascot) {
		this.mascot = mascot;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAltName1() {
		return altName1;
	}

	public void setAltName1(String altName1) {
		this.altName1 = altName1;
	}

	public String getAltName2() {
		return altName2;
	}

	public void setAltName2(String altName2) {
		this.altName2 = altName2;
	}

	public String getAltName3() {
		return altName3;
	}

	public void setAltName3(String altName3) {
		this.altName3 = altName3;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getConference() {
		return conference;
	}

	public void setConference(String conference) {
		this.conference = conference;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
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

	public List<String> getLogos() {
		return logos;
	}

	public void setLogos(List<String> logos) {
		this.logos = logos;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
