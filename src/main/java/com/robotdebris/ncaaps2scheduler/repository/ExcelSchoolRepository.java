package com.robotdebris.ncaaps2scheduler.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;

@Repository
public class ExcelSchoolRepository implements SchoolRepository {

	private final Logger LOGGER = Logger.getLogger(ExcelSchoolRepository.class.getName());
	List<School> schools = new ArrayList<>();

	@Override
	public List<School> findAll() {
		return Collections.unmodifiableList(schools);
	}

	@Override
	public School findById(int id) {
		for (School theSchool : schools) {
			if (theSchool.getTgid() == id) {
				return theSchool;
			}
		}
		LOGGER.warn(id + " could not be found. (school may be an FCS team that is missing in your excel document)");
		return null;
	}

	@Override
	public School findByName(String name) {
		for (School school : schools) {
			if (school.getName().equalsIgnoreCase(name)) {
				return school;
			}
		}
		LOGGER.warn("School with name '" + name + " could not be found, please check your spelling and try again.");
		return null;
	}

	@Override
	public void saveAll(List<School> schools) {
		// TODO: possibly need to look into the cause here
		this.schools = schools.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public List<School> findByConference(Conference conference) {
		return schools.stream().filter(school -> school.getConference().equals(conference)).toList();
	}

	@Override
	public List<School> findByNCAADivision(NCAADivision div) {
		return schools.stream().filter(school -> school.getConference().getClassification().equals(div)).toList();
	}

	@Override
	public List<School> findByDivision(Division div) {
		return schools.stream().filter(school -> school.getDivision() != null && school.getDivision().equals(div))
				.collect(Collectors.toList());
	}

	@Override
	public void save(School school) {
		School existingSchool = findById(school.getTgid());
		existingSchool.setName(school.getName());
		existingSchool.setNickname(school.getNickname());
		// existingSchool.setConferenceId(school.getConferenceId());
		existingSchool.setConference(school.getConference());
		// existingSchool.setDivisionId(school.getDivisionId());
		existingSchool.setDivision(school.getDivision());
		existingSchool.setColor(school.getColor());
		existingSchool.setAltColor(school.getAltColor());
		existingSchool.setLogo(school.getLogo());
		existingSchool.setLatitude(school.getLatitude());
		existingSchool.setLongitude(school.getLongitude());
		existingSchool.setAbbreviation(school.getAbbreviation());
		existingSchool.setStadiumName(school.getStadiumName());
		existingSchool.setState(school.getState());
		existingSchool.setCity(school.getCity());
		existingSchool.setStadiumCapacity(school.getStadiumCapacity());
		existingSchool.setRivals(school.getRivals());
		existingSchool.setUserTeam(school.isUserTeam());
		existingSchool.setxDivRival(school.getxDivRival());

	}

}
