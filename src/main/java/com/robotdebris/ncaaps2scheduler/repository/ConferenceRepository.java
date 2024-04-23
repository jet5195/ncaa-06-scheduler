package com.robotdebris.ncaaps2scheduler.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.exception.ConferenceNotFoundException;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;

/**
 * Repository interface for Conference entities.
 */
@Repository
public interface ConferenceRepository {

	/**
	 * Finds all Conference entities.
	 *
	 * @return a list of all conferences
	 */
	List<Conference> findAll();

	/**
	 * Finds a single Conference entity by its ID.
	 *
	 * @param id the ID of the conference to find
	 * @return the found Conference or null if not found
	 */
	Conference findById(int id) throws ConferenceNotFoundException;

	/**
	 * Finds a single Conference entity by its name.
	 *
	 * @param name the name of the conference to find
	 * @return the found Conference or null if not found
	 */
	Conference findByName(String name) throws ConferenceNotFoundException;

	List<Conference> findByNCAADivision(NCAADivision div);

	void saveAll(List<Conference> conferences);

	void renameConferenceByName(String oldName, String newName);

}
