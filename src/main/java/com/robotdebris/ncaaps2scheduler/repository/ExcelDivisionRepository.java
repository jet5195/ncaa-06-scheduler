package com.robotdebris.ncaaps2scheduler.repository;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExcelDivisionRepository implements DivisionRepository {
    List<Division> divisionList = new ArrayList<>();

    @Override
    public List<Division> findAll() {
        return divisionList;
    }

    @Override
    public Division findById(int id) {
        return divisionList.stream().filter(d -> d.getDivisionId() == id).findFirst().orElse(null);
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
