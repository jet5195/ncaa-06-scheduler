package com.robotdebris.ncaaps2scheduler.model;

public class Swap {
	//might just let ID = place in list
	private int swapID;
	private School school1;
	private School school2;
	
	public Swap(School school1, School school2) {
		this.school1 = school1;
		this.school2 = school2;
	}
	
	public int getSwapID() {
		return swapID;
	}
	public void setSwapID(int swapID) {
		this.swapID = swapID;
	}
	public School getSchool1() {
		return school1;
	}
	public void setSchool1(School school1) {
		this.school1 = school1;
	}
	public School getSchool2() {
		return school2;
	}
	public void setSchool2(School school2) {
		this.school2 = school2;
	}
	
	
}
