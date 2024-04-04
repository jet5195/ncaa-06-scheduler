package com.robotdebris.ncaaps2scheduler;

import java.util.ArrayList;

public class SchedulerUtils {

    public static ArrayList<Integer> findEmptyWeeksHelper(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }


    public static ArrayList<Integer> findEmptyWeeksInConferenceHelper(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks,
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
