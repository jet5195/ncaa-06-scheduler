package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final GameRepository gameRepository;
    private final Logger LOGGER = Logger.getLogger(SchoolService.class.getName());
    @Autowired
    ConferenceService conferenceService;

    public SchoolService(SchoolRepository schoolRepository, GameRepository gameRepository) {
        this.schoolRepository = schoolRepository;
        this.gameRepository = gameRepository;
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
        schoolRepository.saveAll(schoolList);
    }

    /**
     * @param name the String name of the School you are searching for
     * @return School with the same name as the parameter inputted
     */
    public School schoolSearch(String name) {
        return schoolRepository.findByName(name);
    }

    /**
     * @param conferenceName the name of a conference
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
        List<School> allSchools = schoolRepository.findAll();
        for (School school : allSchools) {
            long numOfUserGames = school.getSchedule().stream().filter(game -> game.getUserGame() == 1).count();
            if (numOfUserGames >= 10) {
                school.setUserTeam(true);
            }
        }
        schoolRepository.saveAll(allSchools);
    }

    /**
     * Finds schools with more than 12 games in their schedule.
     *
     * @return a list of schools with too many games
     */
    public List<School> findTooManyGames() {
        List<School> allSchools = schoolRepository.findAll();
        return allSchools.stream()
                .filter(school -> school.getNcaaDivision().isFBS() && school.getSchedule().size() > 12)
                .collect(Collectors.toList());
    }

    /**
     * @return List<School> of schools with < 12 games
     */
    public List<School> findTooFewGames() {
        List<School> allSchools = schoolRepository.findAll();
        return allSchools.stream()
                .filter(school -> school.getNcaaDivision().isFBS() && school.getSchedule().size() < 12)
                .collect(Collectors.toList());
    }
}
