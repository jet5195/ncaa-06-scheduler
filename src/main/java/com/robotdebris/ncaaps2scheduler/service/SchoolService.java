package com.robotdebris.ncaaps2scheduler.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolSchedule;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;

@Service
public class SchoolService {

	@Autowired
	ConferenceService conferenceService;

	private final SchoolRepository schoolRepository;

	private final Logger LOGGER = Logger.getLogger(SchoolService.class.getName());

	public SchoolService(SchoolRepository schoolRepository) {
		this.schoolRepository = schoolRepository;
	}

//	@PostConstruct
//	public void init() {
//
//		final String schoolsFile = "src/main/resources/School_Data.xlsx";
//		// final String schoolsFile = "resources/app/School_Data.xlsx";
//
//		try {
//			List<School> schoolList = excelReader.populateSeasonScheduleFromExcel(schoolsFile);
//			Collections.sort(schoolList);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public List<School> getAllSchools() {
		return schoolRepository.findAll();
	}

	public void setSchoolList(List<School> schoolList) {
		// this.schoolList = schoolList;
	}

	/**
	 * @param school the String name of the School you are searching for
	 * @return School with the same name as the the parameter inputted
	 */
	public School schoolSearch(String name) {
		return schoolRepository.findByName(name);
	}

	/**
	 *
	 * @param conf the name of a a conference
	 * @return all schools in a given conference conf
	 */
	public List<School> getAllSchoolsInConference(String conferenceName) {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> school.getConference() != null
						&& school.getConference().getName().equalsIgnoreCase(conferenceName))
				.collect(Collectors.toList());
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
		return schoolRepository.findById(tgid);
	}

	/**
	 * Searches for user schools and adds that flag to every user school in the list
	 */
	public void populateUserSchools() {
		for (int i = 0; i < getAllSchools().size(); i++) {
			School theSchool = getAllSchools().get(i);
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
	 * Finds schools with more than 12 games in their schedule.
	 *
	 * @return a list of schools with too many games
	 */
	public List<School> findTooManyGames() {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> "FBS".equals(school.getNcaaDivision()) && school.getSchedule().size() > 12)
				.collect(Collectors.toList());
	}

	/**
	 * @return List<School> of schools with < 12 games
	 */
	public List<School> findTooFewGames() {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> "FBS".equals(school.getNcaaDivision()) && school.getSchedule().size() < 12)
				.collect(Collectors.toList());
	}

	public void resetAllSchoolsSchedules() {
		for (School school : getAllSchools()) {
			school.setSchedule(new SchoolSchedule());
		}

	}

}
