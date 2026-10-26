package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;

@Component
public class TwelveTeamConferenceScheduler extends AbstractConferenceScheduler {

    // Four cross-division games per season; each pair meets four times in six
    // years. Row order is part of the existing opponent rotation.
    private static final int[][] FOUR_GAME_OPPONENT_PATTERNS = {
            {0, 1, 2, 3}, {0, 1, 4, 5}, {2, 3, 4, 5},
            {2, 3, 0, 1}, {4, 5, 0, 1}, {4, 5, 2, 3}
    };

    @Override
    public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) throws Exception {
        scheduleConferenceGamesDivisions12(conference, gameRepository);
    }

    public void scheduleConferenceGamesDivisions12(Conference conf, GameRepository gameRepository) throws Exception {
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

        int year = gameRepository.getYear();

        if (numOfConfGames == 8 && !xDivRivals) {
            schedule8GamesWithoutRivals(conf);
        } else if (numOfConfGames == 9 && xDivRivals) {
            schedule9GamesWithXDivRivals(div1, year);
        } else if (xDivRivals) {
            schedule8GamesWithXDivRivals(div1, div2, year);
        } else {
            schedule9Games(div1, div2, year);
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
            for (int opponentIndex : fourGameOpponentIndices(index)) {
                School opponent = div2.get(opponentIndex);
                // Keep the existing opponents, but flip venues on actual meetings,
                // not on calendar years in which this pair might not play.
                boolean home = (originalIndex + opponentIndex) % 2 != 0;
                if (priorMeetingsInSixYearCycle(originalIndex, opponentIndex, cycleYear) % 2 != 0) {
                    home = !home;
                }
                scheduleConferenceGame(school, opponent, home);
            }
        }
    }

    private int priorMeetingsInSixYearCycle(int schoolIndex, int opponentIndex, int cycleYear) {
        int meetings = 0;
        for (int earlierYear = 0; earlierYear < cycleYear; earlierYear++) {
            int earlierIndex = (schoolIndex + earlierYear) % 6;
            for (int earlierOpponent : fourGameOpponentIndices(earlierIndex)) {
                if (earlierOpponent == opponentIndex) meetings++;
            }
        }
        // Every pair meets four times per six-year cycle. Four venue flips return
        // to the base venue, so this count also works across cycle boundaries.
        return meetings;
    }

    private void schedule2CrossDivisionalGames(int index, School school, List<School> div2) {
        int firstOpponent = nextRotatingOpponentIndex(index, school, div2);
        scheduleConferenceGame(school, div2.get(firstOpponent), true);

        int secondOpponent = nextRotatingOpponentIndex(firstOpponent, school, div2);
        scheduleConferenceGame(school, div2.get(secondOpponent), false);
    }

    private int nextRotatingOpponentIndex(int previousIndex, School school, List<School> opponents) {
        int index = (previousIndex + 1) % opponents.size();
        if (opponents.get(index) == school.getxDivRival()) index = (index + 1) % opponents.size();
        return index;
    }

    private void schedule8GamesWithoutRivals(Conference conf) throws Exception {
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
                scheduleConferenceGame(school, opponent, home);
            }
        }
    }

    private int[] fourGameOpponentIndices(int index) {
        return FOUR_GAME_OPPONENT_PATTERNS[index];
    }

}
