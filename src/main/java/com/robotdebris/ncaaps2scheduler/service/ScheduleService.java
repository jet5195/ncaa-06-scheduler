package com.robotdebris.ncaaps2scheduler.service;

import static com.robotdebris.ncaaps2scheduler.SchedulerUtils.findEmptyWeeksHelper;
import static com.robotdebris.ncaaps2scheduler.SchedulerUtils.findEmptyWeeksInConferenceHelper;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.NoWeeksAvailableException;
import com.robotdebris.ncaaps2scheduler.SchedulerUtils;
import com.robotdebris.ncaaps2scheduler.model.AddGameRequest;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.DayOfWeek;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameBuilder;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SuggestedGameResponse;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceScheduler;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;

@Service
public class ScheduleService {

	private final GameRepository gameRepository;
	private final SchoolRepository schoolRepository;
	private final Logger LOGGER = Logger.getLogger(ScheduleService.class);
	@Autowired
	SchoolService schoolService;
	@Autowired
	ConferenceService conferenceService;
	@Autowired
	ExcelReader excelReader;
	@Autowired
	ConferenceSchedulerFactory conferenceSchedulerFactory;

	public ScheduleService(GameRepository gameRepository, SchoolRepository schoolRepository) {
		this.gameRepository = gameRepository;
		this.schoolRepository = schoolRepository;
	}

	private int addRivalryGameTwoSchools(School school, School rival, boolean aggressive, int rivalRank) {
		// School rival = school.getRivals().get(j);
		int count = 0;
		if (isEligibleNonConfMatchup(school, rival)) {
			if (aggressive && rivalRank < 2) {
				count += aggressiveAddRivalryGameHelper(school, rival);
			}
			// if they don't play and aren't in the same conference
			// go through all the rivals for a team
			else if (getScheduleBySchool(rival).size() < 12 && getScheduleBySchool(school).size() < 12) {
				// and stop if the seasonSchedule is full
				count += addRivalryGameHelper(school, rival, rivalRank);
			}
		}
		return count;
	}

	public void addGame(Game game) {
		if (canScheduleGame(game)) {
			gameRepository.saveGame(game);
			LOGGER.info("Added game " + game);
		} else {
			LOGGER.warn("Cannot schedule game " + game);
		}
	}

	public boolean canScheduleGame(Game game) {
		// Check if the teams are the same
		if (game.getHomeTeam().equals(game.getAwayTeam())) {
			return false;
		}
		// Check if the home team already has a game scheduled in the same week
		Optional<Game> existingHomeGame = gameRepository.findByTeamAndWeek(game.getHomeTeam(), game.getWeek());
		if (existingHomeGame.isPresent()) {
			return false;
		}

		// Check if the away team already has a game scheduled in the same week
		Optional<Game> existingAwayGame = gameRepository.findByTeamAndWeek(game.getAwayTeam(), game.getWeek());
		if (existingAwayGame.isPresent()) {
			return false;
		}

		Optional<Game> existingMatchup = gameRepository.findGameByTeams(game.getHomeTeam(), game.getAwayTeam());
		if (existingMatchup.isPresent()) {
			return false;
		}

		return true;
	}

	public ArrayList<Integer> findEmptyWeeks(School s1, School s2) {// returns list of empty weeks between 2 schools
		ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
		ArrayList<Integer> s2weeks = findEmptyWeeks(s2);

		boolean isInConference = s1.isInConference(s2);
		ArrayList<Integer> freeWeeks;
		if (isInConference) {
			freeWeeks = findEmptyWeeksInConferenceHelper(s1weeks, s2weeks, s1.getConference().getConfGamesStartWeek());
			if (freeWeeks.isEmpty()) {
				// TODO: do this some other way
				// freeWeeks = fixNoEmptyWeeks(s1, s2);
			}
		} else {
			freeWeeks = findEmptyWeeksHelper(s1weeks, s2weeks);
		}
		return freeWeeks;
	}

	public List<Game> getSeasonSchedule() {
		return gameRepository.findAll();
	}

	public void setSeasonSchedule(List<Game> seasonSchedule) {
		gameRepository.saveAll(seasonSchedule);
	}

	public int getYear() {
		return gameRepository.getYear();
	}

	public void setYear(int year) {
		gameRepository.setYear(year);
	}

