package com.robotdebris.ncaaps2scheduler.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.robotdebris.ncaaps2scheduler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.FileChooser;

@Service
public class ScheduleService {

	private static SchoolList schoolList = new SchoolList();

	private static SeasonSchedule seasonSchedule;

	
	static {
		FileChooser fileChooser = new FileChooser();
	    //final String schoolsFile = fileChooser.chooseFile("Select Custom Conferences Excel Document");
		final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
//	    if (schoolsFile == null){
//	        System.out.println("No file selected, exiting program.");
//	        System.exit(0);
//	    }
	    //final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
	    final String scheduleFile =  "src/main/resources/SCHED.xlsx";
//	    final String scheduleFile =  fileChooser.chooseFile("Select Schedule Excel Document");
//	    if (scheduleFile == null){
//	        System.out.println("No file selected, exiting program.");
//	        System.exit(0);
//	    }
	    //final String scheduleFile = "src/main/resources/SCHED.xlsx";
	    try {
			schoolList = ExcelReader.getSchoolData(schoolsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			seasonSchedule = ExcelReader.getScheduleData(scheduleFile, schoolList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //System.out.println("Welcome to the NCAA Football PS2 Scheduler");
	}
	
	public static SchoolList getSchoolList() {
		return schoolList;
	}
	
	public static School getSchool(int schoolId) {
		return schoolList.get(schoolId);
	}
	
	public static SchoolSchedule getSchoolSchedule(int schoolId) {
		return schoolList.get(schoolId).getSchedule();
	}
	
	public static SchoolList getSchoolRivals(int schoolId) {
		return schoolList.get(schoolId).getRivals();
	}
	
	public static School searchByTgid(int tgid) {
		return schoolList.schoolSearch(tgid);
    }
	
	public static School searchByName(String name) {
		return schoolList.schoolSearch(name);
    }

    public static SchoolList getAvailableOpponents(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		SchoolList availableOpponents = new SchoolList();
		for (School school: schoolList) {
			// if they don't already play one another
			if (input.isPossibleOpponent(school)){
				// if they don't have a game that week
				if (school.getSchedule().getGame(week) == null){
					availableOpponents.add(school);
				}
			}
		}
		return availableOpponents;
	}

	public static SchoolList getAvailableRivals(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		SchoolList availableOpponents = new SchoolList();
		for (School school: input.getRivals()) {
			// if they don't already play one another
			if (input.isPossibleOpponent(school)){
				// if they don't have a game that week
				if (school.getSchedule().getGame(week) != null){
					availableOpponents.add(school);
				}
			}
		}
		return availableOpponents;
	}

	public void removeAllOocNonRivalGames() {
		seasonSchedule.removeAllNonConferenceGames(false);
	}

	public void removeAllOocGames() {
		seasonSchedule.removeAllNonConferenceGames(true);
	}

	public void removeAllFcsGames() {
		seasonSchedule.removeAllFcsGames();
	}

	public void removeGame(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		Game game = input.getSchedule().getGame(week);
		seasonSchedule.removeGame(game);
	}

	public void addGame(int awayId, int homeId, int week){
		School home = ScheduleService.searchByTgid(homeId);
		School away = ScheduleService.searchByTgid(awayId);
		int day = 5;
		seasonSchedule.addGameSpecificHomeTeam(away, home, week, day);
	}

	public ArrayList<Integer> getEmptyWeeks(int id, int id2) {
		School s1 = ScheduleService.searchByTgid(id);
		School s2 = ScheduleService.searchByTgid(id2);
		ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
		ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
		return findEmptyWeeks(s1weeks, s2weeks);
	}

	//finds empty weeks for one school
	public static ArrayList<Integer> findEmptyWeeks(School s) {
		ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
		ArrayList<Integer> usedWeeks = new ArrayList<Integer>();
		for (int i = 0; i < s.getSchedule().size(); i++) {
			usedWeeks.add(s.getSchedule().get(i).getWeek());
		}//populates freeWeeks with 0-14, all the possible weeks for regular season games
		for (int i = 0; i < 15; i++) {
			if (!usedWeeks.contains(i)) {
				freeWeeks.add(i);
			}
		}
		return freeWeeks;
	}

	private static ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
		ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
		for (int i = 0; i < s1weeks.size(); i++) {
			if (s2weeks.contains(s1weeks.get(i))) {
				freeWeeks.add(s1weeks.get(i));
			}
		}
		return freeWeeks;
	}
}


