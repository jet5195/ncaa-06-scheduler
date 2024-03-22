package com.robotdebris.ncaaps2scheduler.service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.ConferenceList;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SwapList;

@Service
public class ConferenceService {

	private ConferenceList conferenceList;
	@Autowired
	SwapList swaplist;
	@Autowired
	SchoolService schoolService;
	@Autowired
	ExcelReader excelReader;
//	@Autowired
//	int year;

	static {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");
	}

	private final Logger LOGGER = Logger.getLogger(ConferenceList.class.getName());

	public List<Conference> getConferenceList() {
		return conferenceList;
	}

	// always call with callAgain TRUE
	private void swapConferences(Conference c1, Conference c2, boolean callAgain) {
		int i = 0;
		for (School s1 : c1.getSchools()) {
			s1.setConference(c2);
			if (c2.getDivisions() != null) {
				if (i < 6) {
					s1.setDivision(c1.getDivisions().get(0));
				} else {
					s1.setDivision(c1.getDivisions().get(1));
				}
			} else {
				s1.setDivision(null);
			}
			i++;
		}
		if (callAgain) {
			swapConferences(c2, c1, false);
		} else {
			setConferencesSchoolList(schoolService.getSchoolList());
		}
	}

	public void swapConferences(String name1, String name2) {
		Conference c1 = conferenceSearch(name1);
		Conference c2 = conferenceSearch(name2);
		swapConferences(c1, c2, true);

	}

	public SwapList getSwapList() {
		// right now setting the order when pulling the data, not sure if this makes
		// more sense
		// or setting the swap order while running
		for (int i = 0; i < swaplist.size(); i++) {
			swaplist.get(i).setSwapID(i);
		}
		return swaplist;
	}

	public void renameConference(String name, String newName) {
		Conference c1 = conferenceSearch(name);
		c1.setName(newName);
	}

	public void renameDivision(String name, String divisionName, String newName) {
		Conference c1 = conferenceSearch(name);
		if (c1.getDivisions().get(0) == divisionName) {
			c1.getDivisions().set(0, newName);
			for (School school : c1.getSchools()) {
				if (school.getDivision() == divisionName) {
					school.setDivision(newName);
				}
			}
		}
	}

	public void swapSchools(int tgid1, int tgid2) {
		School s1 = schoolService.schoolSearch(tgid1);
		School s2 = schoolService.schoolSearch(tgid2);
		schoolService.swapSchools(s1, s2);
	}

	public List<School> getSchoolsByDivision(String name, String division) {
		Conference conf = conferenceSearch(name);
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
		Conference newConference = conferenceSearch(name);
		Conference oldConference = s1.getConference();
		oldConference.getSchools().remove(s1);
		newConference.getSchools().add(s1);
	}

	/**
	 * @param conference the String name of the Conference you are searching for
	 * @return Conference with the same name as the the parameter inputed
	 */
	public Conference conferenceSearch(String conference) {
		for (Conference conf : conferenceList) {
			if (conf.getName().equalsIgnoreCase(conference)) {
				return conf;
			}
		}
		LOGGER.warn(conference + " could not be found, please check your spelling and try again.");
		return null;
	}

	public void setConferencesSchoolList(List<School> schoolList) {
		for (Conference conf : conferenceList) {
			conf.setSchools(schoolService.getAllSchoolsInConference(conf.getName()));
		}
	}
}
