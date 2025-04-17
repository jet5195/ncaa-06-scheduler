package com.robotdebris.ncaaps2scheduler.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Game implements Comparable {
    private School awayTeam;// gatg
    private School homeTeam;// ghtg
    private GameResult gameResult;
    //    private int awayScore = 0;//gasc
//    private int homeScore = 0;//ghsc
    // private GameTime gameTime;//gtod, gdat
    private int time;// gtod 750 = 12:30 pm, 930 = 3:30 pm, 1080 = 6:00 pm, 1200 = 8:00 pm,
    private DayOfWeek day;// gdat 5 sat.. etc
    private boolean conferenceGame; // gmfx 0 if out of conference, 1 if conference
    private int week; // sewn & sewt
    //    private int ot; //gfot
    private int weight; // sewt (usually = sewn, but not for bowls) if it's a bowlGame, it's week+12
    private boolean userGame; // gffu & gfhu
    private int gameNumber; // sgnm, must be unique per week, highest num I've seen is 55

    public Game() {

    }

    public Game(GameResult gameResult, int time, School awayTeam, School homeTeam, int gameNumber, int week,
                DayOfWeek day, boolean userGame, boolean conferenceGame) {
        this.gameResult = gameResult;
        this.time = time;
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.gameNumber = gameNumber;
        this.week = week;
        this.day = day;
        this.userGame = userGame;
        this.conferenceGame = conferenceGame;
    }

    public Game(School awayTeam, School homeTeam, int gameNumber, int week, DayOfWeek day) {
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.gameNumber = gameNumber;
        this.week = week;
        this.day = day;
        // 750 = 12:30 pm, 930 = 3:30 pm, 1080 = 6:00 pm, 1200 = 8:00 pm,
        boolean random = Math.random() < .5;
        if (isRivalryGame()) {
            if (random) {
                this.time = 930;
            } else {
                this.time = 1200;
            }
        } else {
            if (random) {
                this.time = 750;
            } else {
                this.time = 1080;
            }
        }
        this.gameResult = new GameResult(0, 0, 0);
        this.userGame = awayTeam.isUserTeam() || homeTeam.isUserTeam();
        this.conferenceGame = awayTeam.getConference().equals(homeTeam.getConference());
    }

    public Game(School awayTeam, School homeTeam, int gameNumber, int week, DayOfWeek day, int time,
                GameResult gameResult) {
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.gameNumber = gameNumber;
        this.week = week;
        this.day = day;
        // 750 = 12:30 pm, 930 = 3:30 pm, 1080 = 6:00 pm, 1200 = 8:00 pm,
        this.time = time;
        this.gameResult = gameResult;
        this.userGame = awayTeam.isUserTeam() || homeTeam.isUserTeam();
        this.conferenceGame = awayTeam.getConference().equals(homeTeam.getConference());
    }

    public School getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(School homeTeam) {
        this.homeTeam = homeTeam;
        if (this.awayTeam != null) {
            this.setConferenceGame(this.homeTeam.isInConference(this.awayTeam));
        }
    }

    public School getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(School awayTeam) {
        this.awayTeam = awayTeam;
        if (this.homeTeam != null) {
            this.setConferenceGame(this.homeTeam.isInConference(this.awayTeam));
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public boolean isConferenceGame() {
        return conferenceGame;
    }

    public void setConferenceGame(boolean conferenceGame) {
        this.conferenceGame = conferenceGame;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public boolean isUserGame() {
        return userGame;
    }

    public void setUserGame(boolean userGame) {
        this.userGame = userGame;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Returns true if game is a rivalry game
     *
     * @return true if game is a rivalry game, false if else
     */
    public boolean isRivalryGame() {
        return this.getHomeTeam().isRival(this.getAwayTeam());
    }

    /**
     * Returns true if a game is not a rivalry and is not in conference
     *
     * @return true if a game is not a rivalry and not in conference, false if else
     */
    public boolean isNonConferenceAndNonRivalry() {
        return !this.isConferenceGame() && !this.isRivalryGame();
    }

    public School getWinner() {
        if (this.getGameResult().getHomeScore() > this.gameResult.getAwayScore()) {
            return this.getHomeTeam();
        } else
            return this.getAwayTeam();
    }

    /**
     * @return an arrayList of all the data of a specific game, is used when
     * creating the output excel file
     */
    public List<Integer> gameToList() {
        List<Integer> list = new ArrayList<>();
        int i = 0;
        while (i < 14) {
            if (i == 0) {
                list.add(this.getGameResult().getOt());
            } else if (i == 1) {
                list.add(this.getGameResult().getAwayScore());
            } else if (i == 2) {
                list.add(this.getGameResult().getHomeScore());
            } else if (i == 3) {
                list.add(this.getTime());
            } else if (i == 4) {
                list.add(this.getAwayTeam().getTgid());
            } else if (i == 5) {
                list.add(this.getHomeTeam().getTgid());
            } else if (i == 6) {
                list.add(this.getGameNumber());
            } else if (i == 7) {
                list.add(this.getWeek());
            } else if (i == 8) {
                list.add(this.getDay().getDayIndex());
            } else if (i == 10) {
                if (this.getWeek() < 18) {
                    list.add(this.getWeek());
                } else {
                    list.add(this.getWeek() + 12);
                }
            } else if (i == 11) {
                list.add(this.isUserGame() ? 1 : 0);
            } else if (i == 12) {
                list.add(this.isUserGame() ? 1 : 0);
            } else if (i == 13) {
                list.add(this.isConferenceGame() ? 1 : 0);
            } else {
                list.add(0);
            }
            i++;
        }
        return list;
    }

    /**
     * Compares 2 games by week ascending.
     *
     * @param game2 the game to compare
     * @return a negative number if this game is before game2 in terms of what week
     * they play
     */
    @Override
    public int compareTo(@NotNull Object game2) {
        int compareWeek = ((Game) game2).getWeek();
        return this.getWeek() - compareWeek;
    }

    public boolean involvesTeam(School School) {
        return homeTeam.equals(School) || awayTeam.equals(School);
    }

    @Override
    public String toString() {
        return awayTeam + " at " + homeTeam;
    }

}
