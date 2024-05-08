package com.robotdebris.ncaaps2scheduler.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;

@Repository
public class ExcelDivisionRepository implements DivisionRepository {
	List<Division> divisionList = new ArrayList<>();

	@Override
	public List<Division> findAll() {
		return divisionList;
	}

	@Override
	public Optional<Division> findById(Integer id) {
		if (id == null) {
			return Optional.empty();
		}
		return divisionList.stream().filter(d -> d.getDivisionId() == id).findFirst();
	}

	@Override
	public Division findByName(String name) {
		return divisionList.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Override
	public List<Division> findByConference(Conference conference) {
		return divisionList.stream().filter(d -> d.getConference().equals(conference)).toList();
	}

	@Override
	public void saveAll(List<Division> divisions) {
		this.divisionList = divisions;
	}
}
