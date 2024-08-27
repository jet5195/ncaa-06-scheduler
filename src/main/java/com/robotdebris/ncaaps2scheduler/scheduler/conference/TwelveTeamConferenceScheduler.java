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

            // schedule inner division games
            scheduleRoundRobinConfGames(div1);
            scheduleRoundRobinConfGames(div2);

            // order by cross div rivals
            boolean xDivRivals = div1.getFirst().getxDivRival() != null;
            int numOfConfGames = conf.getNumOfConfGames();
            int year = gameRepository.getYear();

            if (xDivRivals) {
                div2 = orderDivByXDivRivals(div1);
            }

            div1 = rotateDivByYear(div1, year % 6);
            int index = 0;
            for (School school : div1) {
                if (numOfConfGames == 8) {
                    if (!xDivRivals) {
                        scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor3Games(index));
                    } else {
                        scheduleCrossDivisionalRival(div1, div2, school);
                        scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor2Games(index));
                    }
                }
                if (numOfConfGames == 9) {
                    if (!xDivRivals) {
                        scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor4Games(index));
                    } else {
                        scheduleCrossDivisionalRival(div1, div2, school);
                        scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor3Games(index));
                    }
                }
                index++;
            } // end of for team loop

        } catch (

        IndexOutOfBoundsException e) {
            throw e;
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
        int[][] opponentPatterns = { { 1, 0, 2 }, // Pattern for index 0
                { 3, 5, 4 }, // Pattern for index 1
                { 0, 5, 1 }, // Pattern for index 2
                { 2, 4, 3 }, // Pattern for index 3
                { 2, 1, 3 }, // Pattern for index 4
                { 4, 0, 5 } // Pattern for index 5
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
                { 5, 0 } // Pattern for index 5
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }
}
