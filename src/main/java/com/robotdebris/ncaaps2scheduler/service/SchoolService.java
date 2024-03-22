package com.robotdebris.ncaaps2scheduler.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.model.Swap;

public class SchoolService {

	private List<School> schoolList;
	@Autowired
	ExcelReader excelReader;
	@Autowired
	ConferenceService conferenceService;

	private final Logger LOGGER = Logger.getLogger(SchoolService.class.getName());

	@PostConstruct
	public void init() {

		// final String schoolsFile = "src/main/resources/School_Data.xlsx";
		final String schoolsFile = "resources/app/School_Data.xlsx";
//	    
		try {
			schoolList = excelReader.getSchoolData(schoolsFile);
			Collections.sort(schoolList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<School> getSchoolList() {
		return schoolList;
	}

	/**
	 * @param school the String name of the School you are searching for
	 * @return School with the same name as the the parameter inputted
	 */
	public School schoolSearch(String school) {
		for (School theSchool : schoolList) {
			if (theSchool.getName().equalsIgnoreCase(school)) {
				return theSchool;
			}
		}
		LOGGER.warn(school + " could not be found, please check your spelling and try again.");
		return null;
	}

	/**
	 *
	 * @param conf the name of a a conference
	 * @return all schools in a given conference conf
	 */
	public List<School> getAllSchoolsInConference(String conf) {
		List<School> conference = new ArrayList<School>();
		for (int i = 0; i < schoolList.size(); i++) {
			School school = schoolList.get(i);
			if (school.getConference() != null && school.getConference().getName().equalsIgnoreCase(conf)) {
				conference.add(school);
			}
		}
		return conference;
	}

	/**
	 *
	 * @return ArrayList<String> a list of all conferences in the list
	 */
//    public ArrayList<String> getConferences() {
//        ArrayList<String> conferences = new ArrayList();
//        for (int i = 0; i < schoolList.size(); i++) {
//            School school = schoolList.get(i);
//            if (!conferences.contains(school.getConference())) {
//                conferences.add(school.getConference());
//            }
//        }
//        return conferences;
//    }

	/**
	 * @param tgid the tgid of the school you are searching for
	 * @return School with the same tgid as the parameter inputted
	 */
	public School schoolSearch(int tgid) {
		for (School theSchool : schoolList) {
			if (theSchool.getTgid() == tgid) {
				return theSchool;
			}
		}
		LOGGER.warn(
				tgid + " could not be found. (schoolList may be an FCS team that is missing in your excel document)");
		return null;
	}

	/**
	 * Searches for user schools and adds that flag to every user school in the list
	 */
	public void populateUserSchools() {
		for (int i = 0; i < schoolList.size(); i++) {
			School theSchool = schoolList.get(i);
			int numOfUserGames = 0;
			for (int j = 0; j < theSchool.getSchedule().size(); j++) {
				if (theSchool.getSchedule().get(j).getUserGame() == 1) {
					numOfUserGames++;
				}
			}
			if (numOfUserGames >= 10) {
				theSchool.setUserTeam(true);
			}
		}
	}

	/**
	 * @return List<School> of schools with > 12 games
	 */
	public List<School> findTooManyGames() {
		List<School> tooManyGames = new ArrayList<School>();
		for (int i = 0; i < schoolList.size(); i++) {
			School theSchool = schoolList.get(i);
			if (theSchool.getNcaaDivision().equals("FBS") && theSchool.getSchedule().size() > 12) {
				tooManyGames.add(theSchool);
			}
		}
		return tooManyGames;
	}

	/**
	 * @return List<School> of schools with < 12 games
	 */
	public List<School> findTooFewGames() {
		List<School> tooFewGames = new ArrayList<School>();
		for (int i = 0; i < schoolList.size(); i++) {
			School theSchool = schoolList.get(i);
			if (theSchool.getSchedule().size() < 12 && theSchool.getNcaaDivision().equals("FBS")) {
				tooFewGames.add(theSchool);
			}
		}
		return tooFewGames;
	}

	public void resetAllSchoolsSchedules() {
		for (School school : schoolList) {
			school.setSchedule(new SchoolSchedule());
		}

	}

	public void swapSchools(School s1, School s2) {
		// if the conferences & divisions aren't already the same...
		if (!(s1.getConference().getName() == s2.getConference().getName() && s1.getDivision() == s2.getDivision())) {
			Conference tempConf = s1.getConference();
			String tempDiv = s1.getDivision();
			String tempNcaaDiv = s1.getNcaaDivision();

			s1.setConference(s2.getConference());
			s1.setDivision(s2.getDivision());
			s1.setNcaaDivision(s2.getNcaaDivision());
			// this could also be done recursively like conf swap
			s2.setConference(tempConf);
			s2.setDivision(tempDiv);
			s2.setNcaaDivision(tempNcaaDiv);

			// need to add schedule stuff here
			// need to reset the conferenceList, it isn't updating
			// in the future, optimize this by making it only set the updated confs instead
			// of all
			conferenceService.setConferencesSchoolList(schoolList);

			swaplist.add(new Swap(s1, s2));
		}
	}

}
