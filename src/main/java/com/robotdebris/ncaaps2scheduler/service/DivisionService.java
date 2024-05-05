package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.repository.DivisionRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void setDivisionsSchoolList() {
        getAllDivisions().forEach(div -> div.setSchools(schoolRepository.findByDivision(div)));
    }
}
