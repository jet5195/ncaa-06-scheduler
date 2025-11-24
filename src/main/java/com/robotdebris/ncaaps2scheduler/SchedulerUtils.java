package com.robotdebris.ncaaps2scheduler;

import java.util.ArrayList;
import java.util.List;

public class SchedulerUtils {

    public static List<Integer> findEmptyWeeksHelper(List<Integer> s1weeks, List<Integer> s2weeks) {
        List<Integer> freeWeeks = new ArrayList<>();
        for (Integer s1week : s1weeks) {
            if (s2weeks.contains(s1week)) {
                freeWeeks.add(s1week);
            }
        }
        return freeWeeks;
    }


    public static List<Integer> findEmptyWeeksInConferenceHelper(List<Integer> s1weeks, List<Integer> s2weeks,
                                                                 int confGamesStartDate) {
        List<Integer> freeWeeks = findEmptyWeeksHelper(s1weeks, s2weeks);
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
