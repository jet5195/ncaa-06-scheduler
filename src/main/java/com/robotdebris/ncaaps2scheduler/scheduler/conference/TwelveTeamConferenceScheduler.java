package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            boolean xDivRivals = div1.getFirst().getXDivRival() != null;
            int numOfConfGames = conf.getNumOfConfGames();
            int year = gameRepository.getYear();

            // 8 conf games no rivals
            if (numOfConfGames == 8 && !xDivRivals) {
                this.schedule12Teams8GamesNoXDivRivals(conf);
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
        div1 = rotateDivByYear(div1, year, 6);
        int index = 0;
        for (School school : div1) {
            // schedule 9 games
            scheduleXDivGamesByIndex(school, div1, div2, index, getOpponentIndicesfor4Games(index));
            index++;
        }
    }

    private void schedule2CrossDivisionalGames(int index, School school, List<School> div2) {
        int opponent1Id = index + 1;
        if (opponent1Id > 5) {
            opponent1Id = 0;
        }
        if (div2.get(opponent1Id) == school.getXDivRival()) {
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
        if (div2.get(opponent2Id) == school.getXDivRival()) {
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

        int numOfConfGames = conf.getNumOfConfGames();
        int year = scheduleService.getYear();
        int i = 0;
        // if scheduleAbc == true, schedule ABC, else, XYZ
        boolean scheduleAbc = year % 4 < 2;
        for (School school : div1) {
            // on the 3rd team (halfway), swap scheduleAbc, so half the teams play xyz
            if (i == 3) {
                scheduleAbc = !scheduleAbc;
            }

            // if year is 0, 1, and school is 0, 1, 2... schedule abc, else xyz
            if (scheduleAbc) {
                for (int j = 0; j < div1.size() / 2; j++) {
                    School opponent = div2.get(j);
                    int week = scheduleService.findConfGameWeek(school, opponent);
                    if (((year % 2) + i + j) % 2 == 0) {
                        addYearlySeriesHelper(school, opponent, week, true);
                    } else {
                        addYearlySeriesHelper(opponent, school, week, true);
                    }
                }
            } else {
                for (int j = div1.size() / 2; j < div1.size(); j++) {
                    School opponent = div2.get(j);
                    int week = scheduleService.findConfGameWeek(school, opponent);
                    if (((year % 2) + i + j) % 2 == 0) {
                        addYearlySeriesHelper(opponent, school, week, true);
                    } else {
                        addYearlySeriesHelper(school, opponent, week, true);
                    }
                }
            }
            i++;
        }
        // 0 1 2 3
        // 1 1 2 3
        // 2 1 2 3
        // 3 4 5 6
        // 4 4 5 6
        // 5 4 5 6
    }

    private int[] getOpponentIndicesfor4Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = {{0, 1, 2, 3}, // Pattern for index 0
                {0, 1, 4, 5}, // Pattern for index 1
                {2, 3, 4, 5}, // Pattern for index 2
                {2, 3, 0, 1}, // Pattern for index 3
                {4, 5, 0, 1}, // Pattern for index 4
                {4, 5, 2, 3} // Pattern for index 5
        };

        // Return the opponent indices for the given index
        return opponentPatterns[index];
    }

    private int[] getOpponentIndicesfor3Games(int index) {
        // Define a 2D array representing the opponent indices for each index
        int[][] opponentPatterns = {{0, 1, 2}, // Pattern for index 0
                {3, 4, 5}, // Pattern for index 1
                {3, 4, 5}, // Pattern for index 2
                {0, 1, 2}, // Pattern for index 3
                {0, 1, 2}, // Pattern for index 4
                {3, 4, 5} // Pattern for index 5
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
        if (div2.get(opponent2Id).equals(school.getXDivRival())) {
            opponent2Id = (opponent2Id + 1) % div2.size();
        }
        opponent = div2.get(opponent2Id);
        week = scheduleService.findConfGameWeek(school, opponent);
        addYearlySeriesHelper(school, opponent, week, true);
    }

}
