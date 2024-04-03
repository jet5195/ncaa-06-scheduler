package com.robotdebris.ncaaps2scheduler.scheduler.conference;

import java.util.List;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;

public class TwelveTeamConferenceScheduler implements ConferenceScheduler {

	@Override
	public void generateConferenceSchedule(Conference conference) {
		// TODO Auto-generated method stub

	}

	public void scheduleConferenceGamesDivisions12(Conference conf) throws Exception {
		try {
			int index = 0;
			// move school order by year

			List<School> div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
			List<School> div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

			// schedule inner division games
			scheduleRoundRobinConfGames(div1, conf.getConfGamesStartWeek());
			scheduleRoundRobinConfGames(div2, conf.getConfGamesStartWeek());

			// order by cross div rivals
			boolean xDivRivals = div1.get(0).getXDivRival() != null;
			int numOfConfGames = conf.getNumOfConfGames();
			int yearMinus2005 = seasonSchedule.getYear() - 2005;

			// 8 conf games no rivals
			if (numOfConfGames == 8 && !xDivRivals) {
				this.schedule12Teams8GamesNoXDivRivals(conf);
			} else {

				if (xDivRivals) {
					div2 = orderDivByXDivRivals(div1);
				}

				if (yearMinus2005 % 6 == 0) {
					// second attempt at rotating logic... test this in 9 game conf schedules!!
				} else if (yearMinus2005 % 6 == 1) {
					int i = 0;
					while (i < 1) {
						School s1 = div1.remove(div1.size() - 1);
						div1.add(0, s1);
						i++;
					}
				} else if (yearMinus2005 % 6 == 2) {
					int i = 0;
					while (i < 2) {
						School s1 = div1.remove(div1.size() - 1);
						div1.add(0, s1);
						i++;
					}
				} else if (yearMinus2005 % 6 == 3) {
					int i = 0;
					while (i < 3) {
						School s1 = div1.remove(div1.size() - 1);
						div1.add(0, s1);
						i++;
					}

				} else if (yearMinus2005 % 6 == 4) {
					int i = 0;
					while (i < 4) {
						School s1 = div1.remove(div1.size() - 1);
						div1.add(0, s1);
						i++;
					}
				} else if (yearMinus2005 % 6 == 5) {
					int i = 0;
					while (i < 5) {
						School s1 = div1.remove(div1.size() - 1);
						div1.add(0, s1);
						i++;
					}
				}
				for (School school : div1) {
//                int numOfConfGames = 9;

					if (numOfConfGames == 8 && xDivRivals) {
						if (school.getXDivRival() != null) {
							School opponent = school.getXDivRival();
							int week = findConfGameWeek(school, opponent);
							// should be home or away game?
							if (school.getNumOfHomeConferenceGames() >= div1.size() / 2) {
								addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
							} else {
								addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
							}

						}
						if (index == 0) {
							School opponent = div2.get(1);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 2;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						} else if (index == 1) {
							School opponent = div2.get(2);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 3;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						} else if (index == 2) {
							School opponent = div2.get(3);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 4;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						} else if (index == 3) {
							School opponent = div2.get(4);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 5;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						} else if (index == 4) {
							School opponent = div2.get(5);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 0;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						} else if (index == 5) {
							School opponent = div2.get(0);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							int opponent2Id = 1;
							if (div2.get(opponent2Id) == school.getXDivRival()) {
								opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
							}
							opponent = div2.get(opponent2Id);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						}
					}

					// 0 1 2 3
					if (numOfConfGames == 9) {
						if (index == 0) {
							School opponent = div2.get(0);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(1);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(2);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(3);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							// 0 1 4 5
						} else if (index == 1) {
							School opponent = div2.get(0);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(1);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(4);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(5);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							// 2 3 4 5
						} else if (index == 2) {
							School opponent = div2.get(2);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(3);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(4);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(5);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							// 2 3 0 1
						} else if (index == 3) {
							School opponent = div2.get(2);
							int week = randomizeWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(3);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(0);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(1);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
						}

						// 4 5 0 1
						else if (index == 4) {
							School opponent = div2.get(4);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(5);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(0);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(1);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
						}

						// 4 5 2 3
						else if (index == 5) {
							School opponent = div2.get(4);
							int week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(5);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(2);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

							opponent = div2.get(3);
							week = findConfGameWeek(school, opponent);
							addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
						}
					} // end of 9 game loop
					index++;
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

	private void schedule12Teams8GamesNoXDivRivals(Conference conf) throws Exception {
		List<School> div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
		List<School> div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

		// order by cross div rivals
		boolean xDivRivals = div1.get(0).getXDivRival() != null;
		int numOfConfGames = conf.getNumOfConfGames();
		int yearMinus2005 = seasonSchedule.getYear() - 2005;
		int i = 0;
		// if scheduleAbc == true, schedule ABC, else, XYZ
		boolean scheduleAbc = yearMinus2005 % 4 < 2;
		for (School school : div1) {
			// on the 3rd team (halfway), swap scheduleAbc, so half the teams play xyz
			if (i == 3) {
				scheduleAbc = !scheduleAbc;
			}

			// if year is 0, 1, and school is 0, 1, 2... schedule abc, else xyz
			if (scheduleAbc) {
				for (int j = 0; j < div1.size() / 2; j++) {
					School opponent = div2.get(j);
					int week = findConfGameWeek(school, opponent);

					if (school.getNumOfHomeConferenceGames() >= numOfConfGames / 2) {
						addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
					} else if (school.getNumOfAwayConferenceGames() >= numOfConfGames / 2) {
						addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
					} else {
						addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), false);
					}
				}
			} else {
				for (int j = div1.size() / 2; j < div1.size(); j++) {
					School opponent = div2.get(j);
					int week = findConfGameWeek(school, opponent);

					if (school.getNumOfHomeConferenceGames() >= numOfConfGames / 2) {
						addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
					} else if (school.getNumOfAwayConferenceGames() >= numOfConfGames / 2) {
						addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
					} else {
						addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), false);
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
