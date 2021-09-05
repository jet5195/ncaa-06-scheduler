package com.robotdebris.ncaaps2scheduler.model;
import java.util.LinkedList;

public class SchoolSchedule extends LinkedList<Game> {

    /**
     * Finds a game in a school's schedule for a given week
     * @param week the week to search for
     * @return a game of the given week, null if no game is found
     */
    public Game getGame(int week) {
        for (Game game : this) {
            if (game.getWeek() == week) {
                return game;
            }
        }
        return null;
    }

    /**
     * Returns a new game number for an added game, is always one higher than the currently highest game number for the given week
     * @param week week of the game
     * @return the new game number
     */
    protected int findGameNumber(int week) {
        int gameNumber = 0;
        for (Game theGame : this) {
            gameNumber = theGame.getWeek() == week && gameNumber < theGame.getGameNumber() ? theGame.getGameNumber() : gameNumber;
        }
        return ++gameNumber;
    }

    public boolean isFull() {
        return this.size() >= 12;
    }
}