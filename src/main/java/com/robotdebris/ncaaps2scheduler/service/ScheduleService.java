package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.*;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceScheduler;
import com.robotdebris.ncaaps2scheduler.scheduler.conference.ConferenceSchedulerFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static com.robotdebris.ncaaps2scheduler.SchedulerUtils.findEmptyWeeks;

@Service
public class ScheduleService {

    private final GameRepository gameRepository;
    private final Logger LOGGER = Logger.getLogger(ScheduleService.class.getName());
    int year = 2005;
    @Autowired
    SchoolService schoolService;
    @Autowired
    ConferenceService conferenceService;
    @Autowired
    ExcelReader excelReader;

    public ScheduleService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getSeasonSchedule() {
        return gameRepository.findAll();
    }

    public void setSeasonSchedule(List<Game> seasonSchedule) {
        gameRepository.saveAll(seasonSchedule);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
        return !input.isOpponent(school) && school.getGameByWeek(week) == null;
    }

    public List<School> getOpenNonConferenceRivals(int tgid, int week) {
        School input = schoolService.schoolSearch(tgid);
        if (input == null) {
            throw new IllegalArgumentException("School not found with ID: " + tgid);
        }

        return input.getRivals().stream().filter(input::isEligibleNonConferenceMatchup)
                .filter(school -> school.getGameByWeek(week) == null).collect(Collectors.toList());
    }

    public int removeAllOocNonRivalGames() {
        return removeAllNonConferenceGames(false);
    }

    public int removeAllOocGames() {
        return removeAllNonConferenceGames(true);
    }

    public void removeGame(int tgid, int week) {
        School input = schoolService.schoolSearch(tgid);
        Game game = input.getGameByWeek(week);
        removeGame(game);
    }

    public void addGame(int awayId, int homeId, int week) {
        School home = searchSchoolByTgid(homeId);
        School away = searchSchoolByTgid(awayId);
        int day = 5;
        addGameSpecificHomeTeam(away, home, week, day);
    }

//    private ArrayList<Integer> fixNoEmptyWeeks(School s1, School s2) {
//
//        // trying to schedule WVU vs PSU
//        // WVU has weeks 3, 5, 7, 9 available
//        // PSU has weeks 4, 6, 8, 10 available
//        // WVU plays UL week 6
//        // UL has week 7 available
//        // change WVU vs UL to week 7
//        // schedule WVU vs PSU
//        ArrayList<Integer> s1EmptyWeeks = findEmptyWeeks(s1);
//        ArrayList<Integer> s2EmptyWeeks = findEmptyWeeks(s2);
//        int confGamesStartDate = s1.getConference().getConfGamesStartWeek();
//
//        for (Game game : s1.getSchedule()) {
//            if (s2EmptyWeeks.contains(game.getWeek())) {
//                School opponent = game.getHomeTeam().equals(s1) ? game.getAwayTeam() : game.getHomeTeam();
//                ArrayList<Integer> opponentEmptyWeeks = findEmptyWeeks(opponent);
//                ArrayList<Integer> jointEmptyWeeks = findEmptyWeeksInConferenceHelper(s1EmptyWeeks, opponentEmptyWeeks,
//                        confGamesStartDate);
//
//                // move the game to another week
//                if (!jointEmptyWeeks.isEmpty()) {
//                    System.out.println("Moving game: " + game.getAwayTeam() + " at " + game.getHomeTeam()
//                            + ", because no available week was found for " + s1 + " vs " + s2);
//                    removeGame(s1.getTgid(), game.getWeek());
//                    addGame(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(), randomizeWeek(jointEmptyWeeks));
//                    break;
//                }
//            }
//        }
//        s1EmptyWeeks = findEmptyWeeks(s1);
//        s2EmptyWeeks = findEmptyWeeks(s2);
//        return SchedulerUtils.findEmptyWeeksHelper(s1EmptyWeeks, s2EmptyWeeks);
//    }


