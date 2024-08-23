package com.robotdebris.ncaaps2scheduler.scheduler.conference;

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
    public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) {
        try {
            scheduleConferenceGamesDivisions14(conference, gameRepository);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated method stub

    }

    private void scheduleConferenceGamesDivisions14(Conference conf, GameRepository gameRepository) throws Exception {
        List<School> div1 = conf.getDivisions().get(0).getSchools();
        List<School> div2 = conf.getDivisions().get(1).getSchools();

        // schedule inner div games
        scheduleRoundRobinConfGames(div1);
        scheduleRoundRobinConfGames(div2);

        boolean xDivRivals = div1.getFirst().getxDivRival() != null;
        int numOfConfGames = conf.getNumOfConfGames();
        int year = gameRepository.getYear();

        if (xDivRivals) {
            div2 = orderDivByXDivRivals(div1);
        }
        div1 = rotateDivByYear(div1, year % 7);
        int index = 0;
        for (School school : div1) {
            if (numOfConfGames == 9) {
                if (!xDivRivals) {
                    scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor3Games(index));
                } else {
                    scheduleCrossDivisionalRival(div1, div2, school);
                    scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor2Games(index));
                }
            }
            if (numOfConfGames == 8) {
                if (!xDivRivals) {
                    scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor2Games(index));
                } else {
                    scheduleCrossDivisionalRival(div1, div2, school);
                    scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor1Games(index));
                }
            }
            index++;
        }

    }

    private int[] getOpponentIndicesfor3Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = { { 0, 1, 2 }, // Pattern for index 0
                { 1, 2, 3 }, // Pattern for index 1
                { 2, 3, 4 }, // Pattern for index 2
                { 3, 4, 5 }, // Pattern for index 3
                { 4, 5, 6 }, // Pattern for index 4
                { 5, 6, 0 }, // Pattern for index 5
                { 6, 0, 1 } // Pattern for index 6
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

    private int[] getOpponentIndicesfor2Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = { { 0, 1 }, // Pattern for index 0
                { 1, 2 }, // Pattern for index 1
                { 2, 3 }, // Pattern for index 2
                { 3, 4 }, // Pattern for index 3
                { 4, 5 }, // Pattern for index 4
                { 5, 6 }, // Pattern for index 5
                { 6, 0 } // Pattern for index 6
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

    private int[] getOpponentIndicesfor1Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = { { 0 }, // Pattern for index 0
                { 1 }, // Pattern for index 1
                { 2 }, // Pattern for index 2
                { 3 }, // Pattern for index 3
                { 4 }, // Pattern for index 4
                { 5 }, // Pattern for index 5
                { 6 } // Pattern for index 6
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

}
