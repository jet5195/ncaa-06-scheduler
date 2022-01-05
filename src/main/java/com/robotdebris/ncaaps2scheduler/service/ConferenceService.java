package com.robotdebris.ncaaps2scheduler.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.ConferenceList;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolList;
import com.robotdebris.ncaaps2scheduler.model.Swap;
import com.robotdebris.ncaaps2scheduler.model.SwapList;

@Service
public class ConferenceService {
	
	@Autowired
	ConferenceList conferenceList;
	@Autowired
	SwapList swaplist;
	@Autowired
	SchoolList schoolList;
	@Autowired
	ExcelReader excelReader;
//	@Autowired
//	int year;
	
	public void swapSchools(School s1, School s2) {
		//if the conferences & divisions aren't already the same...
		if(!(s1.getConference().getName() == s2.getConference().getName()
				&& s1.getDivision() == s2.getDivision())) {
			Conference tempConf = s1.getConference();
			String tempDiv = s1.getDivision();
			String tempNcaaDiv = s1.getNcaaDivision();
			
			s1.setConference(s2.getConference());
			s1.setDivision(s2.getDivision());
			s1.setNcaaDivision(s2.getNcaaDivision());
			//this could also be done recursively like conf swap
			s2.setConference(tempConf);
			s2.setDivision(tempDiv);
			s2.setNcaaDivision(tempNcaaDiv);
			
			//need to add schedule stuff here
			//need to reset the conferenceList, it isn't updating
			//in the future, optimize this by making it only set the updated confs instead of all
			conferenceList.setConferencesSchoolList(schoolList);
			
			swaplist.add(new Swap(s1, s2));
		}
	}
	
	//always call with callAgain TRUE
	private void swapConferences(Conference c1, Conference c2, boolean callAgain) {
		int i = 0;
		for (School s1 : c1.getSchools()) {
			s1.setConference(c2);
			if(c2.getDivisions()!=null) {
				if(i<6) {
					s1.setDivision(c1.getDivisions().get(0));
				} else {
					s1.setDivision(c1.getDivisions().get(1));
				}
			} else {
				s1.setDivision(null);
			}
			i++;
		}
		if(callAgain) {
			swapConferences(c2, c1, false);
		} else {
			conferenceList.setConferencesSchoolList(schoolList);
		}
	}

	public void swapConferences(String name1, String name2) {
		Conference c1 = conferenceList.conferenceSearch(name1);
		Conference c2 = conferenceList.conferenceSearch(name2);
		swapConferences(c1, c2, true);
		
	}

	public SwapList getSwapList() {
		//right now setting the order when pulling the data, not sure if this makes more sense
		//or setting the swap order while running
		for(int i = 0; i < swaplist.size(); i++) {
			swaplist.get(i).setSwapID(i);
		}
		return swaplist;
	}

	public void renameConference(String name, String newName) {
		Conference c1 = conferenceList.conferenceSearch(name);
		c1.setName(newName);
	}
	
	public void renameDivision(String name, String divisionName, String newName) {
		Conference c1 = conferenceList.conferenceSearch(name);
		if(c1.getDivisions().get(0) == divisionName) {
			c1.getDivisions().set(0, newName);
			for (School school : c1.getSchools()) {
				if(school.getDivision() == divisionName) {
					school.setDivision(newName);
				}
			}
		}
	}

	public void swapSchools(int tgid1, int tgid2) {
		School s1 = schoolList.schoolSearch(tgid1);
		School s2 = schoolList.schoolSearch(tgid2);
		swapSchools(s1, s2);
		
	}

	public SchoolList getSchoolsByDivision(String name, String division) {
		Conference conf = conferenceList.conferenceSearch(name);
		return conf.getSchoolsByDivision(division);
	}
	
	public void downloadSwapFile(Writer writer) {
		try {
			CsvExportService csvExportService = new CsvExportService();
            csvExportService.writeSwapList(writer, swaplist);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	public void addSchool(String name, School s1) {
		Conference newConference = conferenceList.conferenceSearch(name);
		Conference oldConference = s1.getConference();
		oldConference.getSchools().remove(s1);
		newConference.getSchools().add(s1);
	}
}
