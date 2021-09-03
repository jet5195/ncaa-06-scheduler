package com.robotdebris.ncaaps2scheduler.service;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.FileChooser;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolList;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.model.SeasonSchedule;

@Service
public class ScheduleService {
	
	private static SchoolList schoolList = new SchoolList();
	
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
			SeasonSchedule seasonSchedule = ExcelReader.getScheduleData(scheduleFile, schoolList);
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
}


