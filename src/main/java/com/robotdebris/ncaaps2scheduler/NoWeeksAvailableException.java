package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;

public class NoWeeksAvailableException extends RuntimeException {

    public NoWeeksAvailableException(School s1) {

        super("No available weeks found for scheduling " + s1.getName() +
        		" . Make sure you removed all non-conference games before" + 
        		" setting conference schedule.");
    }
    
    public NoWeeksAvailableException(Conference c1) {

        super("No available weeks found for scheduling " + c1.getName() +
        		" . Make sure you removed all non-conference games before" + 
        		" setting conference schedule.");
    }
}