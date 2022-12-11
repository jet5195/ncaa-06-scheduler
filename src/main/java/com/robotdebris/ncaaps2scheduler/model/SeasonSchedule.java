package com.robotdebris.ncaaps2scheduler.model;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class SeasonSchedule extends SchoolSchedule {

//    private SeasonSchedule bowlSchedule;
    private int year;
    private final Logger LOGGER = Logger.getLogger(SeasonSchedule.class.getName());

    /**
     *
     * @return the bowl schedule
     */
//    public SeasonSchedule getBowlSchedule() {
//        return bowlSchedule;
//    }

    /**
     * Sets the bowl schedule. Currently this is only called once when the excel file is being read
     * @param bowlSchedule
     */
//    public void setBowlSchedule(SeasonSchedule bowlSchedule) {
//        this.bowlSchedule = bowlSchedule;
//    }

    /**
     * Adds game with a randomized home team
     * @param s1 school 1
     * @param s2 school 2
     * @param week the week of the game
     * @param day the day of the game
     */
    public void addGame(School s1, School s2, int week, int day) {
        randomizeHomeTeam(s1, s2, week, day, findGameNumber(week));
    }

    /**
     * Adds game to the schedule with the home team already selected
     * @param away the away school
     * @param home the home school
     * @param week the week of the game
     * @param day the day of the game
     */
    public void addGameSpecificHomeTeam(School away, School home, int week, int day) {
        addGame(away, home, week, day, findGameNumber(week));
    }
    
    public void addGameSpecificHomeTeam(School away, School home, int week, int day, int time, GameResult gameResult) {
        addGame(away, home, week, day, findGameNumber(week), time, gameResult);
    }
    
    public void addGameYearlySeries(School s1, School s2, int week, int day, int year) {
    	//logic to decide home or away team here?
    	if(year%2 == 0) {
    		addGame(s1, s2, week, day, findGameNumber(week));
    	}
    	else {
    		addGame(s2, s1, week, day, findGameNumber(week));
    	}
    }

    /**
     * Adds game to schedule after the home team is selected, either randomly or via addGameSpecificHomeTeam method
     * @param away the away school
     * @param home the home school
     * @param week the week of the game
     * @param day the day of the game
     * @param gameNumber the game of the week
     */
    private void addGame(School away, School home, int week, int day, int gameNumber) {
        Game newGame = new Game(away, home, gameNumber, week, day);
		addGame(newGame);
        LOGGER.info("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
        System.out.println("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
    }
    
    /**
     * Adds game to schedule after the home team is selected, either randomly or via addGameSpecificHomeTeam method
     * @param away the away school
     * @param home the home school
     * @param week the week of the game
     * @param day the day of the game
     * @param gameNumber the game of the week
     */
    private void addGame(School away, School home, int week, int day, int gameNumber, int time, GameResult gameResult) {
        Game newGame = new Game(away, home, gameNumber, week, day, time, gameResult);
		away.addGame(newGame);
		home.addGame(newGame);
		this.add(newGame);
        LOGGER.info("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
    }
    

    /**
     * Removes a game from the schedule and updates all affected game numbers
     * @param theGame the game to be removed
     */
    public void removeGame(Game theGame) {
        //code to change the game numbers for all games afterwards in this week
        School s1 = theGame.getHomeTeam();
        School s2 = theGame.getAwayTeam();
        int gameNumber = theGame.getGameNumber();
        int weekNumber = theGame.getWeek();
        this.remove(theGame);
        s1.getSchedule().remove(theGame);
        s2.getSchedule().remove(theGame);
        updateGameNumbers(gameNumber, weekNumber);
        LOGGER.info("Removing game " + s2+ " at " + s1);
        System.out.println("Removing game " + s2 + " at " + s1);
    }

    /**
     * Replaces theGame with a new game between school s1 & s2
     * @param theGame the game to be replaced
     * @param s1 school 1 of the new game
     * @param s2 school 2 of the new game
     */
    public void replaceGame(Game theGame, School s1, School s2) {
        int gameNumber = theGame.getGameNumber();
        int weekNumber = theGame.getWeek();
        int dayNumber = theGame.getDay();
        this.remove(theGame);
        LOGGER.info("Removing and replacing " + theGame.getAwayTeam() + " at " + theGame.getHomeTeam());
        theGame.getHomeTeam().getSchedule().remove(theGame);
        theGame.getAwayTeam().getSchedule().remove(theGame);
        randomizeHomeTeam(s1, s2, weekNumber, dayNumber, gameNumber);
    }

    /**
     * Adds a game with a random home team. This does contain logic for P5 getting home preference over G5 and FCS schools as well.
     * @param s1 school 1
     * @param s2 school 2
     * @param week week of the game
     * @param day day of the game
     * @param game game number of the week
     */
    private void randomizeHomeTeam(School s1, School s2, int week, int day, int game) {
        if (s1.isRival(s2) || s1.getConference().isPowerConf() == s2.getConference().isPowerConf()) {
            int max = 2;
            int min = 1;
            int range = max - min + 1;
            int random = (int) (Math.random() * range) + min;
            if (random == 1 && (s1.isRival(s2) || !s2.getNcaaDivision().equalsIgnoreCase("FCS"))) {
                addGame(s1, s2, week, day, game);
            } else {
                addGame(s2, s1, week, day, game);
            }
        } else {
            if (s1.getConference().isPowerConf()) {
                addGame(s2, s1, week, day, game);
            } else {
                addGame(s1, s2, week, day, game);
            }
        }
    }

    /**
     * Updates the game numbers after removing a game from a week's schedule
     * @param gameNumber the game number that is being removed
     * @param weekNumber the week of the game that is being removed
     */
    private void updateGameNumbers(int gameNumber, int weekNumber) {
        if(weekNumber < 16)
        for (Game game : this) {
            if (game.getWeek() == weekNumber && game.getGameNumber() > gameNumber) {
                game.setGameNumber(game.getGameNumber() - 1);
            }
        }
    }

    /**
     * Removes all FCS games from the schedule, 
     * @return count of removed games
     */
    public int removeAllFcsGames() {
    	int count = 0;
        for (int i = 0; i < this.size(); i++) {
            Game game = this.get(i);
            if(game.getHomeTeam().getNcaaDivision() == null || game.getAwayTeam().getNcaaDivision() == null) {
            	i= i;
            }
            if (game.getHomeTeam().getNcaaDivision().equalsIgnoreCase("FCS") || game.getAwayTeam().getNcaaDivision().equalsIgnoreCase("FCS")) {
                this.removeGame(game);
                count++;
                i--;
            }
        }
        return count;
    }

    /**
     * Removes all conference games from the schedule for a given conference,
     * @param conf to remove games from
     * @return count of removed games
     */
    public int removeAllConferenceGames(Conference conf) {
        int count = 0;
        for(School school : conf.getSchools()) {
            for (int i = 0; i < school.getSchedule().size(); i++) {
                Game game = school.getSchedule().get(i);
                if (game.getHomeTeam().getConference() != null && game.getAwayTeam().getConference() != null && game.getHomeTeam().getConference().getName().equalsIgnoreCase(game.getAwayTeam().getConference().getName())) {
                    this.removeGame(game);
                    i--;
                }
            }
        }
        return count;
    }

    /**@return count of removed games
     * Removes all non-conference games from schedule
     * @param removeRivals if true, all Non-Conference games will be removed. If false, then only non-conference games that aren't rivalry games will be removed
     */
    public int removeAllNonConferenceGames(boolean removeRivals) {
    	int count = 0;
        for (int i = 0; i < this.size(); i++) {
            Game game = this.get(i);
            //remove game no matter what if either team isn't in a conference.
            if(game.getHomeTeam().getConference() == null || game.getAwayTeam().getConference() == null){
                this.removeGame(game);
                count++;
                i--;
            } else if (!game.getHomeTeam().getConference().getName().equalsIgnoreCase(game.getAwayTeam().getConference().getName())) {
                if (removeRivals) {
                    this.removeGame(game);
                    count++;
                    i--;
                } else if (!game.isRivalryGame()) {
                    this.removeGame(game);
                    count++;
                    i--;
                }
            }
        }
        return count;
    }
    
    public int removeAllGames() {
    	int count = 0;
    	for (int i = 0; i < this.size(); i++) {
    		Game game = this.get(i);
    		this.removeGame(game);
    		count++;
    		i--;
    	}
    	return count;
    }

    /**
     * @return ArrayList of Strings of the SeasonSchedule
     */
    public ArrayList scheduleToList(boolean header) {
        ArrayList<ArrayList> list = new ArrayList();
        if (header) {
            ArrayList<String> firstLine = new ArrayList();
            firstLine.add("GSTA");
            firstLine.add("GASC");
            firstLine.add("GHSC");
            firstLine.add("GTOD");
            firstLine.add("GATG");
            firstLine.add("GHTG");
            firstLine.add("SGNM");
            firstLine.add("SEWN");
            firstLine.add("GDAT");
            firstLine.add("GFOT");
            firstLine.add("SEWT");
            firstLine.add("GFFU");
            firstLine.add("GFHU");
            firstLine.add("GMFX");
            list.add(firstLine);
        }
        for (Game game : this) {
            list.add(game.gameToList());
        }
        return list;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public ArrayList<Game> getScheduleByWeek(int week) {
        ArrayList<Game> weeklySchedule = new ArrayList<>();
        for (Game game : this) {
            if (game.getWeek() == week) {
                weeklySchedule.add(game);
            }
        }
        return weeklySchedule;
    }

//    public ArrayList<Game> getBowlScheduleByWeek(int week) {
//        ArrayList<Game> weeklySchedule = new ArrayList<>();
//        for (Game game : this.getBowlSchedule()) {
//            if (game.getWeek() == week) {
//                weeklySchedule.add(game);
//            }
//        }
//        return weeklySchedule;
//    }

    public void addGame(Game game) {
        game.getAwayTeam().addGame(game);
        game.getHomeTeam().addGame(game);
        this.add(game);
    }
}