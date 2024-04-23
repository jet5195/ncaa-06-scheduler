package com.robotdebris.ncaaps2scheduler.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.exception.ConferenceNotFoundException;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;

@Repository
public class ExcelConferenceRepository implements ConferenceRepository {

	private final Logger LOGGER = Logger.getLogger(ExcelConferenceRepository.class.getName());
	List<Conference> conferenceList = new ArrayList<Conference>();

	@Override
	public List<Conference> findAll() {
		return Collections.unmodifiableList(conferenceList);
	}

	@Override
	public Conference findById(int id) {
		// TODO Auto-generated method stub
		return null;
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
		this.conferenceList = conferences;
	}

	@Override
	public void renameConferenceByName(String oldName, String newName) {
		Conference conf = findByName(oldName);
		if (conf != null) {
			conf.setName(newName);
		} else {
			LOGGER.warn("Conference with name '" + oldName + "' could not be found for renaming.");
			throw new ConferenceNotFoundException("Conference with name '" + oldName + "' could not be found.");
		}
	}

	@Override
	public List<Conference> findByNCAADivision(NCAADivision div) {
		return conferenceList.stream().filter(conf -> conf.isFbs() == div.isFBS()).toList();
	}

}
