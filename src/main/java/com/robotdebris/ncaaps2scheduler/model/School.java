package com.robotdebris.ncaaps2scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class School implements Comparable<School> {

    // TEAM tscs = conference ranking
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
    private NCAADivision ncaaDivision;
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
    private List<School> rivals;
    @Getter
    @Setter
    private boolean userTeam;
    //	@Getter
//	@Setter
//	@JsonIgnore
//	private SchoolSchedule schedule = new SchoolSchedule();
    @Getter
    @Setter
    @JsonIgnore
    private School xDivRival;

    private GameRepository gameRepository;

    School() {
    }

    public List<Game> getSchedule() {
        return gameRepository.findGamesByTeam(this);
    }

    public Game getGameByWeek(int week) {
        List<Game> schedule = gameRepository.findGamesByTeam(this);
        Optional<Game> optional = schedule.stream().filter(game -> game.getWeek() == week).findFirst();
        return optional.orElse(null);
    }

//	public void addGame(Game theGame) {
//		this.schedule.add(theGame);
//	}

    /**
     * Searches a school's schedule for a game that is not in conference or a
     * rivalry game
     *
     * @return Game that is removable (not a rivalry and not a conference game)
     */
    public Game findNonConferenceNonRivalryGame() {
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
    public boolean isEligibleNonConferenceMatchup(School school) {
        return !this.isInConference(school) && !this.isOpponent(school);
    }

    /**
     * Prints the schedule of a school
     */
//	public void printSchedule() {
//		int i = 0;
//		int lastWeek = -1;
//		while (i < this.getSchedule().size()) {
//			int nextWeek = 100;// random high number
//			for (int j = 0; j < this.getSchedule().size(); j++) {
//				if (this.getSchedule().get(j).getWeek() < nextWeek && this.getSchedule().get(j).getWeek() > lastWeek) {
//					nextWeek = this.getSchedule().get(j).getWeek();
//				}
//			}
//
//			System.out.print(i + 1 + ". ");
//			Game game = this.getSchedule().getGame(nextWeek);
//			System.out.print(this);
//			if (this.getTgid() == game.getHomeTeam().getTgid()) {
//				System.out.print(" vs " + game.getAwayTeam());
//			} else {
//				System.out.print(" at " + game.getHomeTeam());
//			}
//			System.out.println(" (week " + (nextWeek + 1) + ")");
//			i++;
//			lastWeek = nextWeek;
//		}
//	}

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

    public void updateAlignment(Conference conference, String division, NCAADivision ncaaDivision, School xDivRival) {
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

    // The builder static inner class
    public static class Builder {

        private int tgid;
        private String name;
        private String nickname;
        private String state;
        private Conference conference;
        private String division;
        private NCAADivision ncaaDivision;
        private String color;
        private String altColor;
        private String logo;
        private List<School> rivals;
        private boolean userTeam;
        private School xDivRival;
        // Fields identical to the ones in School
        private GameRepository gameRepository;
        // Other fields...

        public Builder() {
            // Initialize with default values if necessary
        }

        // Setter methods for each field that return the Builder itself
        public Builder withScheduleRepository(GameRepository gameRepository) {
            this.gameRepository = gameRepository;
            return this;
        }

        public Builder withTgid(int tgid) {
            this.tgid = tgid;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public Builder withConference(Conference conference) {
            this.conference = conference;
            return this;
        }

        public Builder withDivision(String division) {
            this.division = division;
            return this;
        }

        public Builder withNCAADivision(NCAADivision ncaaDivision) {
            this.ncaaDivision = ncaaDivision;
            return this;
        }

        public Builder withColor(String color) {
            this.color = color;
            return this;
        }

        public Builder withAltColor(String altColor) {
            this.altColor = altColor;
            return this;
        }

        public Builder withLogo(String logo) {
            this.logo = logo;
            return this;
        }

        public Builder withRivals(List<School> rivals) {
            this.rivals = rivals;
            return this;
        }

        public Builder isUserTeam(boolean userTeam) {
            this.userTeam = userTeam;
            return this;
        }

        public Builder withXDivRival(School xDivRival) {
            this.xDivRival = xDivRival;
            return this;
        }

        public School build() {
            School school = new School();
            school.gameRepository = this.gameRepository;
            school.tgid = this.tgid;
            school.name = this.name;
            school.nickname = this.nickname;
            school.state = this.state;
            school.conference = this.conference;
            school.division = this.division;
            school.ncaaDivision = this.ncaaDivision;
            school.color = this.color;
            school.altColor = this.altColor;
            school.logo = this.logo;
            school.rivals = this.rivals;
            school.userTeam = this.userTeam;
            school.xDivRival = this.xDivRival;
            return school;
        }
    }
}
