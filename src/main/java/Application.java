import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Application {
    private static boolean debug = false;
    private static int year = 2021;
    private static School nullSchool = new School(999, "null", "null", "null", "null", "null");

    private final Logger LOGGER = Logger.getLogger(Schedule.class.getName());

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        commandLineUI();
    }

    private static void commandLineUI() throws IOException {
        final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
        final String scheduleFile = "src/main/resources/SCHED.xlsx";
        SchoolList allSchools = ExcelReader.getSchoolData(schoolsFile);
        Schedule schedule = ExcelReader.getScheduleData(scheduleFile, allSchools);
        System.out.println("Welcome to the NCAA Football 06 Scheduler");
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            System.out.println("_________________________________________");
            System.out.println("1. Automatically add rivalry games (Conservative)");
            System.out.println("2. Automatically add rivalry games (Aggressive)");
            System.out.println("3. Manual Editing");
            System.out.println("4. Options");
            System.out.println("5. Save to Excel Sheet");
            System.out.println("6. Exit");
            String input = scanner.next();
            if (input.equals("1")) {
                addRivalryGamesOption(schedule, allSchools, false);
            } else if (input.equals("2")) {
                addRivalryGamesOption(schedule, allSchools, true);
            } else if (input.equals("3")) {

            } else if (input.equals("4")) {

            } else if (input.equals("5")) {
                ExcelReader.write(schedule);
            } else if (input.equals("6")) {
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }

    }

    private static void addRivalryGamesOption(Schedule schedule, SchoolList schoolList, boolean aggressive) {
        addRivalryGames(schedule, schoolList, aggressive);
        removeExtraGames(schedule, schoolList);
        fillOpenGames(schedule,schoolList);
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

    private static ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }

    private static boolean doSchoolsPlay(School s1, School s2) {
        //boolean play = false;
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            if (s1.getSchedule().get(i).getHomeTeam() != null && s1.getSchedule().get(i).getAwayTeam() != null) {
                if (s1.getSchedule().get(i).getHomeTeam().getTgid() == s2.getTgid() ||
                        s1.getSchedule().get(i).getAwayTeam().getTgid() == s2.getTgid()) {

                    //System.out.println(s1.getName() + " plays " + s2.getName());
                    return true;
                }
            }
        }
        //System.out.println(s1.getName() + " does NOT play " + s2.getName());
        return false;
    }

    private static void addRivalryGames(Schedule schedule, SchoolList allSchools, boolean aggressive) {
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    if (!doSchoolsPlay(s1, rival) && !s1.getConference().equals(rival.getConference())) {
                        if (aggressive && j < 2) {
                            aggressiveAddRivalryGamesHelper(schedule, s1, rival);
                        }
                        //if they don't play and aren't in the same conference
                        //go through all the rivals for a team
                        else if (rival.getSchedule().size() < 12 && s1.getSchedule().size() < 12) {
                            //and stop if the schedule is full
                            addRivalryGamesHelper(schedule, s1, rival);
                        }
                    }
                }
            }
        }
    }

    private static void addRivalryGamesHelper(Schedule schedule, School s1, School rival) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        if (emptyWeeks.contains(12)) {
            schedule.addGame(s1, rival, 12, 5, year);
            //week 12 is empty
        } else if (emptyWeeks.contains(13)) {
            schedule.addGame(s1, rival, 13, 5, year);
            //week 13 is empty
        } else if (!emptyWeeks.isEmpty()) {
            schedule.addGame(s1, rival, emptyWeeks.get(0), 5, year);
            //add game at emptyWeeks.get(0);
        } else if (debug) {
            //System.out.println("No free weeks");
        }
    }

    private static boolean aggressiveAddRivalryGamesHelper(Schedule schedule, School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1weeks, rweeks);
        if (emptyWeeks.contains(12)) {
            schedule.addGame(s1, rival, 12, 5, year);
            return true;
            //week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            schedule.addGame(s1, rival, 13, 5, year);
            return true;
            //week 13 is empty
        }
        if (s1weeks.contains(12)) {
            //if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                schedule.replaceGame(game, s1, rival, year);
                return true;
            }
        }
        if (s1weeks.contains(13)) {
            //if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                schedule.replaceGame(game, s1, rival, year);
                return true;
            }
        }
        if (rweeks.contains(12)) {
            //if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                schedule.replaceGame(game, s1, rival, year);
                return true;
            }
        }
        if (rweeks.contains(13)) {
            //if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                schedule.replaceGame(game, s1, rival, year);
                return true;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getSchedule().getGame(12);
            Game rgame = rival.getSchedule().getGame(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                schedule.removeGame(s1game);
                schedule.replaceGame(rgame, s1, rival, year);
                return true;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                schedule.removeGame(s1game);
                schedule.replaceGame(rgame, s1, rival, year);
                return true;
                //remove both games and replace with this one...
            }
        }
        if (doSchoolsPlay(s1, nullSchool)) {
            for (int i = 0; i < s1.getSchedule().size(); i++) {
                Game s1game = s1.getSchedule().get(i);
                if (nullSchool.getTgid() == s1game.getAwayTeam().getTgid() || nullSchool.getTgid() == s1game.getHomeTeam().getTgid()) {
                    if (rweeks.contains(s1game.getWeek())) {
                        schedule.replaceGame(s1game, s1, rival, year);
                        return true;
                    } else {
                        Game rgame = rival.getSchedule().getGame(s1game.getWeek());
                        if (rgame.isRemovableGame()) {
                            schedule.removeGame(s1game);
                            schedule.replaceGame(rgame, s1, rival, year);
                            return true;
                        }
                    }
                }
            }
        }
        if (doSchoolsPlay(rival, nullSchool)) {
            for (int i = 0; i < rival.getSchedule().size(); i++) {
                Game rgame = rival.getSchedule().get(i);
                if (nullSchool.getTgid() == rgame.getAwayTeam().getTgid() || nullSchool.getTgid() == rgame.getHomeTeam().getTgid()) {
                    if (s1weeks.contains(rgame.getWeek())) {
                        schedule.replaceGame(rgame, s1, rival, year);
                        return true;
                    } else {
                        Game s1game = s1.getSchedule().getGame(rgame.getWeek());
                        if (s1game.isRemovableGame()) {
                            schedule.removeGame(rgame);
                            schedule.replaceGame(s1game, s1, rival, year);
                            return true;
                        }
                    }
                }
            }
        }
        if (!emptyWeeks.isEmpty()) {
            schedule.addGame(s1, rival, emptyWeeks.get(0), 5, year);
            return true;
            //add game at emptyWeeks.get(0);
        }
        return false;
        //add to week 12 or 13
        //addGame(schedule, s1, rival, 800, emptyWeeks.get(0), 5, 0, 0);
        //add game at emptyWeeks.get(0);
    }

    private static void removeExtraGames(Schedule schedule, SchoolList schoolList) {
        SchoolList tooManyGames = schoolList.findTooManyGames();
        for (int i = 0; i < tooManyGames.size(); i++) {
            School school = tooManyGames.get(i);
            while (school.getSchedule().size() > 12) {
                Game removeMe = school.findRemovableGame();
                if(removeMe!=null) {
                    schedule.removeGame(removeMe);
                } else{
                    //remove extra rivalry games
                    for (int j = school.getRivals().size()-1; school.getSchedule().size() > 12; j--) {
                        School rival = school.getRivals().get(j);
                        if (doSchoolsPlay(school, rival)){
                            removeMe = findGame(school, rival);
                            if (removeMe.getConferenceGame() == 0) {
                                schedule.removeGame(removeMe);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void fillOpenGames(Schedule schedule, SchoolList schoolList){
        SchoolList tooFewGames = schoolList.findTooFewGames();
        addRivalryGames(schedule, tooFewGames, false);
        addRandomGames(schedule, schoolList, tooFewGames);
    }



    private static void addRandomGames(Schedule schedule, SchoolList schoolList, SchoolList tooFewGames) {
        for (int i = 0; i < tooFewGames.size(); i++) {
            School s1 = tooFewGames.get(i);
            if (s1.getSchedule().size() < 12) {
                for (int j = 0; j < tooFewGames.size() && s1.getSchedule().size() < 12; j++) {
                    School s2 = tooFewGames.get(j);
                    if (s1.getTgid() != s2.getTgid() && !s1.getConference().equals(s2.getConference()) && s2.getSchedule().size()<12) {
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, s2);
                        if (!emptyWeeks.isEmpty()) {
                            schedule.addGame(s1, s2, emptyWeeks.get(0), 5, year);
                        }
                    }
                }
            }
        }
        for (int i = tooFewGames.size()-1; i >= 0; i--) {
            if (tooFewGames.get(i).getSchedule().size()==12){
                tooFewGames.remove(i);
            }
        }

        if (!tooFewGames.isEmpty()){
            //add games vs fcs schools
            for (int i = 0; i < tooFewGames.size(); i++) {
                School s1 = tooFewGames.get(i);
                for (int j = 0; j < schoolList.size() && s1.getSchedule().size()<12; j++) {
                    if (!schoolList.get(j).getDivision().equals("FBS")){
                        School fcs = schoolList.get(j);
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, fcs);
                        if (!emptyWeeks.isEmpty()){
                            schedule.addGame(s1, fcs, emptyWeeks.get(0), 5, year);
                        }
                    }
                }
            }
        }
        for (int i = tooFewGames.size()-1; i >= 0; i--) {
            if (tooFewGames.get(i).getSchedule().size()==12){
                tooFewGames.remove(i);
            }
        }
    }

    private static Game findGame(School s1, School s2){
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game game = s1.getSchedule().get(i);
            if (game.getHomeTeam().getTgid() == s2.getTgid() ||
                    game.getAwayTeam().getTgid() == s2.getTgid())
            return game;
        }
        return null;
    }
}
