import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Schedule extends LinkedList<Game> {

    static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }
    private final Logger LOGGER = Logger.getLogger(Schedule.class.getName());

    public Game getGame(int week){
        for (Game game : this) {
            if (game.getWeek() == week) {
                return game;
            }
        }
        return null;
    }

    public void addGame(School s1, School s2, int week, int day, int year) {
        addGame(s1, s2, week, day, findGameNumber(week), year);
    }

    //only used in replaceGame method
    private void addGame(School s1, School s2, int week, int day, int gameNumber, int year) {
        //this if statement is so rivalry games switch back and forth from year to year
        Game newGame = year % 2 == 0 ? new Game(s1, s2, gameNumber, week, day) : new Game(s2, s1, gameNumber, week, day);

        s1.addGame(newGame);
        s2.addGame(newGame);
        this.add(newGame);//decide if this is actually how you want to add games.. if doing it like this I will just have to go through and remake the schedule in the end. Best solution is to make a addGame method.. NO make schedule its own new object that extends a list and change the add method.. or add to it
        LOGGER.info("Adding game " + s1.getName() + " vs " + s2.getName());
    }

    private int findGameNumber(int week) {
        int gameNumber = 0;
        for (Game theGame : this) {
            gameNumber = theGame.getWeek() == week && gameNumber < theGame.getGameNumber() ? theGame.getGameNumber() : gameNumber;
        }
        return ++gameNumber;
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
        LOGGER.info("Removing game " + s1 + " vs " + s2);
    }

    public void replaceGame(Game theGame, School s1, School s2, int year) {
        int gameNumber = theGame.getGameNumber();
        int weekNumber = theGame.getWeek();
        this.remove(theGame);
        LOGGER.info("Removing & replacing game between " + theGame.getAwayTeam() + " and " + theGame.getHomeTeam());
        theGame.getHomeTeam().getSchedule().remove(theGame);
        theGame.getAwayTeam().getSchedule().remove(theGame);
        addGame(s1, s2, weekNumber, 5, gameNumber, year);
    }

    private void updateGameNumbers(int gameNumber, int weekNumber) {
        for (Game game : this) {
            if (game.getWeek() == weekNumber && game.getGameNumber() > gameNumber) {
                game.setGameNumber(game.getGameNumber() - 1);
            }
        }
    }

    /**
     * @return ArrayList of Strings of the Schedule
     */
    public ArrayList scheduleToList(){
        ArrayList<ArrayList> list = new ArrayList();
        ArrayList<String> firstLine = new ArrayList();
        firstLine.add("GSTA"); firstLine.add("GASC"); firstLine.add("GHSC"); firstLine.add("GTOD");
        firstLine.add("GATG"); firstLine.add("GHTG"); firstLine.add("SGNM"); firstLine.add("SEWN");
        firstLine.add("GDAT"); firstLine.add("GFOT"); firstLine.add("SEWT"); firstLine.add("GFFU");
        firstLine.add("GFHU"); firstLine.add("GMFX"); firstLine.add(String.valueOf(this.size()));
        list.add(firstLine);
        for (Game game : this) {
            list.add(game.gameToList());
        }
        return list;
    }
}