package com.robotdebris.ncaaps2scheduler.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.ConferenceRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;

@Service
public class ConferenceService {

	static {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");
	}

	private final Logger LOGGER = Logger.getLogger(ConferenceService.class.getName());
	// @Autowired
//	int year;

	ConferenceRepository conferenceRepository;
	SchoolRepository schoolRepository;

	public ConferenceService(ConferenceRepository conferenceRepository, SchoolRepository schoolRepository) {
		this.conferenceRepository = conferenceRepository;
		this.schoolRepository = schoolRepository;
	}

	public List<Conference> getConferenceList() {
		return conferenceRepository.findAll();
	}

	public void saveConferences(List<Conference> conferenceList) {
		conferenceRepository.saveAll(conferenceList);
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
			setConferencesSchoolList(schoolRepository.findAll());
		}
	}

	public void swapConferences(String name1, String name2) {
		Conference c1 = conferenceSearch(name1);
		Conference c2 = conferenceSearch(name2);
		swapConferences(c1, c2, true);

	}

	public void renameConference(String name, String newName) {
		conferenceRepository.renameConferenceByName(name, newName);
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

	public List<School> getSchoolsByDivision(String name, String division) {
		Conference conf = conferenceSearch(name);
		return conf.getSchoolsByDivision(division);
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
		for (Conference conf : getConferenceList()) {
			if (conf.getName().equalsIgnoreCase(conference)) {
				return conf;
			}
		}
		LOGGER.warn(conference + " could not be found, please check your spelling and try again.");
		return null;
	}

	public void setConferencesSchoolList(List<School> schoolList) {
		for (Conference conf : getConferenceList()) {
			conf.setSchools(schoolRepository.findByConference(conf));
		}
	}

}
