package com.robotdebris.ncaaps2scheduler.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.robotdebris.ncaaps2scheduler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.FileChooser;

@Service
public class ScheduleService {

	private static SchoolList schoolList = new SchoolList();

	private static SeasonSchedule seasonSchedule;

	
	static {
		FileChooser fileChooser = new FileChooser();
	    //final String schoolsFile = fileChooser.chooseFile("Select Custom Conferences Excel Document");
		final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
//	    if (schoolsFile == null){
//	        System.out.println("No file selected, exiting program.");
//	        System.exit(0);
//	    }
	    //final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
	    final String scheduleFile =  "src/main/resources/SCHED.xlsx";
//	    final String scheduleFile =  fileChooser.chooseFile("Select Schedule Excel Document");
//	    if (scheduleFile == null){
//	        System.out.println("No file selected, exiting program.");
//	        System.exit(0);
//	    }
	    //final String scheduleFile = "src/main/resources/SCHED.xlsx";
	    try {
			schoolList = ExcelReader.getSchoolData(schoolsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			seasonSchedule = ExcelReader.getScheduleData(scheduleFile, schoolList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //System.out.println("Welcome to the NCAA Football PS2 Scheduler");
	}
	
	public static SchoolList getSchoolList() {
		return schoolList;
	}
	
	public static School getSchool(int schoolId) {
		return schoolList.get(schoolId);
	}
	
	public static SchoolSchedule getSchoolSchedule(int schoolId) {
		return schoolList.get(schoolId).getSchedule();
	}
	
	public static SchoolList getSchoolRivals(int schoolId) {
		return schoolList.get(schoolId).getRivals();
	}
	
	public static School searchByTgid(int tgid) {
		return schoolList.schoolSearch(tgid);
    }
	
	public static School searchByName(String name) {
		return schoolList.schoolSearch(name);
    }

    public static SchoolList getAvailableOpponents(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		SchoolList availableOpponents = new SchoolList();
		for (School school: schoolList) {
			// if they don't already play one another
			if (input.isPossibleOpponent(school)){
				// if they don't have a game that week
				if (school.getSchedule().getGame(week) == null){
					availableOpponents.add(school);
				}
			}
		}
		return availableOpponents;
	}

	public static SchoolList getAvailableRivals(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		SchoolList availableOpponents = new SchoolList();
		for (School school: input.getRivals()) {
			// if they don't already play one another
			if (input.isPossibleOpponent(school)){
				// if they don't have a game that week
				if (school.getSchedule().getGame(week) != null){
					availableOpponents.add(school);
				}
			}
		}
		return availableOpponents;
	}

	public void removeAllOocNonRivalGames() {
		seasonSchedule.removeAllNonConferenceGames(false);
	}

	public void removeAllOocGames() {
		seasonSchedule.removeAllNonConferenceGames(true);
	}

	public void removeAllFcsGames() {
		seasonSchedule.removeAllFcsGames();
	}

	public void removeGame(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		Game game = input.getSchedule().getGame(week);
		seasonSchedule.removeGame(game);
	}

	public void addGame(int awayId, int homeId, int week){
		School home = ScheduleService.searchByTgid(homeId);
		School away = ScheduleService.searchByTgid(awayId);
		int day = 5;
		seasonSchedule.addGameSpecificHomeTeam(away, home, week, day);
	}

	public ArrayList<Integer> getEmptyWeeks(int id, int id2) {
		School s1 = ScheduleService.searchByTgid(id);
		School s2 = ScheduleService.searchByTgid(id2);
		ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
		ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
		return findEmptyWeeks(s1weeks, s2weeks);
	}

	//finds empty weeks for one school
	public static ArrayList<Integer> findEmptyWeeks(School s) {
		ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
		ArrayList<Integer> usedWeeks = new ArrayList<Integer>();
		for (int i = 0; i < s.getSchedule().size(); i++) {
			usedWeeks.add(s.getSchedule().get(i).getWeek());
		}//populates freeWeeks with 0-14, all the possible weeks for regular season games
		for (int i = 0; i < 15; i++) {
			if (!usedWeeks.contains(i)) {
				freeWeeks.add(i);
			}
		}
		return freeWeeks;
	}

	private static ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
		ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
		for (int i = 0; i < s1weeks.size(); i++) {
			if (s2weeks.contains(s1weeks.get(i))) {
				freeWeeks.add(s1weeks.get(i));
			}
		}
		return freeWeeks;
	}
	
	private static ArrayList<Integer> findEmptyWeeks(School s1, School s2) {//returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
        return findEmptyWeeks(s1weeks, s2weeks);
    }

	public Game getSuggestedGame(long id) {
		// TODO Auto-generated method stub
		return null;
		
	}

	public void autoAddGames(boolean aggressive) {
        addRivalryGamesAll(seasonSchedule, schoolList, aggressive);
        removeExtraGames(seasonSchedule, schoolList);
        fillOpenGames(seasonSchedule, schoolList);
		
	}
	
	private static void fillOpenGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        SchoolList tooFewGames = schoolList.findTooFewGames();
        addRivalryGamesAll(seasonSchedule, tooFewGames, false);
        addRandomGames(seasonSchedule, schoolList, tooFewGames);
    }
	
	private static void removeExtraGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        SchoolList tooManyGames = schoolList.findTooManyGames();
        for (int i = 0; i < tooManyGames.size(); i++) {
            School school = tooManyGames.get(i);
            while (school.getSchedule().size() > 12) {
                Game removeMe = school.findRemovableGame();
                if (removeMe != null) {
                    seasonSchedule.removeGame(removeMe);
                } else {
                    //remove extra rivalry games
                    for (int j = school.getRivals().size() - 1; school.getSchedule().size() > 12; j--) {
                        School rival = school.getRivals().get(j);
                        if (school.isOpponent(rival)) {
                            removeMe = findGame(school, rival);
                            if (removeMe.getConferenceGame() == 0) {
                                seasonSchedule.removeGame(removeMe);
                            }
                        }
                    }
                }
            }
        }
    }
	