    public SuggestedGameResponse getSuggestedGame(int tgid) {
        School thisSchool = schoolService.schoolSearch(tgid);
        int homeGameCount = 0;
        boolean isHomeGame = false;
        for (Game game : thisSchool.getSchedule()) {
            if (game.getHomeTeam().getTgid() == tgid) {
                homeGameCount++;
            }
        }

        if (homeGameCount < 6) {
            isHomeGame = true;
        }

        for (School rival : thisSchool.getRivals()) {
            if (thisSchool.isEligibleNonConferenceMatchup(rival)) {
                // should isPossibleOpponent check this instead?
                if (rival.getSchedule().size() < 12) {
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

    public int autoAddGames(boolean aggressive) {
        int count = 0;
        count += addRivalryGamesAll(schoolService.getAllSchools(), aggressive);
        count -= removeExtraGames(schoolService.getAllSchools());
        count += fillOpenGames(schoolService.getAllSchools());
        return count;
    }

    private int fillOpenGames(List<School> schoolList) {
        int count = 0;
        List<School> tooFewGames = schoolService.findTooFewGames();
        count += addRivalryGamesAll(tooFewGames, false);
        // recalculate tooFewGames?
        tooFewGames = schoolService.findTooFewGames();
        count += addRandomGames(schoolList, tooFewGames);
        return count;
    }

    private int removeExtraGames(List<School> schoolList) {
        int count = 0;
        List<School> tooManyGames = schoolService.findTooManyGames();
        for (int i = 0; i < tooManyGames.size(); i++) {
            School school = tooManyGames.get(i);
            while (school.getSchedule().size() > 12) {
                Game removeMe = school.findNonConferenceNonRivalryGame();
                if (removeMe != null) {
                    removeGame(removeMe);
                    count++;
                } else {
                    // remove extra rivalry games
                    for (int j = school.getRivals().size() - 1; school.getSchedule().size() > 12; j--) {
                        School rival = school.getRivals().get(j);
                        if (school.isOpponent(rival)) {
                            removeMe = findFirstGameBetweenSchools(school, rival);
                            if (removeMe.getConferenceGame() == 0) {
                                removeGame(removeMe);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count / 2;// it's returning 1 removed game as removed for both schools
    }

    // finds the FIRST game between 2 schools.
    private Game findFirstGameBetweenSchools(School s1, School s2) {
        return s1.getSchedule().stream().filter(
                        game -> game.getHomeTeam().getTgid() == s2.getTgid() || game.getAwayTeam().getTgid() == s2.getTgid())
                .findFirst().orElse(null);
    }

    // find game by week number and game number
    private Game findGameByWeekAndGameNumber(int week, int gameNumber) {
        return getSeasonSchedule().stream().filter(game -> game.getWeek() == week && game.getGameNumber() == gameNumber)
                .findFirst().orElse(null);
    }

    private int addRivalryGamesAll(List<School> allSchools, boolean aggressive) {
        int count = 0;
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                // go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getNcaaDivision().equals("FBS") && j < s1.getRivals().size()) {
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

    private int addRivalryGameTwoSchools(School school, School rival, boolean aggressive, int rivalRank) {
        // School rival = school.getRivals().get(j);
        int count = 0;
        if (school.isEligibleNonConferenceMatchup(rival)) {
            if (aggressive && rivalRank < 2) {
                count += aggressiveAddRivalryGameHelper(school, rival);
            }
            // if they don't play and aren't in the same conference
            // go through all the rivals for a team
            else if (rival.getSchedule().size() < 12 && school.getSchedule().size() < 12) {
                // and stop if the seasonSchedule is full
                count += addRivalryGameHelper(school, rival, rivalRank);
            }
        }
        return count;
    }

    private int addRivalryGameHelper(School s1, School rival, int rivalRank) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        // TODO: move games if week 13 is taken (ie so FSU UF can be week 13 yearly
        if (rivalRank < 2) {
            if (emptyWeeks.contains(13)) {
                addGame(s1, rival, 13, 5);
                return 1;
                // week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is
                // referenced by 12
            } else if (emptyWeeks.contains(12)) {
                addGame(s1, rival, 12, 5);
                return 1;
                // week 12 is empty
            } else if (emptyWeeks.contains(11)) {
                addGame(s1, rival, 11, 5);
                return 1;
                // week 14 is empty
            } else if (!emptyWeeks.isEmpty()) {
                addGame(s1, rival, emptyWeeks.get(0), 5);
                return 1;
                // add game at emptyWeeks.get(0);
            }
        } else if (!emptyWeeks.isEmpty()) {
            addGame(s1, rival, emptyWeeks.get(0), 5);
            return 1;
        }
        return 0;
    }

    private int aggressiveAddRivalryGameHelper(School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        if (emptyWeeks.contains(12)) {
            addGame(s1, rival, 12, 5);
            return 1;
            // week 13 is empty
        } else if (emptyWeeks.contains(11)) {
            addGame(s1, rival, 11, 5);
            return 1;
            // week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            addGame(s1, rival, 13, 5);
            return 1;
            // week 14 is empty
        }
        if (s1weeks.contains(12)) {
            // if the first team has an opening in week 13...
            Game game = rival.getGameByWeek(12);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;// or should this be a 0?
            }
        }
        if (s1weeks.contains(11)) {
            // if the first team has an opening in week 12...
            Game game = rival.getGameByWeek(11);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (s1weeks.contains(13)) {
            // if the first team has an opening in week 14...
            Game game = rival.getGameByWeek(13);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(12)) {
            // if the first team has an opening in week 13...
            Game game = s1.getGameByWeek(12);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(11)) {
            // if the first team has an opening in week 12...
            Game game = s1.getGameByWeek(11);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(13)) {
            // if the first team has an opening in week 14...
            Game game = s1.getGameByWeek(13);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getGameByWeek(12);
            Game rgame = rival.getGameByWeek(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                removeGame(s1game);
                replaceGame(rgame, s1, rival);
                return 1; // should this still return a 1? I guess so. Just counting added games
                // remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(11) && !rweeks.contains(11)) {
            Game s1game = s1.getGameByWeek(11);
            Game rgame = rival.getGameByWeek(11);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                removeGame(s1game);
                replaceGame(rgame, s1, rival);
                return 1;
                // remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getGameByWeek(13);
            Game rgame = rival.getGameByWeek(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                removeGame(s1game);
                replaceGame(rgame, s1, rival);
                return 1;
                // remove both games and replace with this one...
            }
        }
        if (!emptyWeeks.isEmpty()) {
            addGame(s1, rival, emptyWeeks.get(0), 5);
            return 1;
            // add game at emptyWeeks.get(0);
        }
        return 0;
    }

    private int addRandomGames(List<School> allSchools, List<School> needGames) {
        int count = 0;
        for (int i = 0; i < needGames.size(); i++) {
            School s1 = needGames.get(i);
            List<School> myOptions = schoolService.findTooFewGames();
            myOptions.remove(s1);

            boolean exit = false;
            while (!exit && !myOptions.isEmpty()) {
                int max = myOptions.size() - 1;
                int min = 0;
                int range = max - min + 1;
                int randomNum = (int) (Math.random() * range) + min;
                School randomSchool = myOptions.get(randomNum);
                if (s1.isEligibleNonConferenceMatchup(randomSchool) && randomSchool.getSchedule().size() < 12) {
                    ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, randomSchool);
                    if (!emptyWeeks.isEmpty()) {
                        // verify Alabama won't play Michigan to end the year. Instead they'll play LA
                        // Monroe
                        if (emptyWeeks.get(0) < 11
                                || (s1.getConference().isPowerConf() ^ randomSchool.getConference().isPowerConf())) {
                            addGame(s1, randomSchool, emptyWeeks.get(0), 5);
                            count++;
                        }
                    }
                    // remove randomSchool from myOptions regardless of whether or not it was added
                    myOptions.remove(randomSchool);
                    if (randomSchool.getSchedule().size() > 11) {
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
                    if (s1.getSchedule().size() > 11) {
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
                if (!allSchools.get(i).getNcaaDivision().equals("FBS")) {
                    fcsSchoolList.add(allSchools.get(i));
                }
            }

            // add fcs games to all schools that still need games
            for (int i = 0; i < needGames.size(); i++) {
                School s1 = needGames.get(i);
                // for (int j = 0; j < allSchools.size() && s1.getSchedule().size() < 12; j++) {
                // if (!allSchools.get(j).getNcaaDivision().equals("FBS")) {

                while (s1.getSchedule().size() < 12) {

                    // get random school
                    int min = 0;
                    int max = fcsSchoolList.size() - 1;

                    Random random = new Random();

                    int randomValue = random.nextInt(max + min) + min;

                    School fcs = fcsSchoolList.get(randomValue);
                    if (!s1.isOpponent(fcs)) {
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, fcs);
                        if (!emptyWeeks.isEmpty()) {
                            addGame(s1, fcs, emptyWeeks.get(0), 5);
                            count++;
                        }
                    }
                }
            }
        }
        for (int i = needGames.size() - 1; i >= 0; i--) {
            if (needGames.get(i).getSchedule().size() == 12) {
                needGames.remove(i);
            }
        }
        return count;
    }

    public int fixSchedule() {
        int count = 0;
        count += removeExtraGames(schoolService.getAllSchools());
        // count += fillOpenGames(seasonSchedule, schoolList);
        return count;
    }

    public Conference searchConferenceByName(String name) {
        return conferenceService.conferenceSearch(name);
    }

    public List<Conference> getConferenceList() {
        return conferenceService.getConferenceList();
    }

    public List<School> getSchoolsByConference(String name) {
        if (name.equalsIgnoreCase("All")) {
            return schoolService.getAllSchools();
        }
        return schoolService.getAllSchoolsInConference(name);
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

    public int autoAddRivalries() {
        int count = 0;
        count += addRivalryGamesAll(schoolService.getAllSchools(), false);
        return count;
    }

    public int autoAddRandomly() {
        int count = 0;
        List<School> tooFewGames = schoolService.findTooFewGames();
        count += addRandomGames(schoolService.getAllSchools(), tooFewGames);
        return count;
    }

    public int autoAddConferenceGames(String name) throws Exception {
        Conference conf = conferenceService.conferenceSearch(name);
        // setAllYearlyGames();
        ConferenceScheduler scheduler = ConferenceSchedulerFactory.getScheduler(conf);
        scheduler.generateConferenceSchedule(conf, gameRepository);
        return 0;
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

    public int removeConferenceGames(String name) {
        Conference conf = conferenceService.conferenceSearch(name);
        return removeAllConferenceGames(conf);
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

    public Game getGame(int week, int gameId) {
        ArrayList<Game> weeklyGames = getScheduleByWeek(week);
        for (Game game : weeklyGames) {
            if (game.getGameNumber() == gameId) {
                return game;
            }
        }
        return null;
    }

    public void addGame(AddGameRequest addGameRequest) {
        if (addGameRequest.getGameResult() == null) {
            addGame(addGameRequest.getAwayId(), addGameRequest.getHomeId(), addGameRequest.getWeek());
        } else {
            School home = schoolService.schoolSearch(addGameRequest.getHomeId());
            School away = schoolService.schoolSearch(addGameRequest.getAwayId());
            addGameSpecificHomeTeam(away, home, addGameRequest.getWeek(), addGameRequest.getDay(),
                    addGameRequest.getTime(), addGameRequest.getGameResult());
        }
    }

    public void removeAllConferenceGames() {
        for (Conference conf : conferenceService.getConferenceList()) {
            this.removeConferenceGames(conf.getName());
        }
    }

    public void addAllConferenceGames() throws Exception {
        for (Conference conf : conferenceService.getConferenceList()) {
            // if conference is FBS...
            List<School> schools = conf.getSchools();
            if (schools != null) {
                if (Objects.equals(schools.get(0).getNcaaDivision(), "FBS")) {
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

    public void swapSchedule(int tgid1, int tgid2) {
        School s1 = schoolService.schoolSearch(tgid1);
        School s2 = schoolService.schoolSearch(tgid2);

        // remove original game from season schedule, and add to new lists to iterate
        // through
        ArrayList<Game> s1Schedule = new ArrayList<>();
        for (Game game : s1.getSchedule()) {
            s1Schedule.add(game);
        }

        ArrayList<Game> s2Schedule = new ArrayList<>();
        for (Game game : s2.getSchedule()) {
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

    /**
     * Adds game with a randomized home team
     *
     * @param s1   school 1
     * @param s2   school 2
     * @param week the week of the game
     * @param day  the day of the game
     */
    public void addGame(School s1, School s2, int week, int day) {
        randomizeHomeTeam(s1, s2, week, day, findNewGameNumber(week));
    }

    /**
     * Adds game to the schedule with the home team already selected
     *
     * @param away the away school
     * @param home the home school
     * @param week the week of the game
     * @param day  the day of the game
     */
    public void addGameSpecificHomeTeam(School away, School home, int week, int day) {
        addGame(away, home, week, day, findNewGameNumber(week));
    }

    public void addGameSpecificHomeTeam(School away, School home, int week, int day, int time, GameResult gameResult) {
        addGame(away, home, week, day, findNewGameNumber(week), time, gameResult);
    }

    public void addGameYearlySeries(School s1, School s2, int week, int day, int year) {
        // logic to decide home or away team here?
        if (year % 2 == 0) {
            addGame(s1, s2, week, day, findNewGameNumber(week));
        } else {
            addGame(s2, s1, week, day, findNewGameNumber(week));
        }
    }

    /**
     * Adds game to schedule after the home team is selected, either randomly or via
     * addGameSpecificHomeTeam method
     *
     * @param away       the away school
     * @param home       the home school
     * @param week       the week of the game
     * @param day        the day of the game
     * @param gameNumber the game of the week
     */
    private void addGame(School away, School home, int week, int day, int gameNumber) {
        Game newGame = new Game(away, home, gameNumber, week, day);
        addGame(newGame);
        LOGGER.info("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
        System.out.println("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
    }

    /**
     * Adds game to schedule after the home team is selected, either randomly or via
     * addGameSpecificHomeTeam method
     *
     * @param away       the away school
     * @param home       the home school
     * @param week       the week of the game
     * @param day        the day of the game
     * @param gameNumber the game of the week
     */
    private void addGame(School away, School home, int week, int day, int gameNumber, int time, GameResult gameResult) {
        Game newGame = new Game(away, home, gameNumber, week, day, time, gameResult);
        getSeasonSchedule().add(newGame);
        LOGGER.info("Adding game " + newGame.getAwayTeam().getName() + " at " + newGame.getHomeTeam().getName());
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
        s1.getSchedule().remove(theGame);
        s2.getSchedule().remove(theGame);
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
        int dayNumber = theGame.getDay();
        getSeasonSchedule().remove(theGame);
        LOGGER.info("Removing and replacing " + theGame.getAwayTeam() + " at " + theGame.getHomeTeam());
        theGame.getHomeTeam().getSchedule().remove(theGame);
        theGame.getAwayTeam().getSchedule().remove(theGame);
        randomizeHomeTeam(s1, s2, weekNumber, dayNumber, gameNumber);
    }

    /**
     * Adds a game with a random home team. This does contain logic for P5 getting
     * home preference over G5 and FCS schools as well.
     *
     * @param s1   school 1
     * @param s2   school 2
     * @param week week of the game
     * @param day  day of the game
     * @param game game number of the week
     */
    private void randomizeHomeTeam(School s1, School s2, int week, int day, int game) {
        if (s1.isRival(s2) || s1.getConference().isPowerConf() == s2.getConference().isPowerConf()) {
            int max = 2;
            int min = 1;
            int range = max - min + 1;
            int random = (int) (Math.random() * range) + min;
            if (random == 1 && (s1.isRival(s2) || s2.getNcaaDivision().isFBS())) {
                addGame(s1, s2, week, day, game);
            } else {
                addGame(s2, s1, week, day, game);
            }
        } else {
            if (s1.getConference().isPowerConf()) {
                addGame(s2, s1, week, day, game);
            } else {
                addGame(s1, s2, week, day, game);
            }
        }
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
            if (!game.getHomeTeam().getNcaaDivision().isFBS()
                    || !game.getAwayTeam().getNcaaDivision().isFBS()) {
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
    public int removeAllConferenceGames(Conference conf) {
        int count = 0;
        for (School school : conf.getSchools()) {
            for (int i = 0; i < school.getSchedule().size(); i++) {
                Game game = school.getSchedule().get(i);
                if (game.getHomeTeam().getConference() != null && game.getAwayTeam().getConference() != null
                        && game.getHomeTeam().getConference().getName()
                        .equalsIgnoreCase(game.getAwayTeam().getConference().getName())) {
                    this.removeGame(game);
                    i--;
                }
            }
        }
        return count;
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

    public int removeAllGames() {
        int count = 0;
        for (int i = 0; i < getSeasonSchedule().size(); i++) {
            Game game = getSeasonSchedule().get(i);
            this.removeGame(game);
            count++;
            i--;
        }
        return count;
    }

    /**
     * @return ArrayList of Strings of the SeasonSchedule
     */
    public ArrayList scheduleToList(boolean header) {
        ArrayList<ArrayList> list = new ArrayList();
        if (header) {
            ArrayList<String> firstLine = new ArrayList();
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

    public ArrayList<Game> getScheduleByWeek(int week) {
        ArrayList<Game> weeklySchedule = new ArrayList<>();
        for (Game game : getSeasonSchedule()) {
            if (game.getWeek() == week) {
                weeklySchedule.add(game);
            }
        }
        return weeklySchedule;
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

    public void addGame(Game game) {
        getSeasonSchedule().add(game);
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
}