	public void setScheduleFile(MultipartFile scheduleFile) throws IOException {
		File file = excelReader.convertMultipartFileToFile(scheduleFile);
		try {
			removeAllGames();
			setSeasonSchedule(excelReader.populateSeasonScheduleFromExcel(file, schoolService.getAllSchools()));
			// is this going to miss conference games since conferences aren't set yet?
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<School> getSchoolList() {
		return schoolService.getAllSchools();
	}

	public School searchSchoolByTgid(int tgid) {
		return schoolService.schoolSearch(tgid);
	}

	public List<School> findOpenOpponentsForWeek(int tgid, int week) {
		School input = schoolService.schoolSearch(tgid);
		if (input == null) {
			throw new IllegalArgumentException("School not found with ID: " + tgid);
		}

		return schoolService.getAllSchools().stream().filter(school -> isOpponentOpenForWeek(input, school, week))
				.collect(Collectors.toList());
	}

	private boolean isOpponentOpenForWeek(School input, School school, int week) {
		return !isOpponentForSchool(input, school) && getGameBySchoolAndWeek(school, week) == null;
	}

	public List<School> getOpenNonConferenceRivals(int tgid, int week) {
		School input = schoolService.schoolSearch(tgid);
		if (input == null) {
			throw new IllegalArgumentException("School not found with ID: " + tgid);
		}

		return input.getRivals().stream().filter(rival -> isEligibleNonConfMatchup(input, rival))
				.filter(school -> getGameBySchoolAndWeek(school, week) == null).collect(Collectors.toList());
	}

	public int removeAllOocNonRivalGames() {
		return removeAllNonConferenceGames(false);
	}

	public int removeAllOocGames() {
		return removeAllNonConferenceGames(true);
	}

	public void removeGame(int tgid, int week) {
		School input = schoolService.schoolSearch(tgid);
		Game game = getGameBySchoolAndWeek(input, week);
		removeGame(game);
	}

	private ArrayList<Integer> fixNoEmptyWeeksForConfGame(School s1, School s2) {

		// trying to schedule WVU vs PSU
		// WVU has weeks 3, 5, 7, 9 available
		// PSU has weeks 4, 6, 8, 10 available
		// WVU plays UL week 6
		// UL has week 7 available
		// change WVU vs UL to week 7
		// schedule WVU vs PSU
		ArrayList<Integer> s1EmptyWeeks = findEmptyWeeks(s1);
		ArrayList<Integer> s2EmptyWeeks = findEmptyWeeks(s2);
		int confGamesStartDate = s1.getConference().getConfGamesStartWeek();

		for (Game game : gameRepository.findGamesByTeam(s1)) {
			if (s2EmptyWeeks.contains(game.getWeek())) {
				School opponent = game.getHomeTeam().equals(s1) ? game.getAwayTeam() : game.getHomeTeam();
				ArrayList<Integer> opponentEmptyWeeks = findEmptyWeeks(opponent);
				ArrayList<Integer> jointEmptyWeeks = findEmptyWeeksInConferenceHelper(s1EmptyWeeks, opponentEmptyWeeks,
						confGamesStartDate);

				// move the game to another week
				if (!jointEmptyWeeks.isEmpty()) {
					System.out.println("Moving game: " + game.getAwayTeam() + " at " + game.getHomeTeam()
							+ ", because no available week was found for " + s1 + " vs " + s2);
					removeGame(s1.getTgid(), game.getWeek());
					Game newGame = new GameBuilder().setAwayTeam(game.getAwayTeam()).setHomeTeam(game.getHomeTeam())
							.setWeek(randomizeWeek(jointEmptyWeeks)).build();
					addGame(newGame);
					break;
				}
			}
		}
		s1EmptyWeeks = findEmptyWeeks(s1);
		s2EmptyWeeks = findEmptyWeeks(s2);
		return SchedulerUtils.findEmptyWeeksHelper(s1EmptyWeeks, s2EmptyWeeks);
	}

	public SuggestedGameResponse getSuggestedGame(int tgid) {
		School thisSchool = schoolService.schoolSearch(tgid);
		int homeGameCount = 0;
		boolean isHomeGame = false;
		for (Game game : getScheduleBySchool(thisSchool)) {
			if (game.getHomeTeam().getTgid() == tgid) {
				homeGameCount++;
			}
		}

		if (homeGameCount < 6) {
			isHomeGame = true;
		}

		for (School rival : thisSchool.getRivals()) {
			if (isEligibleNonConfMatchup(thisSchool, rival)) {
				// should isPossibleOpponent check this instead?
				if (getScheduleBySchool(rival).size() < 12) {
					ArrayList<Integer> emptyWeeks = findEmptyWeeks(thisSchool, rival);
					if (emptyWeeks.contains(13)) {
						return new SuggestedGameResponse(13, rival, isHomeGame);
						// week 14 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is
						// referenced by 13
					} else if (emptyWeeks.contains(12)) {
						return new SuggestedGameResponse(12, rival, isHomeGame);
						// week 12 is empty
					} else if (emptyWeeks.contains(11)) {
						return new SuggestedGameResponse(11, rival, isHomeGame);
						// week 14 is empty
					} else if (!emptyWeeks.isEmpty()) {
						return new SuggestedGameResponse(emptyWeeks.get(0), rival, isHomeGame);
						// add game at emptyWeeks.get(0);
					}
				}
			}
		}
		return null;
	}

	public void autoAddGames(boolean aggressive) {
		addRivalryGamesAll(schoolService.getAllSchools(), aggressive);
		removeExtraGames(schoolService.getAllSchools());
		fillOpenGames(schoolService.getAllSchools());
	}

	private void fillOpenGames(List<School> schoolList) {
		List<School> tooFewGames = findTooFewGames();
		addRivalryGamesAll(tooFewGames, false);
		// recalculate tooFewGames?
		tooFewGames = findTooFewGames();
		addRandomGames(schoolList, tooFewGames);
	}

	private void removeExtraGames(List<School> schoolList) {
		List<School> tooManyGames = findTooManyGames();
		for (int i = 0; i < tooManyGames.size(); i++) {
			School school = tooManyGames.get(i);
			while (getScheduleBySchool(school).size() > 12) {
				Game removeMe = findNonConferenceNonRivalryGameForSchool(school);
				if (removeMe != null) {
					removeGame(removeMe);
				} else {
					// remove extra rivalry games
					for (int j = school.getRivals().size() - 1; getScheduleBySchool(school).size() > 12; j--) {
						School rival = school.getRivals().get(j);
						if (isOpponentForSchool(school, rival)) {
							removeMe = findFirstGameBetweenSchools(school, rival);
							if (!removeMe.isConferenceGame()) {
								removeGame(removeMe);
							}
						}
					}
				}
			}
		}
	}

	// finds the FIRST game between 2 schools.
	private Game findFirstGameBetweenSchools(School s1, School s2) {
		return getScheduleBySchool(s1).stream().filter(
				game -> game.getHomeTeam().getTgid() == s2.getTgid() || game.getAwayTeam().getTgid() == s2.getTgid())
				.findFirst().orElse(null);
	}

	// find game by week number and game number
	private Game findGameByWeekAndGameNumber(int week, int gameNumber) {
		return getSeasonSchedule().stream().filter(game -> game.getWeek() == week && game.getGameNumber() == gameNumber)
				.findFirst().orElse(null);
	}

	public int addRivalryGamesAll(List<School> allSchools, boolean aggressive) {
		int count = 0;
		for (int j = 0; j <= 8; j++) {
			for (int i = 0; i < allSchools.size(); i++) {
				// go through all the schools
				School s1 = allSchools.get(i);
				if (s1.getNcaaDivision().equals(NCAADivision.FBS) && j < s1.getRivals().size()) {
					// new chance algorithm so you don't ALWAYS play your 5th rival.
					// 1st rival: 100% chance
					// 2nd rival: 70% chance
					// 3rd rival: 50% chance
					// 4th rival: 20% chance
					// >4th rival: 10% chance
					int chance = new Random().nextInt(10);
					boolean scheduleGame = false;
					if (j == 0) {
						scheduleGame = true;
					} else if (j == 1 && chance < 7) {
						scheduleGame = true;
					} else if (j == 2 && chance < 5) {
						scheduleGame = true;
					} else if (j == 3 && chance < 2) {
						scheduleGame = true;
					} else if (j > 3 && chance < 1) {
						scheduleGame = true;
					}

					if (scheduleGame) {
						School rival = s1.getRivals().get(j);
						count += addRivalryGameTwoSchools(s1, rival, aggressive, j);
					}
				}
			}
		}
		return count;
	}

	private int addRivalryGameHelper(School s1, School rival, int rivalRank) {
		int year = gameRepository.getYear();
		ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
		// TODO: move games if week 13 is taken (ie so FSU UF can be week 13 yearly
		if (rivalRank < 2) {
			if (emptyWeeks.contains(13)) {
				Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, year).setWeek(13)
						.setDay(DayOfWeek.SATURDAY).build();
				addGame(game);
				return 1;
				// week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is
				// referenced by 12
			} else if (emptyWeeks.contains(12)) {
				Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, year).setWeek(12)
						.setDay(DayOfWeek.SATURDAY).build();
				addGame(game);
				return 1;
				// week 12 is empty
			} else if (emptyWeeks.contains(11)) {
				Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, year).setWeek(11)
						.setDay(DayOfWeek.SATURDAY).build();
				addGame(game);
				return 1;
				// week 14 is empty
			} else if (!emptyWeeks.isEmpty()) {
				Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, year).setWeek(emptyWeeks.get(0))
						.setDay(DayOfWeek.SATURDAY).build();
				addGame(game);
				return 1;
				// add game at emptyWeeks.get(0);
			}
		} else if (!emptyWeeks.isEmpty()) {
			Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, year).setWeek(emptyWeeks.get(0))
					.setDay(DayOfWeek.SATURDAY).build();
			addGame(game);
			return 1;
		}
		return 0;
	}

	private int aggressiveAddRivalryGameHelper(School s1, School rival) {
		ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
		ArrayList<Integer> rweeks = findEmptyWeeks(rival);
		ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
		if (emptyWeeks.contains(12)) {
			Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, getYear()).setWeek(12)
					.setDay(DayOfWeek.SATURDAY).build();
			addGame(game);
			return 1;
			// week 13 is empty
		} else if (emptyWeeks.contains(11)) {
			Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, getYear()).setWeek(11)
					.setDay(DayOfWeek.SATURDAY).build();
			addGame(game);
			return 1;
			// week 12 is empty
		}
		if (emptyWeeks.contains(13)) {
			Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, getYear()).setWeek(13)
					.setDay(DayOfWeek.SATURDAY).build();
			addGame(game);
			return 1;
			// week 14 is empty
		}
		if (s1weeks.contains(12)) {
			// if the first team has an opening in week 13...
			Game game = getGameBySchoolAndWeek(rival, 12);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;// or should this be a 0?
			}
		}
		if (s1weeks.contains(11)) {
			// if the first team has an opening in week 12...
			Game game = getGameBySchoolAndWeek(rival, 11);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;
			}
		}
		if (s1weeks.contains(13)) {
			// if the first team has an opening in week 14...
			Game game = getGameBySchoolAndWeek(rival, 13);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;
			}
		}
		if (rweeks.contains(12)) {
			// if the first team has an opening in week 13...
			Game game = getGameBySchoolAndWeek(s1, 12);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;
			}
		}
		if (rweeks.contains(11)) {
			// if the first team has an opening in week 12...
			Game game = getGameBySchoolAndWeek(s1, 11);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;
			}
		}
		if (rweeks.contains(13)) {
			// if the first team has an opening in week 14...
			Game game = getGameBySchoolAndWeek(s1, 13);
			// set game to variable
			if (game.isRemovableGame()) {
				// if the game that is blocking a game being added isn't required..
				replaceGame(game, s1, rival);
				return 1;
			}
		}
		if (!s1weeks.contains(12) && !rweeks.contains(12)) {
			Game s1game = getGameBySchoolAndWeek(s1, 12);
			Game rgame = getGameBySchoolAndWeek(rival, 12);
			if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
				removeGame(s1game);
				replaceGame(rgame, s1, rival);
				return 1; // should this still return a 1? I guess so. Just counting added games
				// remove both games and replace with this one...
			}
		}
		if (!s1weeks.contains(11) && !rweeks.contains(11)) {
			Game s1game = getGameBySchoolAndWeek(s1, 11);
			Game rgame = getGameBySchoolAndWeek(rival, 11);
			if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
				removeGame(s1game);
				replaceGame(rgame, s1, rival);
				return 1;
				// remove both games and replace with this one...
			}
		}
		if (!s1weeks.contains(13) && !rweeks.contains(13)) {
			Game s1game = getGameBySchoolAndWeek(s1, 13);
			Game rgame = getGameBySchoolAndWeek(rival, 13);
			if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
				removeGame(s1game);
				replaceGame(rgame, s1, rival);
				return 1;
				// remove both games and replace with this one...
			}
		}
		if (!emptyWeeks.isEmpty()) {
			Game game = new GameBuilder().setTeamsWithYearlyRotation(s1, rival, getYear()).setWeek(emptyWeeks.get(0))
					.setDay(DayOfWeek.SATURDAY).build();
			addGame(game);
			return 1;
			// add game at emptyWeeks.get(0);
		}
		return 0;
	}

	public void addRandomGames(List<School> allSchools, List<School> needGames) {
		for (int i = 0; i < needGames.size(); i++) {
			School s1 = needGames.get(i);
			List<School> myOptions = findTooFewGames();
			myOptions.remove(s1);

			boolean exit = false;
			while (!exit && !myOptions.isEmpty()) {
				int max = myOptions.size() - 1;
				int min = 0;
				int range = max - min + 1;
				int randomNum = (int) (Math.random() * range) + min;
				School randomSchool = myOptions.get(randomNum);
				if (isEligibleNonConfMatchup(s1, randomSchool) && getScheduleBySchool(randomSchool).size() < 12) {
					ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, randomSchool);
					if (!emptyWeeks.isEmpty()) {
						// verify Alabama won't play Michigan to end the year. Instead they'll play LA
						// Monroe
						if (emptyWeeks.get(0) < 11
								|| (s1.getConference().isPowerConf() ^ randomSchool.getConference().isPowerConf())) {
							Game game = new GameBuilder().setTeamsWithRandomHomeIntelligently(s1, randomSchool)
									.setWeek(emptyWeeks.get(0)).build();
							addGame(game);
						}
					}
					// remove randomSchool from myOptions regardless of whether or not it was added
					myOptions.remove(randomSchool);
					if (getScheduleBySchool(randomSchool).size() > 11) {
						if (randomNum < i) {
							i--;
						}
						needGames.remove(randomSchool);
					}
					// if you can't add any more FBS opponents, exit to next school.
					// fcs opponents will be added afterwards
					if (myOptions.isEmpty()) {
						exit = true;
					}
					// if enough games were successfully added, remove the school from the
					// needGames list
					if (getScheduleBySchool(s1).size() > 11) {
						needGames.remove(s1);
						i--;
						exit = true;
					}
				} else {// remove random school if it has enough games
					if (randomNum < i) {
						i--;
					}
					needGames.remove(randomSchool);
					myOptions.remove(randomSchool);
				}
			}
		}

		if (!needGames.isEmpty()) {
			// add games vs fcs schools

			// create fcs schools array
			List<School> fcsSchoolList = new ArrayList<School>();
			for (int i = 0; i < allSchools.size(); i++) {
				// checking if it doesn't = FBS so schools added to the SchoolData
				// spreadsheet but not added to the conference spreadsheets are
				// still included. On 2nd thought I might already be setting those
				// to FCS... But I'll need to check.
				if (!allSchools.get(i).getNcaaDivision().equals(NCAADivision.FBS)) {
					fcsSchoolList.add(allSchools.get(i));
				}
			}

			// add fcs games to all schools that still need games
			for (int i = 0; i < needGames.size(); i++) {
				School s1 = needGames.get(i);
				// for (int j = 0; j < allSchools.size() && s1.getSchedule().size() < 12; j++) {
				// if (!allSchools.get(j).getNcaaDivision().equals("FBS")) {

				while (getScheduleBySchool(s1).size() < 12) {

					// get random school
					int min = 0;
					int max = fcsSchoolList.size() - 1;

					Random random = new Random();

					int randomValue = random.nextInt(max + min) + min;

					School fcs = fcsSchoolList.get(randomValue);
					if (!isOpponentForSchool(s1, fcs)) {
						ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, fcs);
						if (!emptyWeeks.isEmpty()) {
							Game game = new GameBuilder().setHomeTeam(s1).setAwayTeam(fcs).setWeek(emptyWeeks.get(0))
									.setDay(DayOfWeek.SATURDAY).build();
							addGame(game);
						}
					}
				}
			}
		}
		for (int i = needGames.size() - 1; i >= 0; i--) {
			if (getScheduleBySchool(needGames.get(i)).size() == 12) {
				needGames.remove(i);
			}
		}
	}

	public void fixSchedule() {
		removeExtraGames(schoolService.getAllSchools());
		// count += fillOpenGames(seasonSchedule, schoolList);
	}

	public void setAlignmentFile(MultipartFile alignmentFile) throws IOException {
		File file = excelReader.convertMultipartFileToFile(alignmentFile);
		try {
			List<Conference> conferenceList = excelReader.populateConferencesFromExcel(file);
			conferenceService.setConferenceList(conferenceList);
			excelReader.setAlignmentData(file);
			conferenceService.setConferencesSchoolList(schoolService.getAllSchools());
			// TODO: probably need to reimplement this as well.
			// Collections.sort(schoolService.getAllSchools());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void downloadSchedule(Writer writer) {
		try {
			CsvExportService csvExportService = new CsvExportService();
			List<List> list = scheduleToList(true);
			csvExportService.writeSchedule(writer, list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return null;
	}
//
//    public void setAllYearlyGames() {
//        /*
//         *
//         * week 14 army navy (only game of the week)
//         *
//         * week 13 Ole Miss Miss St UNC NC St? USF UCF EMU CMU WSU Wash UGA GT (Sat) OU
//         * OKST Bama Auburn PSU MSU OSU Oregon ND Stanford? UK UL LSU A&M/Ark? Pitt Cuse
//         * IU Purdue FSU Florida Clem USC Vandy UT ILL NW Wisc Minn Mich OSU ULM ULL Cal
//         * UCLA Zona ASU WKU Marshall? Col Utah? VT UVA Mizzou Kansas OU Nebraska UT
//         * Vandy Duke Wake Cuse BC
//         *
//         *
//         * week 10 LSU Bama ? UK UT
//         *
//         * week 9 Mich MSU? FSU Clem? UGA UF
//         *
//         * Week 8 Bama UT LSU Ole Miss? probs not
//         *
//         * Week 7 SDSU SJSU? Pitt VT?
//         *
//         * Week 6 OU Texas UGA Auburn? probs not
//         *
//         *
//         */
//        // week 6
//        addYearlySeriesHelper("Oklahoma", "Texas", 6, 5, getYear(), false);
//
//        // week 8
//        addYearlySeriesHelper("Alabama", "Tennessee", 8, 5, getYear(), false);
//
//        // week 9
//        addYearlySeriesHelper("Georgia", "Florida", 9, 5, getYear(), false);
//
//        // week 12
//        addYearlySeriesHelper("Tennessee", "Kentucky", 12, 5, getYear(), false);
//
//        // week 13 (rivalry week)
//        addYearlySeriesHelper("Virginia", "Virginia Tech", 13, 5, getYear(), false);
//        addYearlySeriesHelper("North Carolina", "North Carolina State", 13, 5, getYear(), false);
//        for (School school : schoolService.getAllSchools()) {
//            if (school.getNcaaDivision().isFBS()) {
//                boolean endLoop = false;
//                int i = 0;
//                while (!endLoop) {
//                    if (school.getRivals().size() > i) {
//                        School rival = school.getRivals().get(i);
//                        endLoop = addYearlySeriesHelper(school, rival, 13, 5, getYear(), false);
//                        i++;
//                    } else {
//                        endLoop = true;
//                    }
//                }
//            }
//        }
//
//        // week 14
//        addYearlySeriesHelper("Navy", "Army", 14, 5, getYear(), false);
//    }

	public int autoAddConferenceGames(String name) throws Exception {
		Conference conf = conferenceService.conferenceSearch(name);
		// setAllYearlyGames();
		ConferenceScheduler scheduler = conferenceSchedulerFactory.getScheduler(conf);
		scheduler.generateConferenceSchedule(conf, gameRepository);
		return 0;
	}

	public List<Game> getBowlGames() {
		List<Game> bowlGames = new ArrayList<>();
		for (int i = 16; i <= 23; i++) {
			List<Game> weeklySchedule = this.getScheduleByWeek(i);
			for (Game game : weeklySchedule) {
				bowlGames.add(game);
			}
		}
		return bowlGames;
	}

//    public Game getBowlGame(int week, int gameId) {
//        ArrayList<Game> weeklyGames = seasonSchedule.getBowlScheduleByWeek(week);
//        for (Game game : weeklyGames) {
//            if (game.getGameNumber() == gameId) {
//                return game;
//            }
//        }
//        return null;
//    }

//	public int removeConferenceGames(String name) {
//		Conference conf = conferenceService.conferenceSearch(name);
//		return removeAllConferenceGames(conf);
//	}

	public Game getGame(int week, int gameId) {
		ArrayList<Game> weeklyGames = getScheduleByWeek(week);
		for (Game game : weeklyGames) {
			if (game.getGameNumber() == gameId) {
				return game;
			}
		}
		return null;
	}

	public void removeAllConferenceGames() {
		for (Conference conf : conferenceService.getConferenceList()) {
			this.removeConfGamesByConference(conf);
		}
	}

	public void addAllConferenceGames() throws Exception {
		for (Conference conf : conferenceService.getConferenceList()) {
			// if conference is FBS...
			List<School> schools = conf.getSchools();
			if (schools != null) {
				if (Objects.equals(schools.get(0).getNcaaDivision(), NCAADivision.FBS)) {
					this.autoAddConferenceGames(conf.getName());
				}
			}
		}
	}

	public void saveGame(AddGameRequest addGameRequest, int oldWeek, int oldGameNumber) {
		School home = schoolService.schoolSearch(addGameRequest.getHomeId());
		School away = schoolService.schoolSearch(addGameRequest.getAwayId());

		Game oldGame = findGameByWeekAndGameNumber(oldWeek, oldGameNumber);
		removeGame(oldGame);

		oldGame.setGameResult(addGameRequest.getGameResult());
		oldGame.setAwayTeam(away);
		oldGame.setHomeTeam(home);
		oldGame.setWeek(addGameRequest.getWeek());
		oldGame.setDay(addGameRequest.getDay());
		oldGame.setTime(addGameRequest.getTime());

		addGame(oldGame);
		// calculate gameNumber for every game
		this.recalculateGameNumbers();
	}

	public void recalculateGameNumbers() {
		// stop calculating after week 15, because the rest are bowl games.
		// for bowl games changing the gameNumber changes what bowl it is!!
		for (int weekNum = 0; weekNum < 16; weekNum++) {
			ArrayList<Game> weeklySchedule = this.getScheduleByWeek(weekNum);
			for (int gameNum = 0; gameNum < weeklySchedule.size(); gameNum++) {
				weeklySchedule.get(gameNum).setGameNumber(gameNum);
			}
		}
	}

	// TODO: these are the methods removed from SeasonSchedule
	/**
	 *
	 * @return the bowl schedule
	 */
//   public SeasonSchedule getBowlSchedule() {
//       return bowlSchedule;
//   }

	/**
	 * Sets the bowl schedule. Currently this is only called once when the excel
	 * file is being read
	 *
	 * @param bowlSchedule
	 */
//   public void setBowlSchedule(SeasonSchedule bowlSchedule) {
//       this.bowlSchedule = bowlSchedule;
//   }
	public void swapSchedule(int tgid1, int tgid2) {
		School s1 = schoolService.schoolSearch(tgid1);
		School s2 = schoolService.schoolSearch(tgid2);

		// remove original game from season schedule, and add to new lists to iterate
		// through
		ArrayList<Game> s1Schedule = new ArrayList<>();
		for (Game game : getScheduleBySchool(s1)) {
			s1Schedule.add(game);
		}

		ArrayList<Game> s2Schedule = new ArrayList<>();
		for (Game game : getScheduleBySchool(s2)) {
			s2Schedule.add(game);
		}

		for (Game game : s1Schedule) {
			removeGame(game);
			if (game.getHomeTeam().equals(s1)) {
				game.setHomeTeam(s2);
			} else {
				game.setAwayTeam(s2);
			}
			addGame(game);
		}

		for (Game game : s2Schedule) {
			removeGame(game);
			if (game.getHomeTeam().equals(s2)) {
				game.setHomeTeam(s1);
			} else {
				game.setAwayTeam(s1);
			}
			addGame(game);
		}
		recalculateGameNumbers();
	}

	/**
	 * Removes a game from the schedule and updates all affected game numbers
	 *
	 * @param theGame the game to be removed
	 */
	public void removeGame(Game theGame) {
		// code to change the game numbers for all games afterwards in this week
		School s1 = theGame.getHomeTeam();
		School s2 = theGame.getAwayTeam();
		int gameNumber = theGame.getGameNumber();
		int weekNumber = theGame.getWeek();
		getSeasonSchedule().remove(theGame);
		updateGameNumbers(gameNumber, weekNumber);
		LOGGER.info("Removing game " + s2 + " at " + s1);
		System.out.println("Removing game " + s2 + " at " + s1);
	}

	/**
	 * Replaces theGame with a new game between school s1 & s2
	 *
	 * @param theGame the game to be replaced
	 * @param s1      school 1 of the new game
	 * @param s2      school 2 of the new game
	 */
	public void replaceGame(Game theGame, School s1, School s2) {
		int gameNumber = theGame.getGameNumber();
		int weekNumber = theGame.getWeek();
		DayOfWeek dayNumber = theGame.getDay();
		getSeasonSchedule().remove(theGame);
		LOGGER.info("Removing and replacing " + theGame.getAwayTeam() + " at " + theGame.getHomeTeam());
		Game game = new GameBuilder().setTeamsWithRandomHomeIntelligently(s1, s2).setWeek(weekNumber).setDay(dayNumber)
				.setGameNumber(gameNumber).build();
		addGame(game);
	}

	/**
	 * Updates the game numbers after removing a game from a week's schedule
	 *
	 * @param gameNumber the game number that is being removed
	 * @param weekNumber the week of the game that is being removed
	 */
	private void updateGameNumbers(int gameNumber, int weekNumber) {
		if (weekNumber < 16)
			for (Game game : getSeasonSchedule()) {
				if (game.getWeek() == weekNumber && game.getGameNumber() > gameNumber) {
					game.setGameNumber(game.getGameNumber() - 1);
				}
			}
	}

	/**
	 * Removes all FCS games from the schedule,
	 *
	 * @return count of removed games
	 */
	public int removeAllFcsGames() {
		int count = 0;
		for (int i = 0; i < getSeasonSchedule().size(); i++) {
			Game game = getSeasonSchedule().get(i);
			if (game.getHomeTeam().getNcaaDivision() == null || game.getAwayTeam().getNcaaDivision() == null) {
				i = i;
			}
			if (!game.getHomeTeam().getNcaaDivision().isFBS() || !game.getAwayTeam().getNcaaDivision().isFBS()) {
				this.removeGame(game);
				count++;
				i--;
			}
		}
		return count;
	}

	/**
	 * Removes all conference games from the schedule for a given conference,
	 *
	 * @param conf to remove games from
	 * @return count of removed games
	 */
	public int removeConfGamesByConference(Conference conf) {
		List<Game> confGames = gameRepository.findConfGamesByConference(conf);
		gameRepository.removeGames(confGames);
		return 0;
	}

	/**
	 * @param removeRivals if true, all Non-Conference games will be removed. If
	 *                     false, then only non-conference games that aren't rivalry
	 *                     games will be removed
	 * @return count of removed games Removes all non-conference games from schedule
	 */
	public int removeAllNonConferenceGames(boolean removeRivals) {
		int count = 0;
		for (int i = 0; i < getSeasonSchedule().size(); i++) {
			Game game = getSeasonSchedule().get(i);
			// remove game no matter what if either team isn't in a conference.
			if (game.getHomeTeam().getConference() == null || game.getAwayTeam().getConference() == null) {
				this.removeGame(game);
				count++;
				i--;
			} else if (!game.getHomeTeam().getConference().getName()
					.equalsIgnoreCase(game.getAwayTeam().getConference().getName())) {
				if (removeRivals) {
					this.removeGame(game);
					count++;
					i--;
				} else if (!game.isRivalryGame()) {
					this.removeGame(game);
					count++;
					i--;
				}
			}
		}
		return count;
	}

	public void removeAllGames() {
		gameRepository.removeAll();
	}

	/**
	 * @return ArrayList of Strings of the SeasonSchedule
	 */
	public List<List> scheduleToList(boolean header) {
		List<List> list = new ArrayList<>();
		if (header) {
			List<String> firstLine = new ArrayList<String>();
			firstLine.add("GSTA");
			firstLine.add("GASC");
			firstLine.add("GHSC");
			firstLine.add("GTOD");
			firstLine.add("GATG");
			firstLine.add("GHTG");
			firstLine.add("SGNM");
			firstLine.add("SEWN");
			firstLine.add("GDAT");
			firstLine.add("GFOT");
			firstLine.add("SEWT");
			firstLine.add("GFFU");
			firstLine.add("GFHU");
			firstLine.add("GMFX");
			list.add(firstLine);
		}
		for (Game game : getSeasonSchedule()) {
			list.add(game.gameToList());
		}
		return list;
	}

//   public ArrayList<Game> getBowlScheduleByWeek(int week) {
//       ArrayList<Game> weeklySchedule = new ArrayList<>();
//       for (Game game : this.getBowlSchedule()) {
//           if (game.getWeek() == week) {
//               weeklySchedule.add(game);
//           }
//       }
//       return weeklySchedule;
//   }

	public ArrayList<Game> getScheduleByWeek(int week) {
		ArrayList<Game> weeklySchedule = new ArrayList<>();
		for (Game game : getSeasonSchedule()) {
			if (game.getWeek() == week) {
				weeklySchedule.add(game);
			}
		}
		return weeklySchedule;
	}

	/**
	 * Returns a new game number for an added game, is always one higher than the
	 * currently highest game number for the given week
	 *
	 * @param week week of the game
	 * @return the new game number
	 */
	protected int findNewGameNumber(int week) {
		int gameNumber = 0;
		for (Game theGame : getSeasonSchedule()) {
			gameNumber = theGame.getWeek() == week && gameNumber < theGame.getGameNumber() ? theGame.getGameNumber()
					: gameNumber;
		}
		return ++gameNumber;
	}

	public List<Game> getScheduleBySchool(School school) {
		return gameRepository.findGamesByTeam(school);
	}

	public int getNumOfConferenceGamesForSchool(School school) {
		int confGames = 0;
		for (Game game : getScheduleBySchool(school)) {
			if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
				confGames++;
			}
		}
		return confGames;
	}

	public int getNumOfHomeConferenceGamesForSchool(School school) {
		int homeConfGames = 0;
		for (Game game : getScheduleBySchool(school)) {
			if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
				if (game.getHomeTeam() == school) {
					homeConfGames++;
				}
			}
		}
		return homeConfGames;
	}

	public int getNumOfAwayConferenceGamesForSchool(School school) {
		int awayConfGames = 0;
		for (Game game : getScheduleBySchool(school)) {
			if (game.getHomeTeam().isInConference(game.getAwayTeam())) {
				if (game.getAwayTeam() == school) {
					awayConfGames++;
				}
			}
		}
		return awayConfGames;
	}

	public int getNumOfDivisionalGamesForSchool(School school) {
		int divisionalGames = 0;
		for (Game game : getScheduleBySchool(school)) {
			if (game.getHomeTeam().isInConference(game.getAwayTeam())
					&& game.getHomeTeam().getDivision().equalsIgnoreCase(game.getAwayTeam().getDivision())) {
				divisionalGames++;
			}
		}
		return divisionalGames;
	}

	/**
	 * Checks to see if this school plays an opponent
	 *
	 * @param opponent the opponent
	 * @return true if these schools do play, false if else
	 */
	public boolean isOpponentForSchool(School school, School opponent) {
		return gameRepository.findGamesByTeam(school).stream().anyMatch(
				game -> Objects.equals(game.getHomeTeam(), opponent) || Objects.equals(game.getAwayTeam(), opponent));
	}

	/**
	 * Returns true if schools are not in the same conference and don't already play
	 * one another
	 *
	 * @param school the opponent
	 * @return true if schools are not in the same conference and don't already play
	 *         one another
	 */
	public boolean isEligibleNonConfMatchup(School school, School opponent) {
		return !school.isInConference(opponent) && !this.isOpponentForSchool(school, opponent);
	}

	/**
	 * Searches a school's schedule for a game that is not in conference or a
	 * rivalry game
	 *
	 * @return Game that is removable (not a rivalry and not a conference game)
	 */
	public Game findNonConferenceNonRivalryGameForSchool(School school) {
		for (int i = 0; i < getScheduleBySchool(school).size(); i++) {
			Game theGame = getScheduleBySchool(school).get(i);
			// 0 means non-con
			if (theGame.isRemovableGame()) {
				return theGame;
			}
		}
		return null;
	}

	// finds empty weeks for one school
	public ArrayList<Integer> findEmptyWeeks(School s) {
		ArrayList<Integer> freeWeeks = new ArrayList<>();
		ArrayList<Integer> usedWeeks = new ArrayList<>();
		for (int i = 0; i < getScheduleBySchool(s).size(); i++) {
			usedWeeks.add(getScheduleBySchool(s).get(i).getWeek());
		} // populates freeWeeks with 0-14, all the possible weeks for regular season
			// games
		for (int i = 0; i < 15; i++) {
			if (!usedWeeks.contains(i)) {
				freeWeeks.add(i);
			}
		}
		return freeWeeks;
	}

	public int randomizeWeek(School school, School opponent) {
		ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
		emptyWeeks.remove(Integer.valueOf(14));
		return randomizeWeek(emptyWeeks);
	}

	public int randomizeWeek(ArrayList<Integer> weeks) {
		int max = weeks.size() - 1;
		int min = 0;
		int range = max - min + 1;
		int randomNum = (int) (Math.random() * range) + min;
		return weeks.get(randomNum);
	}

	public int findConfGameWeek(School school, School opponent) {
		ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
		if (emptyWeeks.size() == 0) {
			emptyWeeks = fixNoEmptyWeeksForConfGame(school, opponent);
		}

		// Check for mutual top rivalry and attempt to schedule for week 13 or 12
		if (isTopRivals(school, opponent)) {
			return scheduleTopRivalGame(emptyWeeks);
		}

		// Remove week 14 from consideration if there are other options
		if (emptyWeeks.size() > 1) {
			emptyWeeks.remove(Integer.valueOf(14));
		}

		// If no weeks are available, throw a custom exception
		if (emptyWeeks.isEmpty()) {
			throw new NoWeeksAvailableException(school, opponent, gameRepository.findGamesByTeam(school),
					gameRepository.findGamesByTeam(opponent));
		}

		// Randomize the week from the remaining available weeks
		return randomizeWeek(emptyWeeks);
	}

	private boolean isTopRivals(School school, School opponent) {
		return !CollectionUtils.isEmpty(school.getRivals()) && !CollectionUtils.isEmpty(opponent.getRivals())
				&& school.getRivals().get(0).equals(opponent);
	}

	private int scheduleTopRivalGame(ArrayList<Integer> emptyWeeks) {
		if (emptyWeeks.contains(13)) {
			return 13;
		} else if (emptyWeeks.contains(12)) {
			return 12;
		} else {
			return randomizeWeek(emptyWeeks);
		}
	}

	public Game getGameBySchoolAndWeek(School school, int week) {
		List<Game> schedule = gameRepository.findGamesByTeam(school);
		Optional<Game> optional = schedule.stream().filter(game -> game.getWeek() == week).findFirst();
		return optional.orElse(null);
	}

	/**
	 * @return List<School> of schools with < 12 games
	 */
	public List<School> findTooFewGames() {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> school.getNcaaDivision().isFBS() && getScheduleBySchool(school).size() < 12)
				.collect(Collectors.toList());
	}

	/**
	 * Finds schools with more than 12 games in their schedule.
	 *
	 * @return a list of schools with too many games
	 */
	public List<School> findTooManyGames() {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> school.getNcaaDivision().isFBS() && getScheduleBySchool(school).size() > 12)
				.collect(Collectors.toList());
	}
}
