package com.robotdebris.ncaaps2scheduler.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.PostConstruct;

import com.robotdebris.ncaaps2scheduler.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.robotdebris.ncaaps2scheduler.ExcelReader;

@Service
public class ScheduleService {

    @Autowired
    SchoolList schoolList;
    @Autowired
    ConferenceList conferenceList;
    @Autowired
    SeasonSchedule seasonSchedule;
    @Autowired
    ExcelReader excelReader;

    @PostConstruct
    public void init() {

        // final String schoolsFile = "src/main/resources/School_Data.xlsx";
        final String schoolsFile = "resources/app/School_Data.xlsx";
//	    
        try {
            schoolList = excelReader.getSchoolData(schoolsFile);
            Collections.sort(schoolList);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getYear() {
        return seasonSchedule.getYear();
    }

    public void setYear(int year) {
        seasonSchedule.setYear(year);
    }

    public void setScheduleFile(MultipartFile scheduleFile) throws IOException {
        File file = multipartFileToFile(scheduleFile);
        try {
            seasonSchedule = excelReader.getScheduleData(file, schoolList);
            // is this going to miss conference games since conferences aren't set yet?
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }

    public SchoolList getSchoolList() {
        return schoolList;
    }

    public School getSchool(int schoolId) {
        return schoolList.get(schoolId);
    }

    public SchoolSchedule getSchoolSchedule(int schoolId) {
        return schoolList.get(schoolId).getSchedule();
    }

    public SchoolList getSchoolRivals(int schoolId) {
        return schoolList.get(schoolId).getRivals();
    }

    public School searchSchoolByTgid(int tgid) {
        return schoolList.schoolSearch(tgid);
    }

    public School searchSchoolByName(String name) {
        return schoolList.schoolSearch(name);
    }

    public SchoolList getAvailableOpponents(int tgid, int week) {
        School input = schoolList.schoolSearch(tgid);
        SchoolList availableOpponents = new SchoolList();
        for (School school : schoolList) {
            // if they don't already play one another
            if (!input.isOpponent(school)) {
                // if they don't have a game that week
                if (school.getSchedule().getGame(week) == null) {
                    availableOpponents.add(school);
                }
            }
        }
        return availableOpponents;
    }

    public SchoolList getAvailableRivals(int tgid, int week) {
        School input = schoolList.schoolSearch(tgid);
        SchoolList availableOpponents = new SchoolList();
        for (School school : input.getRivals()) {
            // if they don't already play one another
            if (input.isPossibleOpponent(school)) {
                // if they don't have a game that week
                if (school.getSchedule().getGame(week) != null) {
                    availableOpponents.add(school);
                }
            }
        }
        return availableOpponents;
    }

    public int removeAllOocNonRivalGames() {
        return seasonSchedule.removeAllNonConferenceGames(false);
    }

    public int removeAllOocGames() {
        return seasonSchedule.removeAllNonConferenceGames(true);
    }

    public int removeAllFcsGames() {
        return seasonSchedule.removeAllFcsGames();
    }

    public int removeAllGames() {
        return seasonSchedule.removeAllGames();
    }

    public void removeGame(int tgid, int week) {
        School input = schoolList.schoolSearch(tgid);
        Game game = input.getSchedule().getGame(week);
        seasonSchedule.removeGame(game);
    }

    public void addGame(int awayId, int homeId, int week) {
        School home = searchSchoolByTgid(homeId);
        School away = searchSchoolByTgid(awayId);
        int day = 5;
        seasonSchedule.addGameSpecificHomeTeam(away, home, week, day);
    }

    public ArrayList<Integer> getEmptyWeeks(int id, int id2) {
        School s1 = searchSchoolByTgid(id);
        School s2 = searchSchoolByTgid(id2);
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
        return findEmptyWeeks(s1weeks, s2weeks);
    }

    // finds empty weeks for one school
    public ArrayList<Integer> findEmptyWeeks(School s) {
        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        ArrayList<Integer> usedWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s.getSchedule().size(); i++) {
            usedWeeks.add(s.getSchedule().get(i).getWeek());
        } // populates freeWeeks with 0-14, all the possible weeks for regular season
        // games
        for (int i = 0; i < 15; i++) {
            if (!usedWeeks.contains(i)) {
                freeWeeks.add(i);
            }
        }
        return freeWeeks;
    }

    private ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }

    private ArrayList<Integer> findEmptyWeeks(School s1, School s2) {// returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
        return findEmptyWeeks(s1weeks, s2weeks);
    }

    public SuggestedGameResponse getSuggestedGame(int tgid) {
        School thisSchool = schoolList.schoolSearch(tgid);
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
            if (thisSchool.isPossibleOpponent(rival)) {
                // should isPossibleOpponent check this instead?
                if (rival.getSchedule().size() < 12) {
                    ArrayList<Integer> emptyWeeks = findEmptyWeeks(thisSchool, rival);
                    if (emptyWeeks.contains(12)) {
                        return new SuggestedGameResponse(12, rival, isHomeGame);
                        // week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is
                        // referenced by 12
                    } else if (emptyWeeks.contains(11)) {
                        return new SuggestedGameResponse(11, rival, isHomeGame);
                        // week 12 is empty
                    } else if (emptyWeeks.contains(13)) {
                        return new SuggestedGameResponse(13, rival, isHomeGame);
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
        count += addRivalryGamesAll(seasonSchedule, schoolList, aggressive);
        count -= removeExtraGames(seasonSchedule, schoolList);
        count += fillOpenGames(seasonSchedule, schoolList);
        return count;
    }

    private int fillOpenGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        int count = 0;
        SchoolList tooFewGames = schoolList.findTooFewGames();
        count += addRivalryGamesAll(seasonSchedule, tooFewGames, false);
        // shouldn't this recalculate tooFewGames?
        tooFewGames = schoolList.findTooFewGames();
        count += addRandomGames(seasonSchedule, schoolList, tooFewGames);
        return count;
    }

    private int removeExtraGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        int count = 0;
        SchoolList tooManyGames = schoolList.findTooManyGames();
        for (int i = 0; i < tooManyGames.size(); i++) {
            School school = tooManyGames.get(i);
            while (school.getSchedule().size() > 12) {
                Game removeMe = school.findRemovableGame();
                if (removeMe != null) {
                    seasonSchedule.removeGame(removeMe);
                    count++;
                } else {
                    // remove extra rivalry games
                    for (int j = school.getRivals().size() - 1; school.getSchedule().size() > 12; j--) {
                        School rival = school.getRivals().get(j);
                        if (school.isOpponent(rival)) {
                            removeMe = findGame(school, rival);
                            if (removeMe.getConferenceGame() == 0) {
                                seasonSchedule.removeGame(removeMe);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count / 2;// it's returning 1 removed game as removed for both schools
    }

    private Game findGame(School s1, School s2) {
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game game = s1.getSchedule().get(i);
            if (game.getHomeTeam().getTgid() == s2.getTgid() || game.getAwayTeam().getTgid() == s2.getTgid())
                return game;
        }
        return null;
    }

    private int addRivalryGamesAll(SeasonSchedule seasonSchedule, SchoolList allSchools, boolean aggressive) {
        int count = 0;
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                // go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getNcaaDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    count += addRivalryGameTwoSchools(seasonSchedule, s1, rival, aggressive, j);
                }
            }
        }
        return count;
    }

    private int addRivalryGameTwoSchools(SeasonSchedule seasonSchedule, School school, School rival, boolean aggressive,
                                         int rivalRank) {
        // School rival = school.getRivals().get(j);
        int count = 0;
        if (school.isPossibleOpponent(rival)) {
            if (aggressive && rivalRank < 2) {
                count += aggressiveAddRivalryGameHelper(seasonSchedule, school, rival);
            }
            // if they don't play and aren't in the same conference
            // go through all the rivals for a team
            else if (rival.getSchedule().size() < 12 && school.getSchedule().size() < 12) {
                // and stop if the seasonSchedule is full
                count += addRivalryGameHelper(seasonSchedule, school, rival, rivalRank);
            }
        }
        return count;
    }

    private int addRivalryGameHelper(SeasonSchedule seasonSchedule, School s1, School rival, int rivalRank) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        if (rivalRank < 2) {
            if (emptyWeeks.contains(12)) {
                seasonSchedule.addGame(s1, rival, 12, 5);
                return 1;
                // week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is
                // referenced by 12
            } else if (emptyWeeks.contains(11)) {
                seasonSchedule.addGame(s1, rival, 11, 5);
                return 1;
                // week 12 is empty
            } else if (emptyWeeks.contains(13)) {
                seasonSchedule.addGame(s1, rival, 13, 5);
                return 1;
                // week 14 is empty
            } else if (!emptyWeeks.isEmpty()) {
                seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
                return 1;
                // add game at emptyWeeks.get(0);
            }
        } else if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
            return 1;
        }
        return 0;
    }

    private int aggressiveAddRivalryGameHelper(SeasonSchedule seasonSchedule, School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1weeks, rweeks);
        if (emptyWeeks.contains(12)) {
            seasonSchedule.addGame(s1, rival, 12, 5);
            return 1;
            // week 13 is empty
        } else if (emptyWeeks.contains(11)) {
            seasonSchedule.addGame(s1, rival, 11, 5);
            return 1;
            // week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            seasonSchedule.addGame(s1, rival, 13, 5);
            return 1;
            // week 14 is empty
        }
        if (s1weeks.contains(12)) {
            // if the first team has an opening in week 13...
            Game game = rival.getSchedule().getGame(12);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;// or should this be a 0?
            }
        }
        if (s1weeks.contains(11)) {
            // if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(11);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (s1weeks.contains(13)) {
            // if the first team has an opening in week 14...
            Game game = rival.getSchedule().getGame(13);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(12)) {
            // if the first team has an opening in week 13...
            Game game = s1.getSchedule().getGame(12);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(11)) {
            // if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(11);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(13)) {
            // if the first team has an opening in week 14...
            Game game = s1.getSchedule().getGame(13);
            // set game to variable
            if (game.isRemovableGame()) {
                // if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getSchedule().getGame(12);
            Game rgame = rival.getSchedule().getGame(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return 1; // should this still return a 1? I guess so. Just counting added games
                // remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(11) && !rweeks.contains(11)) {
            Game s1game = s1.getSchedule().getGame(11);
            Game rgame = rival.getSchedule().getGame(11);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return 1;
                // remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return 1;
                // remove both games and replace with this one...
            }
        }
        if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
            return 1;
            // add game at emptyWeeks.get(0);
        }
        return 0;
    }

