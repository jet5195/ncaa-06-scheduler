package com.robotdebris.ncaaps2scheduler.repository;

import com.robotdebris.ncaaps2scheduler.exception.ConferenceNotFoundException;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ExcelConferenceRepository implements ConferenceRepository {

    private final Logger LOGGER = Logger.getLogger(ExcelConferenceRepository.class.getName());
    List<Conference> conferenceList = new ArrayList<>();

    @Override
    public List<Conference> findAll() {
        return Collections.unmodifiableList(conferenceList);
    }

    @Override
    public Conference findById(int id) {
        return conferenceList.stream()
                .filter(conference -> conference.getConferenceId() == id)
                .findFirst().orElse(null);
    }


    @Override
    public Conference findByName(String name) throws ConferenceNotFoundException {
        for (Conference conf : conferenceList) {
            if (conf.getName().equalsIgnoreCase(name)) {
                return conf;
            }
        }
        LOGGER.warn(
                "Conference with name '" + name + "' could not be found, please check your spelling and try again.");
        throw new ConferenceNotFoundException("Conference with name '" + name + "' could not be found.");
    }

    @Override
    public void saveAll(List<Conference> conferences) {
        System.out.println("Saving " + conferences.size() + " conferences");
        this.conferenceList = conferences;
        System.out.println("Excel Conf Repo has " + conferenceList.size() + " conferences");
    }

    @Override
    public Conference findByShortName(String conferenceShortName) {
        return conferenceList.stream().filter(c -> c.getShortName().equals(conferenceShortName)).findFirst().orElse(null);
    }

    @Override
    public List<Conference> findByNCAADivision(NCAADivision div) {
        return conferenceList.stream().filter(conf -> conf.getClassification() == div).toList();
    }

}
