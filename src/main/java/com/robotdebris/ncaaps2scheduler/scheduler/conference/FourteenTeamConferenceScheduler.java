package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@Component
public class FourteenTeamConferenceScheduler extends AbstractConferenceScheduler {

    @Autowired
    ScheduleService scheduleService;

    @Override
    public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) throws Exception {
        scheduleConferenceGamesDivisions14(conference, gameRepository);
    }

    private void scheduleConferenceGamesDivisions14(Conference conf, GameRepository gameRepository) throws Exception {
        List<School> div1 = new ArrayList<>(conf.getDivisions().get(0).getSchools());
        List<School> div2 = new ArrayList<>(conf.getDivisions().get(1).getSchools());

        // schedule inner div games
        scheduleRoundRobinConfGames(div1);
        scheduleRoundRobinConfGames(div2);

        boolean xDivRivals = div1.getFirst().getxDivRival() != null;
        int numOfConfGames = conf.getNumOfConfGames();
        int year = gameRepository.getYear();
        // Extend the rotation backward through historical seasons instead of
        // reflecting it around 2005 and repeating the same opponents/venues.
        int yearOffset = year - 2005;

        // Handle scheduling based on the number of conference games and cross-division
        // rivals
        if (numOfConfGames == 8) {
            if (!xDivRivals) {
                schedule8WithoutRivals(div1, div2, yearOffset);
            } else {
                schedule8WithRivals(div1, div2, yearOffset);
            }
        } else if (numOfConfGames == 9) {
            schedule9CrossDivisionalGames(div1, div2, year, xDivRivals);
        }
    }

    private void schedule9CrossDivisionalGames(List<School> div1, List<School> div2,
            int year, boolean protectedRivals) {
        if (!protectedRivals) {
            schedule9WithoutRivals(div1, div2, year);
            return;
        }
        int seasonPair = Math.floorDiv(year, 2);
        div2 = orderDivByXDivRivals(div1);
        // Zero is the protected matching. Rotate one other home matching
        // (offsets 1..3) and one away matching (4..6) over three season pairs.
        int rotation = Math.floorMod(seasonPair, 3);
        int[] offsets = {0, 1 + rotation, 4 + rotation};
        // Each offset is a complete matching: every team in BOTH divisions gets
        // one game. Six divisional games supply three home games; two home
        // cross-division matchings yield five home games for div1 and four for
        // div2 in even years. Odd years reverse every matchup and those totals.
        for (int offset : offsets) {
            for (int index = 0; index < div1.size(); index++) {
                School school = div1.get(index);
                School opponent = div2.get((index + offset) % div2.size());
                int week = scheduleService.findConfGameWeek(school, opponent);
                // Keep a matchup's base venue tied to its stable offset, not
                // its position in this year's list. Opponents play two-year
                // venue pairs, so the next meeting swaps host even after gaps.
                boolean home = offset < 4;
                if (Math.floorMod(year, 2) == 1) home = !home;
                addYearlySeriesHelper(home ? opponent : school, home ? school : opponent, week, true);
            }
        }
    }

    private void schedule9WithoutRivals(List<School> div1, List<School> div2, int year) {
        // Walk the seven complete matchings in order, taking three each season.
        // Seven seasons consume 21 slots: exactly three meetings per opponent.
        // Alternate the host side by slot. A season advances by three (odd),
        // so its 2/1 cross-division home split flips annually. A matchup returns
        // after seven slots (also odd), so every next meeting reverses venue.
        // Fourteen seasons consume 42 slots and complete the opponent/venue cycle.
        int firstSlot = 3 * Math.floorMod(year, 14);
        for (int slot = firstSlot; slot < firstSlot + 3; slot++) {
            int offset = slot % 7;
            boolean home = slot % 2 == 0;
            for (int index = 0; index < div1.size(); index++) {
                School school = div1.get(index);
                School opponent = div2.get((index + offset) % div2.size());
                int week = scheduleService.findConfGameWeek(school, opponent);
                addYearlySeriesHelper(home ? opponent : school, home ? school : opponent, week, true);
            }
        }
    }

    private void schedule8WithRivals(List<School> div1, List<School> div2, int yearOffset)
            throws Exception {
        div2 = orderDivByXDivRivals(div1);
        // add protected rivalry games
        int i = 0;
        for (School school : div1) {
            School opponent = div2.get(i);
            int week = scheduleService.findConfGameWeek(school, opponent);
            addYearlySeriesHelper(school, opponent, week, false);
            i++;
        }
        // at this point all we have to do is alternate that 1 game.. home away SHOULD
        // be taken care of too due to xdivrival
        int year = Math.floorMod(yearOffset, 12) + 1;
        int j = year;
        for (School school : div1) {
            // if we're in years 6-11, alternate the schedule a bit. IE, play team 2 then 1,
            // so we actually swap home/away
            if (year >= 7) {
                if (j >= 7) {
                    // odd & we only go back 5 for the first school!!
                    if (j % 2 != 0 && div1.get(0).equals(school)) {
                        j -= 5;
                        // even
                    } else {
                        j -= 7;
                    }
                }
            } else {
                if (j >= 7) {
                    j -= 7;
                }
            }
            School opponent = div2.get(j);
            int week = scheduleService.findConfGameWeek(school, opponent);
            if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) < 4) {
                addYearlySeriesHelper(opponent, school, week, true);
            } else {
                addYearlySeriesHelper(school, opponent, week, true);
            }
            j++;
        }
        /*
         * 0 1 2 3 4 5 6 _____________________________ 2010 1 2 3 4 5 6 0 2011 2 3 4 5 6
         * 0 1 2012 3 4 5 6 0 1 2 2013 4 5 6 0 1 2 3 2014 5 6 0 1 2 3 4 2015 6 0 1 2 3 4
         * 5 2016 1 2 3 4 5 6 0
         */

    }

    private void schedule8WithoutRivals(List<School> div1, List<School> div2, int yearOffset) throws Exception {
        // so there's 7 different schedules, figure it out based on the year
        int modulo = Math.floorMod(yearOffset, 7);
        for (int i = 0; i < 7; i++) {
            School school = div1.get(i);
            int firstOpponent = i + modulo;
            if (firstOpponent >= 7) {
                firstOpponent -= 7;
            }
            School opponent = div2.get(firstOpponent);
            int week = scheduleService.findConfGameWeek(school, opponent);
            addYearlySeriesHelper(school, opponent, week, true);

            int secondOpponent = firstOpponent + 1;
            if (secondOpponent >= 7) {
                secondOpponent -= 7;
            }
            School opponent2 = div2.get(secondOpponent);
            int week2 = scheduleService.findConfGameWeek(school, opponent2);
            addYearlySeriesHelper(opponent2, school, week2, true);
        }
        /*
         *
         * 0: 0 1 1 2 2 3 3 4 4 5 5 6 6 0 1: 1 2 2 3 3 4 4 5 5 6 6 0 0 1 2: 2 3 3 4 4 5
         * 5 6 6 0 0 1 1 2 3: 3 4 4 5 5 6 6 0 0 1 1 2 2 3 4: 4 5 5: 5 6 6: 6 0
         */

    }

}