    private int addRandomGames(SeasonSchedule seasonSchedule, SchoolList allSchools, SchoolList needGames) {
        int count = 0;
        for (int i = 0; i < needGames.size(); i++) {
            School s1 = needGames.get(i);
            SchoolList myOptions = new SchoolList();
            for (int j = 0; j < needGames.size(); j++) {
                myOptions.add(needGames.get(j));
            }
            boolean exit = false;
            while (!exit && !myOptions.isEmpty()) {
                int max = myOptions.size() - 1;
                int min = 0;
                int range = max - min + 1;
                int randomNum = (int) (Math.random() * range) + min;
                School randomSchool = myOptions.get(randomNum);
                ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, randomSchool);
                if (randomSchool.getSchedule().size() < 12) {
                    if (s1.isPossibleOpponent(randomSchool) && !emptyWeeks.isEmpty()) {
                        // verify Alabama won't play Michigan to end the year. Instead they'll play LA
                        // Monroe
                        if (emptyWeeks.get(0) < 11
                                || (s1.getConference().isPowerConf() ^ randomSchool.getConference().isPowerConf())) {
                            seasonSchedule.addGame(s1, randomSchool, emptyWeeks.get(0), 5);
                            count++;
                        }
                    }
                    myOptions.remove(randomSchool);
                    if (randomSchool.getSchedule().size() > 11) {
                        if (randomNum < i) {
                            i--;
                        }
                        needGames.remove(randomSchool);
                    }
                    if (myOptions.isEmpty()) {
                        exit = true;
                    }
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
            for (int i = 0; i < needGames.size(); i++) {
                School s1 = needGames.get(i);
                for (int j = 0; j < allSchools.size() && s1.getSchedule().size() < 12; j++) {
                    if (!allSchools.get(j).getNcaaDivision().equals("FBS")) {
                        School fcs = allSchools.get(j);
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, fcs);
                        if (!emptyWeeks.isEmpty()) {
                            seasonSchedule.addGame(s1, fcs, emptyWeeks.get(0), 5);
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
        count += removeExtraGames(seasonSchedule, schoolList);
        // count += fillOpenGames(seasonSchedule, schoolList);
        return count;
    }

    public void saveToFile() {
        try {
            excelReader.writeSchedule(seasonSchedule);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Conference searchConferenceByName(String name) {
        return conferenceList.conferenceSearch(name);
    }

    public ConferenceList getConferenceList() {
        return conferenceList;
    }

    public SchoolList getSchoolsByConference(String name) {
        if (name.equalsIgnoreCase("All")) {
            return schoolList;
        }
        return schoolList.getAllSchoolsInConference(name);
    }

    public void setAlignmentFile(MultipartFile alignmentFile) throws IOException {
        File file = multipartFileToFile(alignmentFile);
        try {
            conferenceList = excelReader.getConferenceData(file);
            excelReader.setAlignmentData(file, schoolList, conferenceList);
            conferenceList.setConferencesSchoolList(schoolList);
            Collections.sort(schoolList);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public ByteArrayInputStream downloadSchedule() {
        try {
            return excelReader.writeSchedule(seasonSchedule);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public int autoAddRivalries() {
        int count = 0;
        count += addRivalryGamesAll(seasonSchedule, schoolList, false);
        return count;
    }

    public int autoAddRandomly() {
        int count = 0;
        SchoolList tooFewGames = schoolList.findTooFewGames();
        count += addRandomGames(seasonSchedule, schoolList, tooFewGames);
        return count;
    }

    public int autoAddConferenceGames(String name, int attempts) {
        Conference conf = conferenceList.conferenceSearch(name);
        //setAllYearlyGames();

        try {
            if (conf.getSchools().size() <= 10) {
                scheduleConferenceGamesUnder10Teams(conf);
            } else if (conf.getSchools().size() >= 12) {
                scheduleConferenceGamesDivisions(conf);
            }
        } catch (IndexOutOfBoundsException e) {
            if (attempts < 25) {
                seasonSchedule.removeAllConferenceGames(conf);
                autoAddConferenceGames(name, ++attempts);
            } else {
                throw e;
            }
        }
        return 0;
    }

    public void scheduleConferenceGamesUnder10Teams(Conference conf) throws IndexOutOfBoundsException  {
        scheduleConferenceGamesUnder10Teams(conf.getSchools(), conf.getConfGamesStartWeek());
    }

    public void scheduleConferenceGamesUnder10Teams(SchoolList list, int confGamesStartDate) throws IndexOutOfBoundsException  {
        int numOfSchools = list.size();
        for (School school : list) {
            if (school.getNumOfConferenceGames() < numOfSchools - 1) {
                for (School opponent : list) {
                    if (!school.equals(opponent) && !school.isOpponent(opponent)) {
                        int week = randomizeConfGameWeek(school, opponent, confGamesStartDate);
                        if ((school.getNumOfAwayConferenceGames() >= numOfSchools / 2)
                                || opponent.getNumOfHomeConferenceGames() >= numOfSchools / 2) {
                            //add a home game for school
                            if (seasonSchedule.getYear() % 2 == 0) {
                                addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), false);
                            } else {
                                addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), false);
                            }
                        } else if ((school.getNumOfHomeConferenceGames() >= numOfSchools / 2)
                                || opponent.getNumOfAwayConferenceGames() >= numOfSchools / 2) {
                            //add an away game for school
                            if (seasonSchedule.getYear() % 2 == 0) {
                                addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), false);
                            } else {
                                addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), false);
                            }
                        } else {
                            addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), false);
                        }
                    }
                }
            }
        }
    }

    public void scheduleConferenceGamesDivisions(Conference conf) throws IndexOutOfBoundsException {
        try {
            int index = 0;
            // move school order by year

            SchoolList div1 = conf.getSchoolsByDivision(conf.getDivisions().get(0));
            SchoolList div2 = conf.getSchoolsByDivision(conf.getDivisions().get(1));

            //schedule inner division games
            scheduleConferenceGamesUnder10Teams(div1, conf.getConfGamesStartWeek());
            scheduleConferenceGamesUnder10Teams(div2, conf.getConfGamesStartWeek());

            //order by cross div rivals
            boolean xDivRivals = div1.getFirst().getxDivRival() != null;
            int numOfConfGames = conf.getNumOfConfGames();
            int yearMinus2005 = seasonSchedule.getYear() - 2005;

            // 8 conf games no rivals
            if (numOfConfGames == 8 && !xDivRivals) {
                int i = 0;
                for (School school : div1) {
                    //if year is 0, 1, and school is 0, 1, 2... schedule abc, else xyz
                    if (i < div1.size() / 2 && yearMinus2005 % 4 <= 2) {
                        for (int j = 0; j < div1.size() / 2; j++) {
                            School opponent = div2.get(j);
                            int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());

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
                            int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());

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
                //0 1 2 3
                //1 1 2 3
                //2 1 2 3
                //3 4 5 6
                //4 4 5 6
                //5 4 5 6

            }

            if (xDivRivals) {
                div2 = orderDivByXDivRivals(div1);
            }

            if (yearMinus2005 % 6 == 0) {
                // second attempt at rotating logic... test this in 9 game conf schedules!!
            } else if (yearMinus2005 % 6 == 1) {
                int i = 0;
                while (i < 1) {
                    School s1 = div1.removeLast();
                    div1.addFirst(s1);
                    i++;
                }
            } else if (yearMinus2005 % 6 == 2) {
                int i = 0;
                while (i < 2) {
                    School s1 = div1.removeLast();
                    div1.addFirst(s1);
                    i++;
                }
            } else if (yearMinus2005 % 6 == 3) {
                int i = 0;
                while (i < 3) {
                    School s1 = div1.removeLast();
                    div1.addFirst(s1);
                    i++;
                }

            } else if (yearMinus2005 % 6 == 4) {
                int i = 0;
                while (i < 4) {
                    School s1 = div1.removeLast();
                    div1.addFirst(s1);
                    i++;
                }
            } else if (yearMinus2005 % 6 == 5) {
                int i = 0;
                while (i < 5) {
                    School s1 = div1.removeLast();
                    div1.addFirst(s1);
                    i++;
                }
            }
            for (School school : div1) {
//                int numOfConfGames = 9;

                if (numOfConfGames == 8 && xDivRivals) {
                    if (school.getxDivRival() != null) {
                        School opponent = school.getxDivRival();
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        //should be home or away game?
                        if (school.getNumOfHomeConferenceGames() >= div1.size() / 2) {
                            addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                        } else {
                            addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
                        }

                    }
                    if (index == 0) {
                        School opponent = div2.get(1);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 2;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    } else if (index == 1) {
                        School opponent = div2.get(2);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 3;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    } else if (index == 2) {
                        School opponent = div2.get(3);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 4;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    } else if (index == 3) {
                        School opponent = div2.get(4);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 5;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    } else if (index == 4) {
                        School opponent = div2.get(5);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 0;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    } else if (index == 5) {
                        School opponent = div2.get(0);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        int opponent2Id = 1;
                        if (div2.get(opponent2Id) == school.getxDivRival()) {
                            opponent2Id = opponent2Id < div2.size() - 1 ? opponent2Id++ : 0;
                        }
                        opponent = div2.get(opponent2Id);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    }
                }

                // 0 1 2 3
                if (numOfConfGames == 9) {
                    if (index == 0) {
                        School opponent = div2.get(0);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(1);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(2);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(3);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        //0 1 4 5
                    } else if (index == 1) {
                        School opponent = div2.get(0);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(1);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(4);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(5);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        //2 3 4 5
                    } else if (index == 2) {
                        School opponent = div2.get(2);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(3);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(4);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(5);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        //2 3 0 1
                    } else if (index == 3) {
                        School opponent = div2.get(2);
                        int week = randomizeWeek(school, opponent);
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(3);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(0);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(1);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
                    }

                    //4 5 0 1
                    else if (index == 4) {
                        School opponent = div2.get(4);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(5);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(0);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(1);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);
                    }

                    //4 5 2 3
                    else if (index == 5) {
                        School opponent = div2.get(4);
                        int week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(5);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(2);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(school, opponent, week, 5, seasonSchedule.getYear(), true);

                        opponent = div2.get(3);
                        week = randomizeConfGameWeek(school, opponent, conf.getConfGamesStartWeek());
                        addYearlySeriesHelper(opponent, school, week, 5, seasonSchedule.getYear(), true);
                    }
                }// end of 9 game loop
                index++;
            }//end of for team loop

        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
//            }
			/*
			year 1
			0 6
			7 0
			0 8
			9 0

			6   1
			1   7
			10  1
			1  11

			2   8
			9   2
			2  10
			11  2

			8 3
			3 9
			6 3
			3 7

			4 10
			11 4
			4 6
			7 4

			10 5
			5 11
			8 5
			5 9
			 */
    }

    private SchoolList orderDivByXDivRivals(SchoolList div1) {
        SchoolList orderedDiv = new SchoolList();
        for (School school : div1) {
            orderedDiv.add(school.getxDivRival());
        }
        return orderedDiv;
    }

    private int randomizeWeek(School school, School opponent) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
        emptyWeeks.remove(Integer.valueOf(14));
        int max = emptyWeeks.size() - 1;
        int min = 0;
        int range = max - min + 1;
        int randomNum = (int) (Math.random() * range) + min;
        return emptyWeeks.get(randomNum);
    }

    private int randomizeConfGameWeek(School school, School opponent, int startWeek) throws IndexOutOfBoundsException {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(school, opponent);
        emptyWeeks.remove(Integer.valueOf(14));
        for (int i = 0; i < emptyWeeks.size(); i++) {
            int week = emptyWeeks.get(i);
            if (week < startWeek) {
                emptyWeeks.remove(Integer.valueOf(week));
                i--;
            }
        }
        int max = emptyWeeks.size() - 1;
        int min = 0;
        int range = max - min + 1;
        int randomNum = (int) (Math.random() * range) + min;
//        try {
        return emptyWeeks.get(randomNum);
//        } catch (IndexOutOfBoundsException e) {
//            //retry?
//        }
    }

    public void setAllYearlyGames() {
        /*
         *
         * week 14 army navy (only game of the week)
         *
         * week 13 Ole Miss Miss St UNC NC St? USF UCF EMU CMU WSU Wash UGA GT (Sat) OU
         * OKST Bama Auburn PSU MSU OSU Oregon ND Stanford? UK UL LSU A&M/Ark? Pitt Cuse
         * IU Purdue FSU Florida Clem USC Vandy UT ILL NW Wisc Minn Mich OSU ULM ULL Cal
         * UCLA Zona ASU WKU Marshall? Col Utah? VT UVA Mizzou Kansas OU Nebraska UT
         * Vandy Duke Wake Cuse BC
         *
         *
         * week 10 LSU Bama ? UK UT
         *
         * week 9 Mich MSU? FSU Clem? UGA UF
         *
         * Week 8 Bama UT LSU Ole Miss? probs not
         *
         * Week 7 SDSU SJSU? Pitt VT?
         *
         * Week 6 OU Texas UGA Auburn? probs not
         *
         *
         */
        // week 6
        addYearlySeriesHelper("Oklahoma", "Texas", 6, 5, seasonSchedule.getYear(), false);

        // week 8
        addYearlySeriesHelper("Alabama", "Tennessee", 8, 5, seasonSchedule.getYear(), false);

        // week 9
        addYearlySeriesHelper("Georgia", "Florida", 9, 5, seasonSchedule.getYear(), false);

        // week 12
        addYearlySeriesHelper("Tennessee", "Kentucky", 12, 5, seasonSchedule.getYear(), false);

        // week 13 (rivalry week)
        addYearlySeriesHelper("Virginia", "Virginia Tech", 13, 5, seasonSchedule.getYear(), false);
        addYearlySeriesHelper("North Carolina", "North Carolina State", 13, 5, seasonSchedule.getYear(), false);
        for (School school : schoolList) {
            if (!school.getNcaaDivision().equalsIgnoreCase("FCS")) {
                boolean endLoop = false;
                int i = 0;
                while (!endLoop) {
                    if (school.getRivals().size() > i) {
                        School rival = school.getRivals().get(i);
                        endLoop = addYearlySeriesHelper(school, rival, 13, 5, seasonSchedule.getYear(), false);
                        i++;
                    } else {
                        endLoop = true;
                    }
                }
            }
        }

        // week 14
        addYearlySeriesHelper("Navy", "Army", 14, 5, seasonSchedule.getYear(), false);

    }

    public boolean addYearlySeriesHelper(String s1, String s2, int week, int day, int year, boolean specifyHome) {
        School school1 = schoolList.schoolSearch(s1);
        School school2 = schoolList.schoolSearch(s2);
        return addYearlySeriesHelper(school1, school2, week, day, year, specifyHome);
    }

    public boolean addYearlySeriesHelper(School school1, School school2, int week, int day, int year, boolean specifyHome) {
        if (!school1.isOpponent(school2) && school1.getSchedule().size() < 12 && school2.getSchedule().size() < 12
                && school1.getSchedule().getGame(week) == null && school2.getSchedule().getGame(week) == null) {
            // check if out of division conf opponent here?
            if (!specifyHome) {
                seasonSchedule.addGameYearlySeries(school1, school2, week, day, year);
            } else {
                seasonSchedule.addGameSpecificHomeTeam(school1, school2, week, day);
            }
            return true;
        }
        return false;
    }

    public ArrayList<Game> getScheduleByWeek(int week) {
        return seasonSchedule.getScheduleByWeek(week);
    }

    public SeasonSchedule getBowlGames() {
        return seasonSchedule.getBowlSchedule();
    }

    public int removeConferenceGames(String name) {
        Conference conf = conferenceList.conferenceSearch(name);
        return seasonSchedule.removeAllConferenceGames(conf);
    }
}
