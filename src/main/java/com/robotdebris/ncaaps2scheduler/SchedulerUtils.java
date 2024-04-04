package com.robotdebris.ncaaps2scheduler;

import com.robotdebris.ncaaps2scheduler.model.School;

import java.util.ArrayList;

public class SchedulerUtils {
    // finds empty weeks for one school
    public static ArrayList<Integer> findEmptyWeeks(School s) {
        ArrayList<Integer> freeWeeks = new ArrayList<>();
        ArrayList<Integer> usedWeeks = new ArrayList<>();
        for (int i = 0; i < s.getSchedule().size(); i++) {
            usedWeeks.add(s.getSchedule().get(i).getWeek());
        } // populates freeWeeks with 0-14, all the possible weeks for regular season
        // games
        for (int i = 0; i < 15; i++) {
            if (!usedWeeks.contains(i)) {
                freeWeeks.add(i);
            }
        }
        return freeWeeks;
    }

    public static ArrayList<Integer> findEmptyWeeksHelper(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }

    public static ArrayList<Integer> findEmptyWeeks(School s1, School s2) {// returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);

        boolean isInConference = s1.isInConference(s2);
        ArrayList<Integer> freeWeeks;
        if (isInConference) {
            freeWeeks = findEmptyWeeksInConferenceHelper(s1weeks, s2weeks, s1.getConference().getConfGamesStartWeek());
            if (freeWeeks.isEmpty()) {
                //TODO: do this some other way
                //freeWeeks = fixNoEmptyWeeks(s1, s2);
            }
        } else {
            freeWeeks = findEmptyWeeksHelper(s1weeks, s2weeks);
        }
        return freeWeeks;
    }

    public static int randomizeWeek(ArrayList<Integer> weeks) {
        int max = weeks.size() - 1;
        int min = 0;
        int range = max - min + 1;
        int randomNum = (int) (Math.random() * range) + min;
        return weeks.get(randomNum);
    }

    public static int randomizeWeek(School school, School opponent) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
        emptyWeeks.remove(Integer.valueOf(14));
        return randomizeWeek(emptyWeeks);
    }

    private static ArrayList<Integer> findEmptyWeeksInConferenceHelper(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks,
                                                                       int confGamesStartDate) {
        ArrayList<Integer> freeWeeks = findEmptyWeeksHelper(s1weeks, s2weeks);
        for (int i = 0; i < freeWeeks.size(); i++) {
            int week = freeWeeks.get(i);
            if (week < confGamesStartDate) {
                freeWeeks.remove(Integer.valueOf(week));
                i--;
            }
        }
        return freeWeeks;
    }
}
