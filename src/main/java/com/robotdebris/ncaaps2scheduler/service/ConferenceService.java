package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.ConferenceRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConferenceService {

    private final Logger LOGGER = Logger.getLogger(ConferenceService.class.getName());

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

    public List<School> getSchoolsByDivision(String name, Division division) {
        Conference conf = findByShortName(name);
        return conf.getSchoolsByDivision(division);
    }

    public void addSchool(String name, School s1) {
        Conference newConference = findByShortName(name);
        Conference oldConference = s1.getConference();
        oldConference.getSchools().remove(s1);
        newConference.getSchools().add(s1);
    }

    /**
     * @param conferenceShortName the String name of the Conference you are searching for
     * @return Conference with the same name as the the parameter inputed
     */
    public Conference findByShortName(String conferenceShortName) {
        Conference conference = conferenceRepository.findByShortName(conferenceShortName);
        if (conference == null) {
            LOGGER.warn(conferenceShortName + " could not be found, please check your spelling and try again.");
        }
        return conference;
    }

    public void setConferencesSchoolList() {
        getConferenceList().forEach(conf -> conf.setSchools(schoolRepository.findByConference(conf)));
    }

    public Conference findConferenceById(int cgid) {
        return conferenceRepository.findById(cgid);
    }
}
