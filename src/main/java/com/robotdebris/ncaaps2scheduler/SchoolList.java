package com.robotdebris.ncaaps2scheduler;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.ArrayList;
import java.util.LinkedList;

public class SchoolList extends LinkedList<School> {
    static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    private final Logger LOGGER = Logger.getLogger(SchoolList.class.getName());

    /**
     * @param school the String name of the School you are searching for
     * @return School with the same name as the the parameter inputted
     */
    public School schoolSearch(String school) {
        for (School theSchool : this) {
            if (theSchool.getName().equalsIgnoreCase(school)) {
                return theSchool;
            }
        }
        LOGGER.warn(school + " could not be found, please check your spelling and try again.");
        return null;
    }

    /**
     *
     * @param conf the name of a a conference
     * @return all schools in a given conference conf
     */
    public SchoolList conferenceSearch(String conf) {
        SchoolList conference = new SchoolList();
        for (int i = 0; i < this.size(); i++) {
            School school = this.get(i);
            if (school.getConference().equalsIgnoreCase(conf)) {
                conference.add(school);
            }
        }
        return conference;
    }

    /**
     *
     * @return ArrayList<String> a list of all conferences in the list
     */
    public ArrayList<String> getConferences() {
        ArrayList<String> conferences = new ArrayList();
        for (int i = 0; i < this.size(); i++) {
            School school = this.get(i);
            if (!conferences.contains(school.getConference())) {
                conferences.add(school.getConference());
            }
        }
        return conferences;
    }

    /**
     * @param tgid the tgid of the school you are searching for
     * @return School with the same tgid as the parameter inputted
     */
    public School schoolSearch(int tgid) {
        for (School theSchool : this) {
            if (theSchool.getTgid() == tgid) {
                return theSchool;
            }
        }
        LOGGER.warn(tgid + " could not be found. (This may be an FCS team that is missing in your excel document)");
        return null;
    }

    /**
     * Searches for user schools and adds that flag to every user school in the list
     */
    public void populateUserSchools() {
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            int numOfUserGames = 0;
            for (int j = 0; j < theSchool.getSchedule().size(); j++) {
                if (theSchool.getSchedule().get(j).getUserGame() == 1) {
                    numOfUserGames++;
                }
            }
            if (numOfUserGames >= 10) {
                theSchool.setUserTeam(true);
            }
        }
    }

    /**
     * @return SchoolList of schools with > 12 games
     */
    public SchoolList findTooManyGames() {
        SchoolList tooManyGames = new SchoolList();
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            if (theSchool.getDivision().equals("FBS") && theSchool.getSchedule().size() > 12) {
                tooManyGames.add(theSchool);
            }
        }
        return tooManyGames;
    }

    /**
     * @return SchoolList of schools with < 12 games
     */
    public SchoolList findTooFewGames() {
        SchoolList tooFewGames = new SchoolList();
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            if (theSchool.getSchedule().size() < 12 && theSchool.getDivision().equals("FBS")) {
                tooFewGames.add(theSchool);
            }
        }
        return tooFewGames;
    }
}