	private static Game findGame(School s1, School s2) {
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game game = s1.getSchedule().get(i);
            if (game.getHomeTeam().getTgid() == s2.getTgid() ||
                    game.getAwayTeam().getTgid() == s2.getTgid())
                return game;
        }
        return null;
    }
	
	private static void addRivalryGamesAll(SeasonSchedule seasonSchedule, SchoolList allSchools, boolean aggressive) {
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    addRivalryGameTwoSchools(seasonSchedule, s1, rival, aggressive, j);
                }
            }
        }
    }
	
	private static void addRivalryGameTwoSchools(SeasonSchedule seasonSchedule, School school, School rival, boolean aggressive, int rivalRank) {
        //School rival = school.getRivals().get(j);
        if (school.isPossibleOpponent(rival)) {
            if (aggressive && rivalRank < 2) {
                aggressiveAddRivalryGameHelper(seasonSchedule, school, rival);
            }
            //if they don't play and aren't in the same conference
            //go through all the rivals for a team
            else if (rival.getSchedule().size() < 12 && school.getSchedule().size() < 12) {
                //and stop if the seasonSchedule is full
                addRivalryGameHelper(seasonSchedule, school, rival, rivalRank);
            }
        }
    }
	
	private static void addRivalryGameHelper(SeasonSchedule seasonSchedule, School s1, School rival, int rivalRank) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        if (rivalRank < 2) {
            if (emptyWeeks.contains(12)) {
                seasonSchedule.addGame(s1, rival, 12, 5);
                //week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is referenced by 12
            } else if (emptyWeeks.contains(11)) {
                seasonSchedule.addGame(s1, rival, 11, 5);
                //week 12 is empty
            } else if (emptyWeeks.contains(13)) {
                seasonSchedule.addGame(s1, rival, 13, 5);
                //week 14 is empty
            } else if (!emptyWeeks.isEmpty()) {
                seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
                //add game at emptyWeeks.get(0);
            }
        } else if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
        }
    }
	
	private static void aggressiveAddRivalryGameHelper(SeasonSchedule seasonSchedule, School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1weeks, rweeks);
        if (emptyWeeks.contains(12)) {
            seasonSchedule.addGame(s1, rival, 12, 5);
            return;
            //week 13 is empty
        } else if (emptyWeeks.contains(11)) {
            seasonSchedule.addGame(s1, rival, 11, 5);
            return;
            //week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            seasonSchedule.addGame(s1, rival, 13, 5);
            return;
            //week 14 is empty
        }
        if (s1weeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = rival.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (s1weeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (s1weeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = rival.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (rweeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = s1.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (rweeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (rweeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = s1.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getSchedule().getGame(12);
            Game rgame = rival.getSchedule().getGame(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(11) && !rweeks.contains(11)) {
            Game s1game = s1.getSchedule().getGame(11);
            Game rgame = rival.getSchedule().getGame(11);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
            //add game at emptyWeeks.get(0);
        }
    }
	
	private static void addRandomGames(SeasonSchedule seasonSchedule, SchoolList allSchools, SchoolList needGames) {
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
                        //verify Alabama won't play Michigan to end the year. Instead they'll play LA Monroe
                        if (emptyWeeks.get(0) < 11 || (s1.isPowerConf() ^ randomSchool.isPowerConf())) {
                            seasonSchedule.addGame(s1, randomSchool, emptyWeeks.get(0), 5);
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
                } else {//remove random school if it has enough games
                    if (randomNum < i) {
                        i--;
                    }
                    needGames.remove(randomSchool);
                    myOptions.remove(randomSchool);
                }
            }
        }

        if (!needGames.isEmpty()) {
            //add games vs fcs schools
            for (int i = 0; i < needGames.size(); i++) {
                School s1 = needGames.get(i);
                for (int j = 0; j < allSchools.size() && s1.getSchedule().size() < 12; j++) {
                    if (!allSchools.get(j).getDivision().equals("FBS")) {
                        School fcs = allSchools.get(j);
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, fcs);
                        if (!emptyWeeks.isEmpty()) {
                            seasonSchedule.addGame(s1, fcs, emptyWeeks.get(0), 5);
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
    }
	
	public void fixSchedule() {
		removeExtraGames(seasonSchedule, schoolList);
        fillOpenGames(seasonSchedule, schoolList);
	}

	public void saveToFile() {
		try {
			ExcelReader.write(seasonSchedule);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


