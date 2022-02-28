package com.robotdebris.ncaaps2scheduler.model;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class ConferenceList extends LinkedList<Conference> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7843984523561502029L;

	static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    private final Logger LOGGER = Logger.getLogger(ConferenceList.class.getName());

    /**
     * @param conference the String name of the Conference you are searching for
     * @return Conference with the same name as the the parameter inputed
     */
    public Conference conferenceSearch(String conference) {
        for (Conference conf : this) {
            if (conf.getName().equalsIgnoreCase(conference)) {
                return conf;
            }
        }
        LOGGER.warn(conference + " could not be found, please check your spelling and try again.");
        return null;
    }
    
    public void setConferencesSchoolList(SchoolList schoolList) {
		for (Conference conf : this) {
			conf.setSchools(schoolList.getAllSchoolsInConference(conf.getName()));
		}
	}
}
