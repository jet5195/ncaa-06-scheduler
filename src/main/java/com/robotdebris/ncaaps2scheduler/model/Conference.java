package com.robotdebris.ncaaps2scheduler.model;

import java.util.ArrayList;

public class Conference {
	
	//private int conferenceID;
	private String name;
	private ArrayList<String> divisions;
	private boolean powerConf;
	private String color;
	private String altColor;
	private String logo;
	
	public Conference(String conferenceName, boolean powerConf, String division1, String division2, String color, String altColor,
			String logo) {
		this.name = conferenceName;
		this.powerConf = powerConf;
		if(division1 != null && !division1.trim().isEmpty()) {
			divisions = new ArrayList<>();
			divisions.add(division1);
			divisions.add(division2);
		}
		this.color = color;
		this.altColor = altColor;
		this.logo = logo;
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
	public void setDivision(ArrayList<String> divisions) {
		this.divisions = divisions;
	}
	public boolean isPowerConf() {
		return powerConf;
	}
	public void setPowerConf(boolean powerConf) {
		this.powerConf = powerConf;
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
	public void setDivisions(ArrayList<String> divisions) {
		this.divisions = divisions;
	}

}
