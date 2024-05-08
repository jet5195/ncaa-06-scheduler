package com.robotdebris.ncaaps2scheduler.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.repository.DivisionRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;

@Service
public class DivisionService {
	@Autowired
	private DivisionRepository divisionRepository;
	@Autowired
	private SchoolRepository schoolRepository;

	public List<Division> getAllDivisions() {
		return divisionRepository.findAll();
	}

	public void saveDivisions(List<Division> divisions) {
		divisionRepository.saveAll(divisions);
	}

	public Division findByName(String divisionName) {
		return divisionRepository.findByName(divisionName);
	}

	public Optional<Division> findById(Integer id) {
		return divisionRepository.findById(id);
	}

	public void setDivisionsSchoolList() {
		getAllDivisions().forEach(div -> div.setSchools(schoolRepository.findByDivision(div)));
	}
}
