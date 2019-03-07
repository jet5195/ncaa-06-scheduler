import java.util.ArrayList;

import org.apache.log4j.Logger;

public class SeasonSchedule extends SchoolSchedule {

    private SeasonSchedule bowlSchedule;

    private final Logger LOGGER = Logger.getLogger(SeasonSchedule.class.getName());

    public SeasonSchedule getBowlSchedule() {
        return bowlSchedule;
    }

    public void setBowlSchedule(SeasonSchedule bowlSchedule) {
        this.bowlSchedule = bowlSchedule;
    }


    public void addGame(School s1, School s2, int week, int day) {
        randomizeHomeTeam(s1, s2, week, day, findGameNumber(week));
    }

    public void addGameSpecificHomeTeam(School s1, School s2, int week, int day) {
        addGame(s1, s2, week, day, findGameNumber(week));
    }

    //only used in replaceGame method
    private void addGame(School away, School home, int week, int day, int gameNumber) {
        //this if statement is so rivalry games switch back and forth from year to year
        Game newGame = new Game(away, home, gameNumber, week, day);
        away.addGame(newGame);
        home.addGame(newGame);
        this.add(newGame);//decide if this is actually how you want to add games.. if doing it like this I will just have to go through and remake the schedule in the end. Best solution is to make a addGame method.. NO make schedule its own new object that extends a list and change the add method.. or add to it
        LOGGER.info("Adding game " + away.getName() + " at " + home.getName());
    }

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
        LOGGER.info("Removing game " + s1 + " at " + s2);
    }

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

    private void randomizeHomeTeam(School s1, School s2, int week, int day, int game) {
        if (s1.isRival(s2) || s1.isPowerConf() == s2.isPowerConf()) {
            int max = 2;
            int min = 1;
            int range = max - min + 1;
            int random = (int) (Math.random() * range) + min;
            if (random == 1 && (s1.isRival(s2) || !s2.getDivision().equalsIgnoreCase("FCS"))) {
                addGame(s1, s2, week, day, game);
            } else {
                addGame(s2, s1, week, day, game);
            }
        } else {
            if (s1.isPowerConf()) {
                addGame(s2, s1, week, day, game);
            } else {
                addGame(s1, s2, week, day, game);
            }
        }
    }

    private void updateGameNumbers(int gameNumber, int weekNumber) {
        for (Game game : this) {
            if (game.getWeek() == weekNumber && game.getGameNumber() > gameNumber) {
                game.setGameNumber(game.getGameNumber() - 1);
            }
        }
    }

    public void removeAllFcsGames() {
        for (int i = 0; i < this.size(); i++) {
            Game game = this.get(i);
            if (game.getHomeTeam().getDivision().equalsIgnoreCase("FCS") || game.getAwayTeam().getDivision().equalsIgnoreCase("FCS")) {
                this.removeGame(game);
                i--;
            }
        }
    }

    public void removeAllNonConferenceGames(boolean removeRivals) {
        for (int i = 0; i < this.size(); i++) {
            Game game = this.get(i);
            if (!game.getHomeTeam().getConference().equalsIgnoreCase(game.getAwayTeam().getConference())) {
                if (removeRivals) {
                    this.removeGame(game);
                    i--;
                } else if (!game.isRivalryGame()) {
                    this.removeGame(game);
                    i--;
                }
            }
        }
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
}