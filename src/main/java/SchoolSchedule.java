import java.util.LinkedList;

public class SchoolSchedule extends LinkedList<Game> {
    public Game getGame(int week) {
        for (Game game : this) {
            if (game.getWeek() == week) {
                return game;
            }
        }
        return null;
    }

    protected int findGameNumber(int week) {
        int gameNumber = 0;
        for (Game theGame : this) {
            gameNumber = theGame.getWeek() == week && gameNumber < theGame.getGameNumber() ? theGame.getGameNumber() : gameNumber;
        }
        return ++gameNumber;
    }
}