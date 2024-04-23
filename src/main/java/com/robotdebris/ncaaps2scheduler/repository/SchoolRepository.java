package com.robotdebris.ncaaps2scheduler.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.exception.SchoolNotFoundException;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;

/**
 * Repository interface for School entities.
 */
@Repository
public interface SchoolRepository {

	/**
	 * Finds all School entities.
	 *
	 * @return a list of all schools
	 */
	List<School> findAll();

	/**
	 * Finds a single School entity by its ID.
	 *
	 * @param id the ID of the school to find
	 * @return the found School or null if not found
	 */
	School findById(int id) throws SchoolNotFoundException;

	/**
	 * Finds a single School entity by its name.
	 *
	 * @param name the name of the school to find
	 * @return the found School or null if not found
	 */
	School findByName(String name) throws SchoolNotFoundException;

	List<School> findByConference(Conference conference);

	void saveAll(List<School> schools);

}
