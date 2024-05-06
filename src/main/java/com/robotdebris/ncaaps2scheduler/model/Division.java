package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Division {

    @Id
    private int divisionId;
    private String name;
    private String shortName;
    @JsonBackReference("conference-divisions-ref")
    private Conference conference;
    @JsonManagedReference("division-schools-ref")
    private List<School> schools;

    public Division(int divisionId, String name, String shortName, Conference conference) {
        this.divisionId = divisionId;
        this.name = name;
        this.shortName = shortName;
        this.conference = conference;
    }

    public Division() {

    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int dgid) {
        this.divisionId = dgid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return divisionId == ((Division) o).divisionId;
    }
}
