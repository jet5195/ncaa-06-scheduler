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
		
		//final String schoolsFile = "src/main/resources/School_Data.xlsx";
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
	
	public void setScheduleFile(MultipartFile scheduleFile) throws IOException {
		File file = multipartFileToFile(scheduleFile);
		try {
			seasonSchedule = excelReader.getScheduleData(file, schoolList);
			//is this going to miss conference games since conferences aren't set yet?
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

    public SchoolList getAvailableOpponents(int tgid, int week){
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

	public SchoolList getAvailableRivals(int tgid, int week){
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

	public int removeAllOocNonRivalGames() {
		return seasonSchedule.removeAllNonConferenceGames(false);
	}

	public int removeAllOocGames() {
		return seasonSchedule.removeAllNonConferenceGames(true);
	}

	public int removeAllFcsGames() {
		return seasonSchedule.removeAllFcsGames();
	}

	public void removeGame(int tgid, int week){
		School input = schoolList.schoolSearch(tgid);
		Game game = input.getSchedule().getGame(week);
		seasonSchedule.removeGame(game);
	}

	public void addGame(int awayId, int homeId, int week){
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

	//finds empty weeks for one school
	public ArrayList<Integer> findEmptyWeeks(School s) {
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

	private ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
		ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
		for (int i = 0; i < s1weeks.size(); i++) {
			if (s2weeks.contains(s1weeks.get(i))) {
				freeWeeks.add(s1weeks.get(i));
			}
		}
		return freeWeeks;
	}
	
	private ArrayList<Integer> findEmptyWeeks(School s1, School s2) {//returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
        return findEmptyWeeks(s1weeks, s2weeks);
    }

	public SuggestedGameResponse getSuggestedGame(int tgid) {
		School thisSchool = schoolList.schoolSearch(tgid);
		int homeGameCount = 0;
		boolean isHomeGame = false;
		for (Game game : thisSchool.getSchedule()) {
			if(game.getHomeTeam().getTgid() == tgid) {
				homeGameCount++;
			}	
		}

		if(homeGameCount < 6) {
			isHomeGame = true;
		}
		
		for (School rival : thisSchool.getRivals()) {
			if(thisSchool.isPossibleOpponent(rival)) {
			    //should isPossibleOpponent check this instead?
                if (rival.getSchedule().size()<12) {
                    ArrayList<Integer> emptyWeeks = findEmptyWeeks(thisSchool, rival);
                    if (emptyWeeks.contains(12)) {
                        return new SuggestedGameResponse(12, rival, isHomeGame);
                        //week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is referenced by 12
                    } else if (emptyWeeks.contains(11)) {
                        return new SuggestedGameResponse(11, rival, isHomeGame);
                        //week 12 is empty
                    } else if (emptyWeeks.contains(13)) {
                        return new SuggestedGameResponse(13, rival, isHomeGame);
                        //week 14 is empty
                    } else if (!emptyWeeks.isEmpty()) {
                        return new SuggestedGameResponse(emptyWeeks.get(0), rival, isHomeGame);
                        //add game at emptyWeeks.get(0);
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
        //shouldn't this recalculate tooFewGames?
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
                    //remove extra rivalry games
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
        return count/2;//it's returning 1 removed game as removed for both schools
    }
	
	private Game findGame(School s1, School s2) {
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game game = s1.getSchedule().get(i);
            if (game.getHomeTeam().getTgid() == s2.getTgid() ||
                    game.getAwayTeam().getTgid() == s2.getTgid())
                return game;
        }
        return null;
    }
	
	private int addRivalryGamesAll(SeasonSchedule seasonSchedule, SchoolList allSchools, boolean aggressive) {
        int count = 0;
		for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getNcaaDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    count += addRivalryGameTwoSchools(seasonSchedule, s1, rival, aggressive, j);
                }
            }
        }
		return count;
    }
	
	private int addRivalryGameTwoSchools(SeasonSchedule seasonSchedule, School school, School rival, boolean aggressive, int rivalRank) {
        //School rival = school.getRivals().get(j);
		int count = 0;
        if (school.isPossibleOpponent(rival)) {
            if (aggressive && rivalRank < 2) {
                count += aggressiveAddRivalryGameHelper(seasonSchedule, school, rival);
            }
            //if they don't play and aren't in the same conference
            //go through all the rivals for a team
            else if (rival.getSchedule().size() < 12 && school.getSchedule().size() < 12) {
                //and stop if the seasonSchedule is full
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
                //week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is referenced by 12
            } else if (emptyWeeks.contains(11)) {
                seasonSchedule.addGame(s1, rival, 11, 5);
                return 1;
                //week 12 is empty
            } else if (emptyWeeks.contains(13)) {
                seasonSchedule.addGame(s1, rival, 13, 5);
                return 1;
                //week 14 is empty
            } else if (!emptyWeeks.isEmpty()) {
                seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
                return 1;
                //add game at emptyWeeks.get(0);
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
            //week 13 is empty
        } else if (emptyWeeks.contains(11)) {
            seasonSchedule.addGame(s1, rival, 11, 5);
            return 1;
            //week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            seasonSchedule.addGame(s1, rival, 13, 5);
            return 1;
            //week 14 is empty
        }
        if (s1weeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = rival.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;//or should this be a 0?
            }
        }
        if (s1weeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (s1weeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = rival.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = s1.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival);
                return 1;
            }
        }
        if (rweeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = s1.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
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
                return 1; //should this still return a 1? I guess so. Just counting added games
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(11) && !rweeks.contains(11)) {
            Game s1game = s1.getSchedule().getGame(11);
            Game rgame = rival.getSchedule().getGame(11);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return 1;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival);
                return 1;
                //remove both games and replace with this one...
            }
        }
        if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5);
            return 1;
            //add game at emptyWeeks.get(0);
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
                        //verify Alabama won't play Michigan to end the year. Instead they'll play LA Monroe
                        if (emptyWeeks.get(0) < 11 || (s1.getConference().isPowerConf() ^ randomSchool.getConference().isPowerConf())) {
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
        //count += fillOpenGames(seasonSchedule, schoolList);
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
		if(name.equalsIgnoreCase("All")){
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
}


