package com.robotdebris.ncaaps2scheduler.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Stadium {

    @Id
    private int stadiumId;

    private String name;

    private int capacity;
}
