package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class School implements Comparable<School> {

    //	TEAM  tscs = conference ranking
//0 = conf champ
//TBRK = bcs bowl ranking
//tmrk = media poll rank
    @Getter
    @Setter
    private int tgid;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String nickname;
    @Getter
    @Setter
    private String state;
    @Getter
    @Setter
    private Conference conference;
    @Getter
    @Setter
    private String division;
    @Getter
    @Setter
    private String ncaaDivision; // fbs or fcs
    @Getter
    @Setter
    private String color;
    @Getter
    @Setter
    private String altColor;
    @Getter
    @Setter
    private String logo;
    @Getter
    @Setter
    @JsonIgnore
    private SchoolList rivals;
    @Getter
    @Setter
    private boolean userTeam;
    @Getter
    @Setter
    @JsonIgnore
    private SchoolSchedule schedule = new SchoolSchedule();
    @Getter
    @Setter
    @JsonIgnore
    private School xDivRival;

    public School(int tgid, String name, String nickname, String state, Conference conference, String division,
                  String ncaaDivision, String color, String altColor, String logo) {
        this.tgid = tgid;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.conference = conference;
        this.division = division;
        this.ncaaDivision = ncaaDivision;
        this.color = color;
        this.altColor = altColor;
        this.logo = logo;
    }

    public School(int tgid, String name, String nickname, String state, String color, String altColor, String logo) {
        this.tgid = tgid;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.color = color;
        this.altColor = altColor;
        this.logo = logo;
    }

    public void addGame(Game theGame) {
        this.schedule.add(theGame);
    }

    /**
     * Searches a school's schedule for a game that is not in conference or a
     * rivalry game
     *
     * @return Game that is removable (not a rivalry and not a conference game)
     */
    public Game findRemovableGame() {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            Game theGame = this.getSchedule().get(i);
            // 0 means non-con
            if (theGame.isRemovableGame()) {
                return theGame;
            }
        }
        return null;
    }

    /**
     * Checks to see if this school is in the same conference as another
     *
     * @param school the school you are checking against
     * @return true if in the same conference, false if else
     */
    public boolean isInConference(School school) {
        if (this.getConference() == null || this.getConference().getName().equalsIgnoreCase("Independent")) {
            return false;

        } else if (school.getConference() == null) {
            return false;
        } else
            return this.getConference().getName().equalsIgnoreCase(school.getConference().getName());
    }

    /**
     * Checks to see if this school plays an opponent
     *
     * @param school the opponent
     * @return true if these schools do play, false if else
     */
    public boolean isOpponent(School school) {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            if (this.getSchedule().get(i).getHomeTeam() != null && this.getSchedule().get(i).getAwayTeam() != null) {
                if (this.getSchedule().get(i).getHomeTeam().getTgid() == school.getTgid()
                        || this.getSchedule().get(i).getAwayTeam().getTgid() == school.getTgid()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if schools are not in the same conference and don't already play
     * one another
     *
     * @param school the opponent
     * @return true if schools are not in the same conference and don't already play
     * one another
     */
    public boolean isPossibleOpponent(School school) {
        return !this.isInConference(school) && !this.isOpponent(school);
    }

    /**
     * Prints the schedule of a school
     */
    public void printSchedule() {
        int i = 0;
        int lastWeek = -1;
        while (i < this.getSchedule().size()) {
            int nextWeek = 100;// random high number
            for (int j = 0; j < this.getSchedule().size(); j++) {
                if (this.getSchedule().get(j).getWeek() < nextWeek && this.getSchedule().get(j).getWeek() > lastWeek) {
                    nextWeek = this.getSchedule().get(j).getWeek();
                }
            }

            System.out.print(i + 1 + ". ");
            Game game = this.getSchedule().getGame(nextWeek);
            System.out.print(this);
            if (this.getTgid() == game.getHomeTeam().getTgid()) {
                System.out.print(" vs " + game.getAwayTeam());
            } else {
                System.out.print(" at " + game.getHomeTeam());
            }
            System.out.println(" (week " + (nextWeek + 1) + ")");
            i++;
            lastWeek = nextWeek;
        }
    }

    /**
     * Returns true if opponent is a rival, false if else
     *
     * @param opponent the opponent
     * @return true if opponent is a rival, false if else
     */
    public boolean isRival(School opponent) {
        for (int i = 0; this.getRivals() != null && i < this.getRivals().size(); i++) {
            if (this.getRivals().get(i).getName().equals(opponent.getName())) {
                return true;
            }
        }
        for (int i = 0; this.getRivals() != null && i < opponent.getRivals().size(); i++) {
            if (opponent.getRivals().get(i).getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }

    public int getNumOfConferenceGames() {
        int confGames = 0;
        for (Game game : this.getSchedule()) {
            if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
                confGames++;
            }
        }
        return confGames;
    }

    public int getNumOfHomeConferenceGames() {
        int homeConfGames = 0;
        for (Game game : this.getSchedule()) {
            if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
                if (game.getHomeTeam() == this) {
                    homeConfGames++;
                }
            }
        }
        return homeConfGames;
    }

    public int getNumOfAwayConferenceGames() {
        int awayConfGames = 0;
        for (Game game : this.getSchedule()) {
            if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
                if (game.getAwayTeam() == this) {
                    awayConfGames++;
                }
            }
        }
        return awayConfGames;
    }

    public int getNumOfDivisionalGames() {
        int divisionalGames = 0;
        for (Game game : this.getSchedule()) {
            if (game.getHomeTeam().isInConference(game.getAwayTeam())
                    && game.getHomeTeam().getDivision().equalsIgnoreCase(game.getAwayTeam().getDivision())) {
                divisionalGames++;
            }
        }
        return divisionalGames;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public void updateAlignment(Conference conference, String division, String ncaaDivision, School xDivRival) {
        this.conference = conference;
        this.division = division;
        this.ncaaDivision = ncaaDivision;
        this.xDivRival = xDivRival;

    }

    @Override
    public int compareTo(School o) {
        return this.name.compareTo(o.name);
    }

//	@Override
//	public boolean equals(School o) {
//		if(this.name.equals(o.name)) {
//			return true;
//		} else {
//			return false;
//		}
//	}
}
