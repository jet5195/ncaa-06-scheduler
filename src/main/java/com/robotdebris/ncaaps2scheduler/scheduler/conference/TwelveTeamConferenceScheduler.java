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
	public void generateConferenceSchedule(Conference conference, GameRepository gameRepository) {
		if (schoolService == null) {
			System.out.println("school service is null");
		}
		if (scheduleService == null) {
			System.out.println("schedule service is null");
		}
		try {
			scheduleConferenceGamesDivisions12(conference, gameRepository);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void scheduleConferenceGamesDivisions12(Conference conf, GameRepository gameRepository) throws Exception {
		try {
			// int index = 0;
			// move school order by year

			List<School> div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
			List<School> div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

			// schedule inner division games
			scheduleRoundRobinConfGames(div1, conf.getConfGamesStartWeek());
			scheduleRoundRobinConfGames(div2, conf.getConfGamesStartWeek());

			// order by cross div rivals
			boolean xDivRivals = div1.get(0).getXDivRival() != null;
			int numOfConfGames = conf.getNumOfConfGames();
			int yearOffset = gameRepository.getYear() - 2005;

			// 8 conf games no rivals
			if (numOfConfGames == 8 && !xDivRivals) {
				this.schedule12Teams8GamesNoXDivRivals(conf);
			} else {

				if (xDivRivals) {
					div2 = orderDivByXDivRivals(div1);
				}

				int rotationAmount = yearOffset % 6;
				for (int i = 0; i < rotationAmount; i++) {
					School s1 = div1.remove(div1.size() - 1);
					div1.add(0, s1);
				}
				for (School school : div1) {
//                int numOfConfGames = 9;

					if (numOfConfGames == 8 && xDivRivals) {
						if (school.getXDivRival() != null) {
							School opponent = school.getXDivRival();
							int week = scheduleService.findConfGameWeek(school, opponent);
							// should be home or away game?
							if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= div1.size() / 2) {
								addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);
							} else {
								addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), true);
							}

						}
						for (int index = 0; index < div1.size(); index++) {
							schedule8GamesByIndex(div1.get(index), div2, index, gameRepository);
						}
					}

					// 0 1 2 3
					if (numOfConfGames == 9) {
						for (int index = 0; index < div1.size(); index++) {
							schedule9GamesByIndex(div1.get(index), div2, index, gameRepository);
						}
					} // end of 9 game loop
						// index++;
				} // end of for team loop
			}

		} catch (IndexOutOfBoundsException e) {
			throw e;
		}
//            }
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

	private void schedule9GamesByIndex(School school, List<School> div2, int index, GameRepository gameRepository)
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
			addYearlySeriesHelper(isHomeGame ? opponent : school, isHomeGame ? school : opponent, week, 5,
					gameRepository.getYear(), true);
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

	private void schedule8GamesByIndex(School school, List<School> div2, int index, GameRepository gameRepository)
			throws Exception {
		// Schedule the first opponent
		School opponent = div2.get((index + 1) % div2.size());
		int week = scheduleService.findConfGameWeek(school, opponent);
		addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), true);

		// Find the next opponent, ensuring they're not the xDivRival
		int opponent2Id = (index + 2) % div2.size();
		if (div2.get(opponent2Id).equals(school.getXDivRival())) {
			opponent2Id = (opponent2Id + 1) % div2.size();
		}
		opponent = div2.get(opponent2Id);
		week = scheduleService.findConfGameWeek(school, opponent);
		addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);
	}

	private void schedule12Teams8GamesNoXDivRivals(Conference conf) throws Exception {
		List<School> div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
		List<School> div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

		// order by cross div rivals
		boolean xDivRivals = div1.get(0).getXDivRival() != null;
		int numOfConfGames = conf.getNumOfConfGames();
		int yearOffset = gameRepository.getYear() - 2005;
		int i = 0;
		// if scheduleAbc == true, schedule ABC, else, XYZ
		boolean scheduleAbc = yearOffset % 4 < 2;
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
						addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);
					} else if (scheduleService.getNumOfAwayConferenceGamesForSchool(school) >= numOfConfGames / 2) {
						addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), true);
					} else {
						addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), false);
					}
				}
			} else {
				for (int j = div1.size() / 2; j < div1.size(); j++) {
					School opponent = div2.get(j);
					int week = scheduleService.findConfGameWeek(school, opponent);

					if (scheduleService.getNumOfHomeConferenceGamesForSchool(school) >= numOfConfGames / 2) {
						addYearlySeriesHelper(school, opponent, week, 5, gameRepository.getYear(), true);
					} else if (scheduleService.getNumOfAwayConferenceGamesForSchool(school) >= numOfConfGames / 2) {
						addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), true);
					} else {
						addYearlySeriesHelper(opponent, school, week, 5, gameRepository.getYear(), false);
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

}
