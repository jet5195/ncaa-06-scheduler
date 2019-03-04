import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Application {
    static boolean debug = false;
    static int year = 2021;
    static School nullSchool = new School(999, "null", "null", "null", "null", "null");

    public static void main(String[] args) throws IOException {
        commandLineUI();
    }

    public static void commandLineUI() throws IOException {
        final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
        final String scheduleFile = "src/main/resources/SCHED.xlsx";
        SchoolList allSchools = ExcelReader.getSchoolData(schoolsFile);
        Schedule theSchedule = ExcelReader.getScheduleData(scheduleFile, allSchools);
        System.out.println("Welcome to the NCAA Football 06 Scheduler");
        System.out.println("Would you like to automatically add rivalry games?");
        System.out.println("(Y/N)");
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            String input = scanner.next();
            if (input.equalsIgnoreCase("Y")) {
                addRivalryGamesOption(theSchedule, allSchools);
                exit = true;
            } else if (input.equalsIgnoreCase("N")) {
                exit = true;
            } else {
                System.out.println(input + " is not a valid response. Enter 'Y' or 'N");
            }
        }

    }

    public static void addRivalryGamesOption(Schedule schedule, SchoolList schoolList) throws IOException {
        aggressiveAddRivalryGames(schedule, schoolList);
        verifyFewerThan13Games(schedule, schoolList);
        verifyMoreThan11Games(schedule, schoolList);
        //write to excel
        ExcelReader.write(schedule);
        System.out.println("done");

    }

    public static void ui(){

    }

    public static ArrayList<Integer> findEmptyWeeks(School s1, School s2) {//returns list of empty weeks between 2 schools
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> s2weeks = findEmptyWeeks(s2);

        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
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

    public static ArrayList<Integer> findEmptyWeeks(ArrayList<Integer> s1weeks, ArrayList<Integer> s2weeks) {
        ArrayList<Integer> freeWeeks = new ArrayList<Integer>();
        for (int i = 0; i < s1weeks.size(); i++) {
            if (s2weeks.contains(s1weeks.get(i))) {
                freeWeeks.add(s1weeks.get(i));
            }
        }
        return freeWeeks;
    }

    public static boolean doSchoolsPlay(School s1, School s2) {
        //boolean play = false;
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            if (s1.getSchedule().get(i).getHomeTeam() != null && s1.getSchedule().get(i).getAwayTeam() != null) {
                if (s1.getSchedule().get(i).getHomeTeam().getTgid() == s2.getTgid() ||
                        s1.getSchedule().get(i).getAwayTeam().getTgid() == s2.getTgid()) {
                    if (debug) {
                        //System.out.println(s1.getName() + " plays " + s2.getName());
                    }
                    return true;
                }
            }
        }
        //System.out.println(s1.getName() + " does NOT play " + s2.getName());
        return false;
    }

    public static void addRivalryGames(Schedule theSchedule, SchoolList allSchools) {
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    if (!doSchoolsPlay(s1, rival) && !s1.getConference().equals(rival.getConference())) {
                        //if they don't play and aren't in the same conference
                        //go through all the rivals for a team
                        if (rival.getSchedule().size() < 12 && s1.getSchedule().size() < 12) {
                            //and stop if the schedule is full
                            addRivalryGamesHelper(theSchedule, s1, rival);
                        }
                    }
                }
            }
        }
    }

    public static void aggressiveAddRivalryGames(Schedule theSchedule, SchoolList allSchools) {
        for (int j = 0; j <= 8; j++) {
            for (int i = 0; i < allSchools.size(); i++) {
                //go through all the schools
                School s1 = allSchools.get(i);
                if (s1.getDivision().equals("FBS") && j < s1.getRivals().size()) {
                    School rival = s1.getRivals().get(j);
                    if (!doSchoolsPlay(s1, rival) && !s1.getConference().equals(rival.getConference())) {
                        if (j < 2) {
                            aggressiveAddRivalryGamesHelper(theSchedule, s1, rival);
                        }
                        //if they don't play and aren't in the same conference
                        //go through all the rivals for a team
                        else if (rival.getSchedule().size() < 12 && s1.getSchedule().size() < 12) {
                            //and stop if the schedule is full
                            addRivalryGamesHelper(theSchedule, s1, rival);
                        }
                    }
                }
            }
        }
    }

    public static void addRivalryGamesHelper(Schedule theSchedule, School s1, School rival) {
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, rival);
        if (emptyWeeks.contains(12)) {
            addGame(theSchedule, s1, rival, 12, 5);
            //week 12 is empty
        } else if (emptyWeeks.contains(13)) {
            addGame(theSchedule, s1, rival, 13, 5);
            //week 13 is empty
        } else if (!emptyWeeks.isEmpty()) {
            addGame(theSchedule, s1, rival, emptyWeeks.get(0), 5);
            //add game at emptyWeeks.get(0);
        } else if (debug) {
            //System.out.println("No free weeks");
        }
    }

    public static boolean aggressiveAddRivalryGamesHelper(Schedule theSchedule, School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1weeks, rweeks);
        if (emptyWeeks.contains(12)) {
            addGame(theSchedule, s1, rival, 12, 5);
            return true;
            //week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            addGame(theSchedule, s1, rival, 13, 5);
            return true;
            //week 13 is empty
        }
        if (s1weeks.contains(12)) {
            //if the first team has an opening in week 12...
            Game theGame = rival.getSchedule().getGame(12);
            //set game to variable
            if (theGame.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                replaceGame(theSchedule, theGame, s1, rival);
                return true;
            }
        }
        if (s1weeks.contains(13)) {
            //if the first team has an opening in week 12...
            Game theGame = rival.getSchedule().getGame(13);
            //set game to variable
            if (theGame.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                replaceGame(theSchedule, theGame, s1, rival);
                return true;
            }
        }
        if (rweeks.contains(12)) {
            //if the first team has an opening in week 12...
            Game theGame = s1.getSchedule().getGame(12);
            //set game to variable
            if (theGame.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                replaceGame(theSchedule, theGame, s1, rival);
                return true;
            }
        }
        if (rweeks.contains(13)) {
            //if the first team has an opening in week 12...
            Game theGame = s1.getSchedule().getGame(13);
            //set game to variable
            if (theGame.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                replaceGame(theSchedule, theGame, s1, rival);
                return true;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getSchedule().getGame(12);
            Game rgame = rival.getSchedule().getGame(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                removeGame(theSchedule, s1game);
                replaceGame(theSchedule, rgame, s1, rival);
                return true;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                removeGame(theSchedule, s1game);
                replaceGame(theSchedule, rgame, s1, rival);
                return true;
                //remove both games and replace with this one...
            }
        }
        if (doSchoolsPlay(s1, nullSchool)) {
            for (int i = 0; i < s1.getSchedule().size(); i++) {
                Game s1game = s1.getSchedule().get(i);
                if (nullSchool.getTgid() == s1game.getAwayTeam().getTgid() || nullSchool.getTgid() == s1game.getHomeTeam().getTgid()) {
                    if (rweeks.contains(s1game.getWeek())) {
                        replaceGame(theSchedule, s1game, s1, rival);
                        return true;
                    } else {
                        Game rgame = rival.getSchedule().getGame(s1game.getWeek());
                        if (rgame.isRemovableGame()) {
                            removeGame(theSchedule, s1game);
                            replaceGame(theSchedule, rgame, s1, rival);
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
                        replaceGame(theSchedule, rgame, s1, rival);
                        return true;
                    } else {
                        Game s1game = s1.getSchedule().getGame(rgame.getWeek());
                        if (s1game.isRemovableGame()) {
                            removeGame(theSchedule, rgame);
                            replaceGame(theSchedule, s1game, s1, rival);
                            return true;
                        }
                    }
                }
            }
        }
        if (!emptyWeeks.isEmpty()) {
            addGame(theSchedule, s1, rival, emptyWeeks.get(0), 5);
            return true;
            //add game at emptyWeeks.get(0);
        }

        if (debug) {
            //System.out.println("No free weeks");
        }
        return false;
        //add to week 12 or 13
        //addGame(theSchedule, s1, rival, 800, emptyWeeks.get(0), 5, 0, 0);
        //add game at emptyWeeks.get(0);


        //add check for number of games for both teams
    }

    public static void addGame(Schedule theSchedule, School s1, School s2, int week, int day) {
        int gameNumber = findGameNumber(theSchedule, week);
        //This wonky if statement is so some yearly rivalry games switch off betweeen home and away
        Game newGame = year % 2 == 0 ? new Game(s1, s2, gameNumber, week, day) : new Game(s2, s1, gameNumber, week, day);

        s1.addGame(newGame);
        s2.addGame(newGame);
        theSchedule.add(newGame);//decide if this is actually how you want to add games.. if doing it like this I will just have to go through and remake the schedule in the end. Best solution is to make a addGame method.. NO make schedule its own new object that extends a list and change the add method.. or add to it
        if (debug) {
            System.out.println("Adding game between " + s1.getName() + " and " + s2.getName());
        }
    }

    //only used in replaceGame method
    public static void addGame(Schedule theSchedule, School s1, School s2, int week, int day, int gameNumber) {
        Game newGame = year % 2 == 0 ? new Game(s1, s2, gameNumber, week, day) : new Game(s2, s1, gameNumber, week, day);

        s1.addGame(newGame);
        s2.addGame(newGame);
        theSchedule.add(newGame);//decide if this is actually how you want to add games.. if doing it like this I will just have to go through and remake the schedule in the end. Best solution is to make a addGame method.. NO make schedule its own new object that extends a list and change the add method.. or add to it
        if (debug) {
            System.out.println("Adding game between " + s1.getName() + " and " + s2.getName());
        }
    }

    public static void removeGame(Schedule theSchedule, Game theGame) {
        //code to change the game numbers for all games afterwards in this week
        School s1 = theGame.getHomeTeam();
        School s2 = theGame.getAwayTeam();
        int gameNumber = theGame.getGameNumber();
        int weekNumber = theGame.getWeek();
        theSchedule.remove(theGame);
        s1.getSchedule().remove(theGame);
        s2.getSchedule().remove(theGame);
        updateGameNumbers(theSchedule, gameNumber, weekNumber);
        if (debug) {
            System.out.println("Removing game between " + s1 + " and " + s2);
        }
    }

    public static void replaceGame(Schedule theSchedule, Game theGame, School s1, School s2) {
        int gameNumber = theGame.getGameNumber();
        int weekNumber = theGame.getWeek();
        theSchedule.remove(theGame);
        if (debug) {
            System.out.println("Removing & replacing game between " + theGame.getAwayTeam() + " and " + theGame.getHomeTeam());
        }
        theGame.getHomeTeam().getSchedule().remove(theGame);
        theGame.getAwayTeam().getSchedule().remove(theGame);
        addGame(theSchedule, s1, s2, weekNumber, 5, gameNumber);
    }

    public static int findGameNumber(Schedule theSchedule, int week) {
        int gameNumber = 0;
        for (int i = 0; i < theSchedule.size(); i++) {
            Game theGame = theSchedule.get(i);
            gameNumber = theGame.getWeek() == week && gameNumber < theGame.getGameNumber() ? theGame.getGameNumber() : gameNumber;
        }
        return ++gameNumber;
    }

    public static void updateGameNumbers(Schedule theSchedule, int gameNumber, int weekNumber) {
        for (int i = 0; i < theSchedule.size(); i++) {
            if (theSchedule.get(i).getWeek() == weekNumber && theSchedule.get(i).getGameNumber() > gameNumber) {
                theSchedule.get(i).setGameNumber(theSchedule.get(i).getGameNumber() - 1);
            }
        }
        //does this only update the games in theSchedule? it shouldnt
    }

    public static boolean isRivalryGame(School s1, School s2) {
        if (s1.getRivals() != null && s2.getRivals() != null) {
            for (int i = 0; i < s1.getRivals().size(); i++) {
                if (s1.getRivals().get(i).getName().equals(s2.getName())) {
                    return true;
                }
            }
            for (int i = 0; i < s2.getRivals().size(); i++) {
                if (s2.getRivals().get(i).getName().equals(s1.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    //returns game that is non-conference and not between rivals
    public static Game findRemovableGame(School s1) {
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game theGame = s1.getSchedule().get(i);
            //0 means non-con
            if (theGame.isRemovableGame()) {
                return theGame;
            }
        }
        return null;
    }

    public static void verifyFewerThan13Games(Schedule theSchedule, SchoolList schoolList) {
        for (int i = 0; i < schoolList.size(); i++) {
            if (schoolList.get(i).getDivision().equals("FBS")) {
                verifyFewerThan13Games(theSchedule, schoolList.get(i));
            }
        }
    }

    public static void verifyFewerThan13Games(Schedule theSchedule, School theSchool) {
        while (theSchool.getSchedule().size() > 12) {
            Game removeMe = findRemovableGame(theSchool);
            if(removeMe!=null) {
                removeGame(theSchedule, removeMe);
            } else{
                for (int i = theSchool.getRivals().size()-1; theSchool.getSchedule().size() > 12; i--) {
                    School rival = theSchool.getRivals().get(i);
                    if (doSchoolsPlay(theSchool, rival)){
                        removeMe = findGame(theSchool, rival);
                        if (removeMe.getConferenceGame() == 0) {
                            removeGame(theSchedule, removeMe);
                        }
                    }
                }
            }
        }
    }

    public static void verifyMoreThan11Games(Schedule theSchedule, SchoolList schoolList) {
        SchoolList tooFewGames = new SchoolList();
        for (int i = 0; i < schoolList.size(); i++) {
            School theSchool = schoolList.get(i);
            if (theSchool.getSchedule().size() < 12 && theSchool.getDivision().equals("FBS")) {
                tooFewGames.add(theSchool);
                if (debug) {
                    //System.out.println(theSchool + " has too few games");
                }
            }
        }
        addRivalryGames(theSchedule, tooFewGames);
        addRandomGames(theSchedule, schoolList, tooFewGames);

    }

    public static void addRandomGames(Schedule theSchedule, SchoolList schoolList, SchoolList tooFewGames) {
        for (int i = 0; i < tooFewGames.size(); i++) {
            School s1 = tooFewGames.get(i);
            if (s1.getSchedule().size() < 12) {
                for (int j = 0; j < tooFewGames.size() && s1.getSchedule().size() < 12; j++) {
                    School s2 = tooFewGames.get(j);
                    if (s1.getTgid() != s2.getTgid() && !s1.getConference().equals(s2.getConference()) && s2.getSchedule().size()<12) {
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, s2);
                        if (!emptyWeeks.isEmpty()) {
                            addGame(theSchedule, s1, s2, emptyWeeks.get(0), 5);
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
                            addGame(theSchedule, s1, fcs, emptyWeeks.get(0), 5);
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

    public static Game findGame(School s1, School s2){
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            Game theGame = s1.getSchedule().get(i);
            if (theGame.getHomeTeam().getTgid() == s2.getTgid() ||
                    theGame.getAwayTeam().getTgid() == s2.getTgid())
            return theGame;
        }
        return null;
    }
}
