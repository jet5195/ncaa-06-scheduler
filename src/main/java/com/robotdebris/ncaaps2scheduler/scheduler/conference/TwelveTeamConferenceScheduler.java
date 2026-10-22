package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;

@Component
public class TwelveTeamConferenceScheduler extends AbstractConferenceScheduler {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    SchoolService schoolService;

    @Override
    public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) throws Exception {
        if (schoolService == null) {
            System.out.println("school service is null");
        }
        if (scheduleService == null) {
            System.out.println("schedule service is null");
        }
        scheduleConferenceGamesDivisions12(conference, gameRepository);
    }

    public void scheduleConferenceGamesDivisions12(Conference conf, GameRepository gameRepository) throws Exception {
        try {
            // int index = 0;
            // move school order by year

            List<School> div1 = new ArrayList<>(conf.getDivisions().get(0).getSchools());
            List<School> div2 = new ArrayList<>(conf.getDivisions().get(1).getSchools());

            boolean xDivRivals = div1.getFirst().getxDivRival() != null;
            int numOfConfGames = conf.getNumOfConfGames();
            if (numOfConfGames == 8 && xDivRivals) {
                div2 = orderDivByXDivRivals(div1);
            }
            // schedule inner division games
            scheduleRoundRobinConfGames(div1);
            // Five divisional games give alternating teams three or two home games.
            // With eight games and protected rivals, paired schools need
            // complementary divisional totals to finish with four home games.
            scheduleRoundRobinConfGames(div2, numOfConfGames == 8 && xDivRivals);

            // order by cross div rivals
            int year = gameRepository.getYear();

            // 8 conf games no rivals
            if (numOfConfGames == 8 && !xDivRivals) {
                this.schedule12Teams8GamesNoXDivRivals(conf);
            } else if (numOfConfGames == 9 && xDivRivals) {
                schedule9GamesWithXDivRivals(div1, year);
            } else if (xDivRivals) {
                schedule8GamesWithXDivRivals(div1, div2, year);
            } else {
                schedule9Games(div1, div2, year);
            }

        } catch (

        IndexOutOfBoundsException e) {
            throw e;
        }
    }

    private void schedule9GamesWithXDivRivals(List<School> div1, int year) {
        List<School> pairedDivision = orderDivByXDivRivals(div1);
        // Offset zero is the annual protected matching. Three rotating slots
        // give div1 two home games in even years and one in odd years; this
        // protected game supplies the other home game for an annual 2/2 split.
        scheduleCrossDivisionMatching(div1, pairedDivision, 0, Math.floorMod(year, 2) == 1);

        // Walk offsets 1..5 continuously, taking three each season. Five years
        // contain three meetings per rotating opponent; ten contain six, with
        // three at each venue. A pair returns every five slots (odd), so flipping
        // the host side by slot reverses the venue on every next meeting.
        int firstSlot = 3 * Math.floorMod(year, 10);
        for (int slot = firstSlot; slot < firstSlot + 3; slot++) {
            scheduleCrossDivisionMatching(div1, pairedDivision, 1 + slot % 5, slot % 2 == 0);
        }
    }

    private void scheduleCrossDivisionMatching(List<School> div1, List<School> div2,
            int offset, boolean home) {
        // A complete matching gives every school in BOTH divisions one game.
        for (int index = 0; index < div1.size(); index++) {
            School school = div1.get(index);
            School opponent = div2.get((index + offset) % div2.size());
            int week = scheduleService.findConfGameWeek(school, opponent);
            addYearlySeriesHelper(home ? opponent : school, home ? school : opponent, week, true);
        }
    }

    private void schedule8GamesWithXDivRivals(List<School> div1, List<School> div2, int year) throws Exception {
        div2 = orderDivByXDivRivals(div1);
        div1 = rotateDivByYear(div1, year, 5);
        int index = 0;
        for (School school : div1) {
            scheduleCrossDivisionalRival(div1, div2, school);
            schedule2CrossDivisionalGames(index, school, div2);
            index++;
        }
    }

    private void schedule9Games(List<School> div1, List<School> div2, int year) throws Exception {
        int cycleYear = Math.floorMod(year, 6);
        List<School> rotatedDivision = rotateDivByYear(new ArrayList<>(div1), cycleYear, 6);
        for (int index = 0; index < rotatedDivision.size(); index++) {
            School school = rotatedDivision.get(index);
            int originalIndex = div1.indexOf(school);
            for (int opponentIndex : getOpponentIndicesfor4Games(index)) {
                School opponent = div2.get(opponentIndex);
                // Keep the existing opponents, but flip venues on actual meetings,
                // not on calendar years in which this pair might not play.
                boolean home = (originalIndex + opponentIndex) % 2 != 0;
                if (priorMeetingsInSixYearCycle(originalIndex, opponentIndex, cycleYear) % 2 != 0) {
                    home = !home;
                }
                int week = scheduleService.findConfGameWeek(school, opponent);
                addYearlySeriesHelper(home ? opponent : school, home ? school : opponent, week, true);
            }
        }
    }

    private int priorMeetingsInSixYearCycle(int schoolIndex, int opponentIndex, int cycleYear) {
        int meetings = 0;
        for (int earlierYear = 0; earlierYear < cycleYear; earlierYear++) {
            int earlierIndex = (schoolIndex + earlierYear) % 6;
            for (int earlierOpponent : getOpponentIndicesfor4Games(earlierIndex)) {
                if (earlierOpponent == opponentIndex) meetings++;
            }
        }
        // Every pair meets four times per six-year cycle. Four venue flips return
        // to the base venue, so this count also works across cycle boundaries.
        return meetings;
    }

    private void schedule2CrossDivisionalGames(int index, School school, List<School> div2) {
        int opponent1Id = index + 1;
        if (opponent1Id > 5) {
            opponent1Id = 0;
        }
        if (div2.get(opponent1Id) == school.getxDivRival()) {
            opponent1Id = opponent1Id + 1;
            if (opponent1Id > 5) {
                opponent1Id = 0;
            }
        }
        School opponent = div2.get(opponent1Id);
        int week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(opponent, school, week, true);

        int opponent2Id = opponent1Id + 1;
        if (opponent2Id > 5) {
            opponent2Id = 0;
        }
        if (div2.get(opponent2Id) == school.getxDivRival()) {
            opponent2Id = opponent2Id + 1;
            if (opponent2Id > 5) {
                opponent2Id = 0;
            }
        }
        opponent = div2.get(opponent2Id);
        week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(school, opponent, week, true);
    }

    private void schedule12Teams8GamesNoXDivRivals(Conference conf) throws Exception {
        List<School> div1 = conf.getDivisions().get(0).getSchools();
        List<School> div2 = conf.getDivisions().get(1).getSchools();
        int year = scheduleService.getYear();
        // Play each group of three complete matchings for a home-and-away pair
        // of seasons. Every cross-division opponent is visited at both venues
        // in four years, without an unintended annual protected opponent.
        boolean firstGroup = Math.floorMod(Math.floorDiv(year, 2), 2) == 0;
        int[] offsets = firstGroup ? new int[]{0, 1, 2} : new int[]{3, 4, 5};
        for (int offset : offsets) {
            for (int index = 0; index < div1.size(); index++) {
                School school = div1.get(index);
                School opponent = div2.get((index + offset) % div2.size());
                // In even years, even-indexed schools have three divisional
                // home games and need one cross-division home game; odd-indexed
                // schools need two. These assignments also balance div2.
                // Keep each matchup's base venue fixed across cycle repeats.
                boolean home = firstGroup
                        ? (index % 2 == 0 ? offset == 0 : offset != 0)
                        : (index % 2 == 0 ? offset == 4 : offset != 3);
                if (Math.floorMod(year, 2) == 1) home = !home;
                int week = scheduleService.findConfGameWeek(school, opponent);
                addYearlySeriesHelper(home ? opponent : school, home ? school : opponent, week, true);
            }
        }
    }

    private int[] getOpponentIndicesfor4Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = { { 0, 1, 2, 3 }, // Pattern for index 0
                { 0, 1, 4, 5 }, // Pattern for index 1
                { 2, 3, 4, 5 }, // Pattern for index 2
                { 2, 3, 0, 1 }, // Pattern for index 3
                { 4, 5, 0, 1 }, // Pattern for index 4
                { 4, 5, 2, 3 } // Pattern for index 5
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

    private int[] getOpponentIndicesfor3Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = { { 0, 1, 2 }, // Pattern for index 0
                { 3, 4, 5 }, // Pattern for index 1
                { 3, 4, 5 }, // Pattern for index 2
                { 0, 1, 2 }, // Pattern for index 3
                { 0, 1, 2 }, // Pattern for index 4
                { 3, 4, 5 } // Pattern for index 5
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

    private void schedule8GamesByIndex(School school, List<School> div2, int index) throws Exception {
        // Schedule the first opponent
        School opponent = div2.get((index + 1) % div2.size());
        int week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(opponent, school, week, true);

        // Find the next opponent, ensuring they're not the xDivRival
        int opponent2Id = (index + 2) % div2.size();
        if (div2.get(opponent2Id).equals(school.getxDivRival())) {
            opponent2Id = (opponent2Id + 1) % div2.size();
        }
        opponent = div2.get(opponent2Id);
        week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(school, opponent, week, true);
    }

}
