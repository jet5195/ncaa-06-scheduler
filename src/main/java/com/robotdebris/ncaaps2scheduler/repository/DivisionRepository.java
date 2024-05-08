package com.robotdebris.ncaaps2scheduler.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;

@Repository
public interface DivisionRepository {

	/**
	 * Finds all Division entities.
	 *
	 * @return a list of all Divisions
	 */
	List<Division> findAll();

	/**
	 * Finds a single Division entity by its ID.
	 *
	 * @param id the ID of the Division to find
	 * @return the found Division or null if not found
	 */
	Optional<Division> findById(Integer id);

	/**
	 * Finds a single Division entity by its name.
	 *
	 * @param name the name of the Division to find
	 * @return the found Division or null if not found
	 */
	Division findByName(String name);

	List<Division> findByConference(Conference conference);

	void saveAll(List<Division> Divisions);
}
