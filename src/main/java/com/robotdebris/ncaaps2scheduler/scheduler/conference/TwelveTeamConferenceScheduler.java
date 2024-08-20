package com.robotdebris.ncaaps2scheduler.scheduler.conference;

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

            List<School> div1 = conf.getDivisions().get(0).getSchools();
            List<School> div2 = conf.getDivisions().get(1).getSchools();

            // schedule inner division games
            scheduleRoundRobinConfGames(div1);
            scheduleRoundRobinConfGames(div2);

            // order by cross div rivals
            boolean xDivRivals = div1.getFirst().getxDivRival() != null;
            int numOfConfGames = conf.getNumOfConfGames();
            int year = gameRepository.getYear();

            // 8 conf games no rivals
            if (numOfConfGames == 8 && !xDivRivals) {
                this.schedule12Teams8GamesNoXDivRivals(conf);
            } else {

                if (xDivRivals) {
                    div2 = orderDivByXDivRivals(div1);
                }

                int rotationAmount;
                if (numOfConfGames == 9) {
                    rotationAmount = year % 6;
                    for (int i = 0; i < rotationAmount; i++) {
                        School s1 = div1.removeLast();
                        div1.addFirst(s1);
                    }
                }
                if (numOfConfGames == 8) {
                    rotationAmount = year % 5;
                    for (int i = 0; i < rotationAmount; i++) {
                        School s1 = div1.removeLast();
                        div1.addFirst(s1);
                    }
                }

                int index = 0;
                for (School school : div1) {
                    if (numOfConfGames == 8 && xDivRivals) {
                        // schedule8GamesXDivRivals(div1, div2, school);
                        scheduleCrossDivisionalRival8Games(div1, div2, school);
                        schedule2CrossDivisionalGames(index, school, div2);
                    }

                    // 0 1 2 3
                    if (numOfConfGames == 9) {
                        schedule4XDivGamesByIndex(school, div1, div2, index);
                    }
                    index++;
                } // end of for team loop
            }

        } catch (

        IndexOutOfBoundsException e) {
            throw e;
        }
        // }
        /*
         * year 1 0 6 7 0 0 8 9 0
         *
         * 6 1 1 7 10 1 1 11
         *
         * 2 8 9 2 2 10 11 2
         *
         * 8 3 3 9 6 3 3 7
         *
         * 4 10 11 4 4 6 7 4
         *
         * 10 5 5 11 8 5 5 9
         */
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

    private void scheduleCrossDivisionalRival8Games(List<School> div1, List<School> div2, School school)
            throws Exception {
        School opponent = school.getxDivRival();
        int week = scheduleService.findConfGameWeek(school, opponent);
        // should be home or away game?
        if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= div1.size() / 2) {
            addYearlySeriesHelper(school, opponent, week, true);
        } else {
            addYearlySeriesHelper(opponent, school, week, true);
        }
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

                    if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= numOfConfGames / 2) {
                        addYearlySeriesHelper(school, opponent, week, true);
                    } else if (scheduleService.getNumOfAwayConferenceGamesForSchool(school) >= numOfConfGames / 2) {
                        addYearlySeriesHelper(opponent, school, week, true);
                    } else {
                        addYearlySeriesHelper(opponent, school, week, false);
                    }
                }
            } else {
                for (int j = div1.size() / 2; j < div1.size(); j++) {
                    School opponent = div2.get(j);
                    int week = scheduleService.findConfGameWeek(school, opponent);

                    if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= numOfConfGames / 2) {
                        addYearlySeriesHelper(school, opponent, week, true);
                    } else if (scheduleService.getNumOfAwayConferenceGamesForSchool(school) >= numOfConfGames / 2) {
                        addYearlySeriesHelper(opponent, school, week, true);
                    } else {
                        addYearlySeriesHelper(opponent, school, week, false);
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

    private void schedule4XDivGamesByIndex(School school, List<School> div1, List<School> div2, int index)
            throws Exception {
        // Define the opponents based on the index
        int[] opponentIndices = getOpponentIndicesForIndex(index);

        // Schedule games against the determined opponents
        for (int opponentIndex : opponentIndices) {
            School opponent = div2.get(opponentIndex);
            int week = scheduleService.findConfGameWeek(school, opponent);
            boolean isHomeGame = opponentIndex % 2 == 0; // Alternate home and away games
            if (index % 2 == 0) {
                isHomeGame = !isHomeGame;
            }
            addYearlySeriesHelper(isHomeGame ? opponent : school, isHomeGame ? school : opponent, week, true);
        }
    }

    private int[] getOpponentIndicesForIndex(int index) {
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

    /**
     * @param div1
     * @param div2
     * @param school
     * @throws Exception
     */
    private void schedule8GamesXDivRivals(List<School> div1, List<School> div2, School school) throws Exception {
        School opponent = school.getxDivRival();
        int week = scheduleService.findConfGameWeek(school, opponent);
        // should be home or away game?
        if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= div1.size() / 2) {
            addYearlySeriesHelper(school, opponent, week, true);
        } else {
            addYearlySeriesHelper(opponent, school, week, true);
        }

        for (int index = 0; index < div1.size(); index++) {
            schedule8GamesByIndex(div1.get(index), div2, index);
        }
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
