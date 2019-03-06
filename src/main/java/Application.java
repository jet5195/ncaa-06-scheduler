import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class Application {
    private static boolean debug = false;
    private static int year = 2021;
    private static final School nullSchool = new School(999, "null", "null", "null", "null", "null");

    private final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        commandLineUI();
    }

    private static void commandLineUI() throws IOException {
        final String schoolsFile = "src/main/resources/My_Custom_Conferences.xlsx";
        final String scheduleFile = "src/main/resources/SCHED.xlsx";
        SchoolList schoolList = ExcelReader.getSchoolData(schoolsFile);
        SeasonSchedule seasonSchedule = ExcelReader.getScheduleData(scheduleFile, schoolList);
        System.out.println("Welcome to the NCAA Football PS2 Scheduler");
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Home");
            System.out.println("1. Automatically add rivalry games (Conservative)");
            System.out.println("2. Automatically add rivalry games (Aggressive)");
            System.out.println("3. Remove Non-Conference Games");
            System.out.println("4. Manual Schedule Editing");
            System.out.println("5. Options");
            System.out.println("6. Save to Excel Sheet");
            System.out.println("7. Exit");
            String input = scanner.next();
            if (input.equals("1")) {
                addRivalryGamesOption(seasonSchedule, schoolList, false);
            } else if (input.equals("2")) {
                addRivalryGamesOption(seasonSchedule, schoolList, true);
            } else if (input.equals("3")){
                removeNonConferenceGamesUI(seasonSchedule);
            } else if (input.equals("4")) {
                manualOptionUI(seasonSchedule, schoolList);
            } else if (input.equals("5")) {
                optionsOptionUI();
            } else if (input.equals("6")) {
                ExcelReader.write(seasonSchedule);
            } else if (input.equals("7")) {
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private static void removeNonConferenceGamesUI(SeasonSchedule seasonSchedule){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Remove Non-Conference Games");
            System.out.println("1. Remove All FCS Games");
            System.out.println("2. Remove All Non-Conference Games (Keep Rivals)");
            System.out.println("3. Remove All Non-Conference Games");
            System.out.println("4. Back");
            String input = scanner.next();
            if (input.equals("1")) {
                seasonSchedule.removeAllFcsGames();
            } else if (input.equals("2")) {
                seasonSchedule.removeAllNonConferenceGames(false);
            } else if (input.equals("3")) {
                seasonSchedule.removeAllNonConferenceGames(true);
            } else if (input.equals("4")) {
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private static void manualOptionUI(SeasonSchedule seasonSchedule, SchoolList schoolList) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Manual Schedule Editing");
            System.out.println("1. Select School");
            System.out.println("2. Options");
            System.out.println("3. Save to Excel Sheet");
            System.out.println("4. Back");
            String input = scanner.next();
            if (input.equals("1")) {
                School school = schoolSelectUI(schoolList);
                if (school != null) {
                    modifyScheduleUI(seasonSchedule, schoolList, school);
                }
            } else if (input.equals("2")) {
                optionsOptionUI();
            } else if (input.equals("3")) {
                ExcelReader.write(seasonSchedule);
            } else if (input.equals("4")) {
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }
    }

    private static void optionsOptionUI(){
        System.out.println("This option is still under development...");
    }

    private static School searchByConferenceUI(SchoolList schoolList){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        ArrayList<String> conferences = schoolList.getConferences();
        while(!exit) {
            printTitle("Enter number of desired Conference");
            int i;
            for (i = 0; i < conferences.size(); i++) {
                System.out.println(i+1 + ". " + conferences.get(i));
            }
            System.out.println(i+1 + ". Back");
            int input = scanner.nextInt();
            if (input>0&&input<i+1) {
                School school = selectSchoolByNumUI(schoolList.conferenceSearch(conferences.get(input-1)));
                if (school!=null){
                    return school;
                }
            } else if(input == i+1){
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }
        return null;
    }

    private static School searchByNameUI(SchoolList schoolList){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Enter School name");
            System.out.println("1. Back");
            String input = scanner.nextLine();
            School result = schoolList.schoolSearch(input);
            if (input.equals("1")) {
                exit = true;
            } else if (result!=null){
                return result;
            } else {
                System.out.println("Try again, " + input + " was not found.");
            }
        }
        return null;
    }

    private static School searchByTgidUI(SchoolList schoolList){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        School school;
        while(!exit) {
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
                } catch (NumberFormatException error){
                    System.out.println("You must either enter an Integer or 'B' to go back.");
                }
            }
            if (modifiedTgid){
                school = schoolList.schoolSearch(tgid);
                if (school != null){
                    return school;
                } else {
                    System.out.println("Invalid TGID, please try again.");
                }
            }
        }
        return null;
    }

    private static School schoolSelectUI(SchoolList schoolList){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        School school = nullSchool;
        while(!exit) {
            printTitle("Select School");
            System.out.println("1. Search by Conference");
            System.out.println("2. Search by School Name");
            System.out.println("3. Search by TGID");
            System.out.println("4. Back");
            String input = scanner.next();
            if (input.equals("1")) {
                school = searchByConferenceUI(schoolList);
            } else if (input.equals("2")) {
                school = searchByNameUI(schoolList);
            } else if (input.equals("3")) {
                school = searchByTgidUI(schoolList);
            } else if (input.equals("4")) {
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
            if (school!= null){
                return school;
            }
        }
        return null;
    }

    private static void modifyScheduleUI(SeasonSchedule seasonSchedule, SchoolList schoolList, School school){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle(school.getName() + " " + school.getNickname());
            System.out.println("1. View Schedule");
            System.out.println("2. Add Game");
            System.out.println("3. Remove Game");
            System.out.println("4. Back");
            String input = scanner.next();
            if (input.equals("1")) {
                school.printSchedule();
            } else if (input.equals("2")) {
                addGameOptionUI(seasonSchedule, schoolList, school);
            } else if (input.equals("3")) {
                removeGameUI(seasonSchedule, school);
            } else {
                exit = true;
            }
        }
    }

    private static void addGameOptionUI(SeasonSchedule seasonSchedule, SchoolList schoolList, School school){
        if(school.getSchedule().size()<12) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                printTitle("Manually Add Games");
                System.out.println("1. Automatically add rivalry games (Conservative)");
                System.out.println("2. Automatically add rivalry games (Aggressive)");
                System.out.println("3. Add rivalry games manually");
                System.out.println("4. Add other games manually");
                System.out.println("5. Back");
                String input = scanner.next();
                if (input.equals("1")) {
                    addRivalryGamesSchool(seasonSchedule, school, false);
                } else if (input.equals("2")) {
                    addRivalryGamesSchool(seasonSchedule, school, true);
                } else if (input.equals("3")) {
                    addRivalryGamesManuallyUI();
                } else if (input.equals("4")) {
                    addGameTwoSchoolsUI(seasonSchedule, school, schoolSelectUI(schoolList));
                } else    {
                    exit = true;
                }
            }
        } else {
            System.out.println(school + " is already at the maximum scheduled games. Try removing games before adding any.");
        }
    }

    private static void removeGameUI(SeasonSchedule seasonSchedule, School school){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Remove Game");
            System.out.println("0. Back");
            school.printSchedule();
            String input = scanner.next();
            if (input.equals("0")) {
               exit = true;
            } else {
                try {
                    int selection = Integer.parseInt(input);
                    if (selection > 0 && selection < school.getSchedule().size()){
                        SeasonSchedule newSchedule = new SeasonSchedule();
                        school.getSchedule().addAll(newSchedule);
                        Collections.sort(newSchedule);
                        seasonSchedule.removeGame(school.getSchedule().getGame(newSchedule.get(selection-1).getWeek()));
                    } else {
                        System.out.println("Please enter a valid option.");
                    }
                } catch (NumberFormatException error){
                    System.out.println("Please enter a valid option.");
                }
            }
        }
    }

    private static void addRivalryGamesManuallyUI(){

    }

    private static void addGameTwoSchoolsUI(SeasonSchedule seasonSchedule, School s1, School s2){
        boolean doSchoolsPlay = doSchoolsPlay(s1, s2);
        if (!doSchoolsPlay && !s1.getConference().equals(s2.getConference())) {
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
                    int input = scanner.nextInt();
                    if (input == 0) {
                        exit = true;
                    } else {
                        int week = emptyWeeks.get(input - 1);
                        seasonSchedule.addGame(s1, s2, week, 5, year);
                        exit = true;
                    }
                } else {
                    System.out.println("Sorry, there are no free weeks between these two teams, try removing a game first.");
                    exit = true;
                }
                //School opponent = schoolSelectUI(seasonSchedule, schoolList);
            }
        } else {
            if (doSchoolsPlay) {
                System.out.println("Cannot add a game between schools that already play each other.");
            } else {
                System.out.println("Cannot add a game between schools in the same conference.");
            }
        }
    }

    private static School selectSchoolByNumUI(SchoolList schoolList){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            printTitle("Enter number of desired school");
            int i = 0;
            for (i = 0; i < schoolList.size(); i++) {
                System.out.println(i+1 + ". " + schoolList.get(i));
            }

            System.out.println(i+1 + ". Back");
            int input = scanner.nextInt();
            if (input>0&&input<i+1) {
                return schoolList.get(input-1);
            } else if(input == i+1){
                exit = true;
            } else {
                System.out.println("Please choose an option.");
            }
        }
        return null;
    }

    private static void addRivalryGamesOption(SeasonSchedule seasonSchedule, SchoolList schoolList, boolean aggressive) {
        addRivalryGamesAll(seasonSchedule, schoolList, aggressive);
        removeExtraGames(seasonSchedule, schoolList);
        fillOpenGames(seasonSchedule,schoolList);
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
        for (int i = 0; i < s1.getSchedule().size(); i++) {
            if (s1.getSchedule().get(i).getHomeTeam() != null && s1.getSchedule().get(i).getAwayTeam() != null) {
                if (s1.getSchedule().get(i).getHomeTeam().getTgid() == s2.getTgid() ||
                        s1.getSchedule().get(i).getAwayTeam().getTgid() == s2.getTgid()) {
                    return true;
                }
            }
        }
        return false;
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

    private static void addRivalryGamesSchool(SeasonSchedule seasonSchedule, School school, boolean aggressive){
        for (int j = 0; j < school.getRivals().size(); j++){
            School rival = school.getRivals().get(j);
            addRivalryGameTwoSchools(seasonSchedule, school, rival, aggressive, j);
        }
    }

    private static void addRivalryGameTwoSchools(SeasonSchedule seasonSchedule, School school, School rival, boolean aggressive, int rivalRank){
        //School rival = school.getRivals().get(j);
        if (!doSchoolsPlay(school, rival) && !school.getConference().equals(rival.getConference())) {
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
                seasonSchedule.addGame(s1, rival, 12, 5, year);
                //week 13 is empty, keep in mind week 1 is referenced by a 0, therefore 13 is referenced by 12
            } else if (emptyWeeks.contains(11)) {
                seasonSchedule.addGame(s1, rival, 11, 5, year);
                //week 12 is empty
            } else if (emptyWeeks.contains(13)) {
                seasonSchedule.addGame(s1, rival, 13, 5, year);
                //week 14 is empty
            } else if (!emptyWeeks.isEmpty()) {
                seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5, year);
                //add game at emptyWeeks.get(0);
            }
        } else if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5, year);
        }
    }

    private static void aggressiveAddRivalryGameHelper(SeasonSchedule seasonSchedule, School s1, School rival) {
        ArrayList<Integer> s1weeks = findEmptyWeeks(s1);
        ArrayList<Integer> rweeks = findEmptyWeeks(rival);
        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1weeks, rweeks);
        if (emptyWeeks.contains(12)) {
            seasonSchedule.addGame(s1, rival, 12, 5, year);
            return;
            //week 13 is empty
        }
         else if (emptyWeeks.contains(11)) {
            seasonSchedule.addGame(s1, rival, 11, 5, year);
            return;
            //week 12 is empty
        }
        if (emptyWeeks.contains(13)) {
            seasonSchedule.addGame(s1, rival, 13, 5, year);
            return;
            //week 14 is empty
        }
        if (s1weeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = rival.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (s1weeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = rival.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (s1weeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = rival.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (rweeks.contains(12)) {
            //if the first team has an opening in week 13...
            Game game = s1.getSchedule().getGame(12);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (rweeks.contains(11)) {
            //if the first team has an opening in week 12...
            Game game = s1.getSchedule().getGame(11);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (rweeks.contains(13)) {
            //if the first team has an opening in week 14...
            Game game = s1.getSchedule().getGame(13);
            //set game to variable
            if (game.isRemovableGame()) {
                //if the game that is blocking a game being added isn't required..
                seasonSchedule.replaceGame(game, s1, rival, year);
                return;
            }
        }
        if (!s1weeks.contains(12) && !rweeks.contains(12)) {
            Game s1game = s1.getSchedule().getGame(12);
            Game rgame = rival.getSchedule().getGame(12);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival, year);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(11) && !rweeks.contains(11)) {
            Game s1game = s1.getSchedule().getGame(11);
            Game rgame = rival.getSchedule().getGame(11);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival, year);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!s1weeks.contains(13) && !rweeks.contains(13)) {
            Game s1game = s1.getSchedule().getGame(13);
            Game rgame = rival.getSchedule().getGame(13);
            if (s1game.isRemovableGame() && rgame.isRemovableGame()) {
                seasonSchedule.removeGame(s1game);
                seasonSchedule.replaceGame(rgame, s1, rival, year);
                return;
                //remove both games and replace with this one...
            }
        }
        if (!emptyWeeks.isEmpty()) {
            seasonSchedule.addGame(s1, rival, emptyWeeks.get(0), 5, year);
            return;
            //add game at emptyWeeks.get(0);
        }
    }

    private static void removeExtraGames(SeasonSchedule seasonSchedule, SchoolList schoolList) {
        SchoolList tooManyGames = schoolList.findTooManyGames();
        for (int i = 0; i < tooManyGames.size(); i++) {
            School school = tooManyGames.get(i);
            while (school.getSchedule().size() > 12) {
                Game removeMe = school.findRemovableGame();
                if(removeMe!=null) {
                    seasonSchedule.removeGame(removeMe);
                } else{
                    //remove extra rivalry games
                    for (int j = school.getRivals().size()-1; school.getSchedule().size() > 12; j--) {
                        School rival = school.getRivals().get(j);
                        if (doSchoolsPlay(school, rival)){
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

    private static void fillOpenGames(SeasonSchedule seasonSchedule, SchoolList schoolList){
        SchoolList tooFewGames = schoolList.findTooFewGames();
        addRivalryGamesAll(seasonSchedule, tooFewGames, false);
        addRandomGames(seasonSchedule, schoolList, tooFewGames);
    }



    private static void addRandomGames(SeasonSchedule seasonSchedule, SchoolList schoolList, SchoolList tooFewGames) {
        for (int i = 0; i < tooFewGames.size(); i++) {
            School s1 = tooFewGames.get(i);
            if (s1.getSchedule().size() < 12) {
                for (int j = 0; j < tooFewGames.size() && s1.getSchedule().size() < 12; j++) {
                    School s2 = tooFewGames.get(j);
                    if (s1.getTgid() != s2.getTgid() && !s1.getConference().equals(s2.getConference()) && s2.getSchedule().size()<12) {
                        ArrayList<Integer> emptyWeeks = findEmptyWeeks(s1, s2);
                        if (!emptyWeeks.isEmpty()) {
                            seasonSchedule.addGame(s1, s2, emptyWeeks.get(0), 5, year);
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
                            seasonSchedule.addGame(s1, fcs, emptyWeeks.get(0), 5, year);
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

    private static void printTitle(String text){
        System.out.println("_________________________________________");
        System.out.println(text);
        System.out.println("_________________________________________");
    }
}
