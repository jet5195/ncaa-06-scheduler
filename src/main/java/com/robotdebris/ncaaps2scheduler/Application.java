package com.robotdebris.ncaaps2scheduler;
import com.robotdebris.ncaaps2scheduler.model.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class Application {
	
	@Autowired
	private ExcelReader excelReader;
    private static final School nullSchool = new School(999, "null", "null", "null", new Conference("null", false, null, null, null), null, "null", "null", "null", "null");

    private final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        //commandLineUI();
    }

//    private static void commandLineUI() throws IOException {
//        FileChooser fileChooser = new FileChooser();
//        final String schoolsFile = "src/main/resources/School_Data.xlsx";
//        final String conferencesFile = fileChooser.chooseFile("Select Custom Conferences Excel Document");
//        if (conferencesFile == null){
//            System.out.println("No file selected, exiting program.");
//            System.exit(0);
//        }
//        //final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
//        final String scheduleFile =  fileChooser.chooseFile("Select Schedule Excel Document");
//        if (scheduleFile == null){
//            System.out.println("No file selected, exiting program.");
//            System.exit(0);
//        }
//        //final String scheduleFile = "src/main/resources/SCHED.xlsx";
//        SchoolList schoolList = ExcelReader.getSchoolData(schoolsFile);
//        ConferenceList conferenceList = ExcelReader.getConferenceData(conferencesFile);
//        ExcelReader.setAlignmentData(conferencesFile, schoolList, conferenceList);
//        SeasonSchedule seasonSchedule = ExcelReader.getScheduleData(scheduleFile, schoolList);
//        System.out.println("Welcome to the NCAA Football PS2 Scheduler");
//        Scanner scanner = new Scanner(System.in);
//        boolean exit = false;
//        while (!exit) {
//            printTitle("Home");
//            System.out.println("0. Exit");
//            System.out.println("1. Automatically add rivalry games (Conservative)");
//            System.out.println("2. Automatically add rivalry games (Aggressive)");
//            System.out.println("3. Add Games Randomly");
//            System.out.println("4. Remove Non-Conference Games");
//            System.out.println("5. Manual Schedule Editing");
//            System.out.println("6. Validate Schedule");
//            System.out.println("7. Options");
//            System.out.println("8. Save to Excel Sheet");
//            String input = scanner.next();
//            if (input.equals("0")) {
//                exit = true;
//            } else if (input.equals("1")) {
//                addRivalryGamesOption(seasonSchedule, schoolList, false);
//            } else if (input.equals("2")) {
//                addRivalryGamesOption(seasonSchedule, schoolList, true);
//            } else if (input.equals("3")) {
//                addRandomGames(seasonSchedule, schoolList, schoolList.findTooFewGames());
//            } else if (input.equals("4")) {
//                removeNonConferenceGamesUI(seasonSchedule);
//            } else if (input.equals("5")) {
//                manualOptionUI(seasonSchedule, schoolList, conferenceList);
//            } else if (input.equals("6")) {
//                validateSchedule(seasonSchedule, schoolList);
//            } else if (input.equals("7")) {
//                optionsOptionUI(seasonSchedule, schoolList, conferenceList);
//            } else if (input.equals("8")) {
//                ExcelReader.write(seasonSchedule);
//            } else {
//                System.out.println("Please choose an option.");
//            }
//        }
//        System.exit(0);
//    }

    private static void removeNonConferenceGamesUI(SeasonSchedule seasonSchedule) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Remove Non-Conference Games");
            System.out.println("0. Back");
            System.out.println("1. Remove All FCS Games");
            System.out.println("2. Remove All Non-Conference Games (Keep Rivals)");
            System.out.println("3. Remove All Non-Conference Games");
            String input = scanner.next();
            if (input.equals("0")) {
                exit = true;
            } else if (input.equals("1")) {
                seasonSchedule.removeAllFcsGames();
            } else if (input.equals("2")) {
                seasonSchedule.removeAllNonConferenceGames(false);
            } else if (input.equals("3")) {
                seasonSchedule.removeAllNonConferenceGames(true);
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private void manualOptionUI(SeasonSchedule seasonSchedule, SchoolList schoolList, ConferenceList conferenceList) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Manual Schedule Editing");
            System.out.println("0. Back");
            System.out.println("1. Select School");
            System.out.println("2. Options");
            System.out.println("3. Save to Excel Sheet");
            String input = scanner.next();
            if (input.equals("0")) {
                exit = true;
            } else if (input.equals("1")) {
                School school = schoolSelectUI(schoolList, conferenceList);
                if (school != null) {
                    modifyScheduleUI(seasonSchedule, schoolList, conferenceList, school);
                }
            } else if (input.equals("2")) {
                optionsOptionUI(seasonSchedule, schoolList, conferenceList);
            } else if (input.equals("3")) {
                excelReader.write(seasonSchedule);
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private static void optionsOptionUI(SeasonSchedule schedule, SchoolList schoolList, ConferenceList conferenceList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Options");
            System.out.println("0. Back");
            System.out.println("1. Assign Power Conferences");
            int input = scanner.nextInt();
            if (input == 0) {
                exit = true;
            } else if (input == 1) {
                setPowerConferencesUI(schoolList, conferenceList);
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private static School searchByConferenceUI(SchoolList schoolList, ConferenceList conferenceList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Enter number of desired Conference");
            System.out.println("0. Back");
            int i;
            for (i = 0; i < conferenceList.size(); i++) {
                System.out.println(i + 1 + ". " + conferenceList.get(i));
            }
            int input = scanner.nextInt();
            if (input == 0) {
                exit = true;
            } else if (input > 0 && input < i + 1) {
                School school = selectSchoolByNumUI(schoolList.getAllSchoolsInConference(conferenceList.get(input - 1).getName()));
                if (school != null) {
                    return school;
                }
            } else {
                System.out.println("Please choose an option.");
            }
        }
        return null;
    }

    private static School searchByNameUI(SchoolList schoolList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Enter School name");
            System.out.println("0. Back");
            String input = scanner.nextLine();
            School result = schoolList.schoolSearch(input);
            if (input.equals("0")) {
                exit = true;
            } else if (result != null) {
                return result;
            } else {
                System.out.println("Try again, " + input + " was not found.");
            }
        }
        return null;
    }

    private static School searchByTgidUI(SchoolList schoolList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        School school;
        while (!exit) {
            int tgid = 0;//dummy value
            boolean modifiedTgid = false;
            printTitle("Enter School TGID");
            System.out.println("B. Back");
            String input = scanner.next();
            if (input.equalsIgnoreCase("B")) {
                exit = true;
            } else {
                try {
                    tgid = Integer.parseInt(input);
                    modifiedTgid = true;
                } catch (NumberFormatException error) {
                    System.out.println("You must either enter an Integer or 'B' to go back.");
                }
            }
            if (modifiedTgid) {
                school = schoolList.schoolSearch(tgid);
                if (school != null) {
                    return school;
                } else {
                    System.out.println("Invalid TGID, please try again.");
                }
            }
        }
        return null;
    }

    private static School schoolSelectUI(SchoolList schoolList, ConferenceList conferenceList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        School school = nullSchool;
        while (!exit) {
            printTitle("Select School");
            System.out.println("0. Back");
            System.out.println("1. Search by Conference");
            System.out.println("2. Search by School Name");
            System.out.println("3. Search by TGID");
            String input = scanner.next();
            if (input.equals("0")) {
                exit = true;
            } else if (input.equals("1")) {
                school = searchByConferenceUI(schoolList, conferenceList);
            } else if (input.equals("2")) {
                school = searchByNameUI(schoolList);
            } else if (input.equals("3")) {
                school = searchByTgidUI(schoolList);
            } else {
                System.out.println("Please choose an option.");
            }
            if (school != null) {
                return school;
            }
        }
        return null;
    }

    private static void modifyScheduleUI(SeasonSchedule seasonSchedule, SchoolList schoolList, ConferenceList conferenceList, School school) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle(school.getName() + " " + school.getNickname());
            System.out.println("0. Back");
            System.out.println("1. View Schedule");
            System.out.println("2. Add Game");
            System.out.println("3. Remove Game");
            String input = scanner.next();
            if (input.equals("0")) {
                exit = true;
            } else if (input.equals("1")) {
                school.printSchedule();
            } else if (input.equals("2")) {
                addGameOptionUI(seasonSchedule, schoolList, conferenceList, school);
            } else if (input.equals("3")) {
                removeGameUI(seasonSchedule, school);
            } else {
                System.out.println("Please enter a valid option.");
            }
        }
    }

    private static void addGameOptionUI(SeasonSchedule seasonSchedule, SchoolList schoolList, ConferenceList conferenceList, School school) {
        if (school.getSchedule().size() < 12) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                printTitle("Manually Add Games");
                System.out.println("0. Back");
                System.out.println("1. Automatically add rivalry games (Conservative)");
                System.out.println("2. Automatically add rivalry games (Aggressive)");
                System.out.println("3. Add rivalry games manually");
                System.out.println("4. Add other games manually");
                String input = scanner.next();
                if (input.equals("0")) {
                    exit = true;
                } else if (input.equals("1")) {
                    addRivalryGamesSchool(seasonSchedule, school, false);
                } else if (input.equals("2")) {
                    addRivalryGamesSchool(seasonSchedule, school, true);
                } else if (input.equals("3")) {
                    addRivalryGamesManuallyUI(seasonSchedule, school);
                } else if (input.equals("4")) {
                    School opponent = schoolSelectUI(schoolList, conferenceList);
                    if (opponent!= null && !opponent.getName().equals("null")) {
                        addGameTwoSchoolsUI(seasonSchedule, school, opponent);
                    }
                } else {
                    System.out.println("Select a valid option.");
                }
            }
        } else {
            System.out.println(school + " is already at the maximum scheduled games. Try removing games before adding any.");
        }
    }

    private static void removeGameUI(SeasonSchedule seasonSchedule, School school) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Remove Game");
            System.out.println("0. Back");
            school.printSchedule();
            String input = scanner.next();
            if (input.equals("0")) {
                exit = true;
            } else {
                try {
                    int selection = Integer.parseInt(input);
                    if (selection > 0 && selection <= school.getSchedule().size()) {
                        SchoolSchedule newSchedule = new SchoolSchedule();
                        for (int i = 0; i < school.getSchedule().size(); i++) {
                            newSchedule.add(school.getSchedule().get(i));
                        }
                        Collections.sort(newSchedule);
                        Game removeMe = newSchedule.get(selection - 1);
                        if (removeMe.getConferenceGame() != 1) {
                            seasonSchedule.removeGame(school.getSchedule().getGame(newSchedule.get(selection - 1).getWeek()));
                        } else {
                            System.out.println("Please enter a different option. You can not remove Conference games.");
                        }
                    } else {
                        System.out.println("Please enter a valid option.");
                    }
                } catch (NumberFormatException error) {
                    System.out.println("Please enter a valid option.");
                }
            }
        }
    }

    private static void addRivalryGamesManuallyUI(SeasonSchedule seasonSchedule, School school) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Add Game VS Rival");
            System.out.println("0. Back");
            SchoolList rivalList = new SchoolList();
            for (int i = 0; i < school.getRivals().size(); i++) {
                School rival = school.getRivals().get(i);
                if (school.isPossibleOpponent(rival)) {
                    rivalList.add(rival);
                    System.out.println((rivalList.size()) + ". " + rival);
                }
            }
            if (!rivalList.isEmpty()) {
                int input = scanner.nextInt();
                if (input == 0) {
                    exit = true;
                } else if (input > 0 && input <= rivalList.size()) {
                    addGameTwoSchoolsUI(seasonSchedule, school, rivalList.get(input - 1));
                } else {
                    System.out.println("Please enter a valid option.");
                }
            } else {
                System.out.println("Sorry, all available rivalry games are already added.");
                exit = true;
            }
        }
    }

    private static void setPowerConferencesUI(SchoolList schoolList, ConferenceList conferenceList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Choose Conference to switch Power Conference flag");
            System.out.println("0. Back");
            for (int i = 0; i < conferenceList.size(); i++) {
                System.out.print((i + 1) + ". " + conferenceList.get(i));
                boolean exit2 = false;
                for (int j = 0; j < schoolList.size() && !exit2; j++) {
                    if (schoolList.get(j).getConference().equals(conferenceList.get(i))) {
                        exit2 = true;
                        if (schoolList.get(j).getConference().isPowerConf()) {
                            System.out.print("\t Power");
                        }
                        System.out.println();
                    }
                }
            }
            int input = scanner.nextInt();
            if (input == 0) {
                exit = true;
            } else if (input > 0 && input <= conferenceList.size()) {
                String selectedConf = conferenceList.get(input - 1).getName();
                for (int i = 0; i < schoolList.size(); i++) {
                    School school = schoolList.get(i);
                    if (school.getConference().equals(selectedConf)) {
                        if (school.getConference().isPowerConf()) {
                            school.getConference().setPowerConf(false);
                        } else {
                            school.getConference().setPowerConf(true);
                        }
                    }
                }
            } else {
                System.out.println("Select a valid option.");
            }
        }
    }

    private static void addGameTwoSchoolsUI(SeasonSchedule seasonSchedule, School s1, School s2) {
        boolean isOpponent = s1.isOpponent(s2);
        boolean tooManyGames = s2.getSchedule().size() > 11;
        printTitle(s2.getName() + " " + s2.getNickname());
        if (!isOpponent && !s1.isInConference(s2) && !tooManyGames) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                printTitle("Choose an Empty Week");
                ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, s2);
                System.out.println("0. Back");
                if (!emptyWeeks.isEmpty()) {
                    for (int i = 0; i < emptyWeeks.size(); i++) {
                        System.out.println((i + 1) + ". " + (emptyWeeks.get(i) + 1));
                    }
                    int week = scanner.nextInt();
                    if (week == 0) {
                        exit = true;
                    } else {
                        boolean exit2 = false;
                        boolean gameAdded = false;
                        while (!exit2) {
                            printTitle("Choose the Home Team");
                            System.out.println("0. Back");
                            System.out.println("1. " + s1);
                            System.out.println("2. " + s2);
                            System.out.println("3. Random");
                            int input = scanner.nextInt();
                            if (input == 0) {
                                exit2 = true;
                            } else {
                                week = emptyWeeks.get(week - 1);
                                if (input == 1) {
                                    seasonSchedule.addGameSpecificHomeTeam(s2, s1, week, 5);
                                    exit2 = true;
                                    gameAdded = true;
                                } else if (input == 2) {
                                    seasonSchedule.addGameSpecificHomeTeam(s1, s2, week, 5);
                                    exit2 = true;
                                    gameAdded = true;
                                } else if (input == 3) {
                                    seasonSchedule.addGame(s1, s2, week, 5);
                                    exit2 = true;
                                    gameAdded = true;
                                } else {
                                    System.out.println("Please enter a valid option.");
                                }
                            }

                        }
                        if (gameAdded) {
                            exit = true;
                        }
                    }
                } else {
                    System.out.println("Sorry, there are no free weeks between these two teams, try removing a game first.");
                    exit = true;
                }
            }
        } else {
            if (isOpponent) {
                System.out.println("Cannot add a game between schools that already play each other.");
            } else if (tooManyGames) {
                System.out.println("Sorry, " + s2 + " already has the maximum number of scheduled games.");
            } else {
                System.out.println("Cannot add a game between schools in the same conference.");
            }
        }
    }

    private static School selectSchoolByNumUI(SchoolList schoolList) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            printTitle("Enter number of desired school");
            int i = 0;
            System.out.println("0. Back");
            for (i = 0; i < schoolList.size(); i++) {
                System.out.println(i + 1 + ". " + schoolList.get(i));
            }
            int input = scanner.nextInt();
            if (input == 0) {
                exit = true;
            } else if (input > 0 && input < i + 1) {
                return schoolList.get(input - 1);
            } else {
                System.out.println("Please choose an option.");
            }
        }
        return null;
    }

    private static void addRivalryGamesOption(SeasonSchedule seasonSchedule, SchoolList schoolList, boolean aggressive) {
        addRivalryGamesAll(seasonSchedule, schoolList, aggressive);
        removeExtraGames(seasonSchedule, schoolList);
        fillOpenGames(seasonSchedule, schoolList);
    }

    private static ArrayList<Integer> findEmptyWeeks(School s1, School s2) {//returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);
        return findEmptyWeeks(s1weeks, s2weeks);
    }

    //finds empty weeks for one school
    private static ArrayList<Integer> findEmptyWeeks(School s) {
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

    //finds emtpy weeks for 2 schools (I think)
    private static ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }

    private static void addRivalryGamesAll(SeasonSchedule seasonSchedule, SchoolList allSchools, boolean aggressive) {
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getNcaaDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    addRivalryGameTwoSchools(seasonSchedule, s1, rival, aggressive, j);
                }
            }
        }
    }

    private static void addRivalryGamesSchool(SeasonSchedule seasonSchedule, School school, boolean aggressive) {
        for (int j = 0; j < school.getRivals().size(); j++) {
            School rival = school.getRivals().get(j);
            addRivalryGameTwoSchools(seasonSchedule, school, rival, aggressive, j);
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

    private static void validateSchedule(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        removeExtraGames(seasonSchedule, schoolList);
        fillOpenGames(seasonSchedule, schoolList);
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

    private static void fillOpenGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        SchoolList tooFewGames = schoolList.findTooFewGames();
        addRivalryGamesAll(seasonSchedule, tooFewGames, false);
        addRandomGames(seasonSchedule, schoolList, tooFewGames);
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
                        if (emptyWeeks.get(0) < 11 || (s1.getConference().isPowerConf() ^ randomSchool.getConference().isPowerConf())) {
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
                    if (!allSchools.get(j).getNcaaDivision().equals("FBS")) {
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

    private static void printTitle(String text) {
        System.out.println("_________________________________________");
        System.out.println(text);
        System.out.println("_________________________________________");
    }
}
