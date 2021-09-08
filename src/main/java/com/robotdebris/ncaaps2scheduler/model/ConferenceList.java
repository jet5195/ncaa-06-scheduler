package com.robotdebris.ncaaps2scheduler.model;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConferenceList extends LinkedList<Conference> {
    static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    private final Logger LOGGER = Logger.getLogger(ConferenceList.class.getName());

    /**
     * @param conference the String name of the Conference you are searching for
     * @return Conference with the same name as the the parameter inputted
     */
    public Conference conferenceSearch(String conference) {
        for (Conference theConference : this) {
            if (theConference.getName().equalsIgnoreCase(conference)) {
                return theConference;
            }
        }
        LOGGER.warn(conference + " could not be found, please check your spelling and try again.");
        return null;
    }
}
