import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Game implements Comparable {
    private School homeTeam;//ghtg
    private School awayTeam;//gatg
    private int time;//gtod 750, 930, 1080, 1200,
    private int day;//gdat 5 sat.. etc
    private int conferenceGame; // gmfx 0 if out of conference, 1 if conference
    private int week; //sewn & sewt
    private int userGame; //gffu & gfhu
    private int gameNumber; //sgnm, must be unique per week, highest num I've seen is 55

    public Game(int time, School awayTeam, School homeTeam, int gameNumber, int week, int day, int userGame, int conferenceGame) {
        this.time = time;
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.gameNumber = gameNumber;
        this.week = week;
        this.day = day;
        this.userGame = userGame;
        this.conferenceGame = conferenceGame;
    }

    public Game(School awayTeam, School homeTeam, int gameNumber, int week, int day) {
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.gameNumber = gameNumber;
        this.week = week;
        this.day = day;
        if (isRivalryGame()) {
            this.time = 1080;
        } else {
            this.time = 1200;
        }
        if (awayTeam.isUserTeam() || homeTeam.isUserTeam()) {
            this.userGame = 1;
        } else {
            this.userGame = 0;
        }
        if (awayTeam.getConference().equals(homeTeam.getConference())) {
            this.conferenceGame = 1;
        } else {
            this.conferenceGame = 0;
        }
    }

    public School getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(School homeTeam) {
        this.homeTeam = homeTeam;
    }

    public School getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(School awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getConferenceGame() {
        return conferenceGame;
    }

    public void setConferenceGame(int conferenceGame) {
        this.conferenceGame = conferenceGame;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getUserGame() {
        return userGame;
    }

    public void setUserGame(int userGame) {
        this.userGame = userGame;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    /**
     * Returns true if game is a rivalry game
     * @return true if game is a rivalry game, false if else
     */
    public boolean isRivalryGame() {
        return this.getHomeTeam().isRival(this.getAwayTeam());
    }

    /**
     * Returns true if a game is not a rivalry and is not in conference
     * @return true if a game is not a rivalry and not in conference, false if else
     */
    public boolean isRemovableGame() {
        return this.getConferenceGame() == 0 && !this.isRivalryGame();
    }

    /**
     *
     * @return an arrayList of all the data of a specific game, is used when creating the output excel file
     */
    public ArrayList gameToList() {
        ArrayList<Integer> list = new ArrayList();
        int i = 0;
        while (i < 14) {
            if (i == 3) {
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
                list.add(this.getDay());
            } else if (i == 10) {
                list.add(this.getWeek());
            } else if (i == 11) {
                list.add(this.getUserGame());
            } else if (i == 12) {
                list.add(this.getUserGame());
            } else if (i == 13) {
                list.add(this.getConferenceGame());
            } else {
                list.add(0);
            }
            i++;
        }
        return list;
    }

    /**
     * Compares 2 games by week ascending.
     * @param game2 the game to compare
     * @return a negative number if this game is before game2 in terms of what week they play
     */
    public int compareTo(@NotNull Object game2) {
        int compareWeek = ((Game) game2).getWeek();
        return this.getWeek() - compareWeek;
    }
}
