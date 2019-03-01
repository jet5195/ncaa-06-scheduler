import java.util.LinkedList;

public class SchoolList extends LinkedList<School> {
    public School schoolSearch(String school){
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            if (theSchool.getName().equals(school)){
                return theSchool;
            }
        }
        System.out.println(school + " could not be found, please check your spelling and try again.");
        return null;
    }

    public School schoolSearch(int tgid){
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            if (theSchool.getTgid() == tgid){
                return theSchool;
            }
        }
        School newSchool = new School(tgid, "null", "null", "null", "null", "null");
        this.add(newSchool);
        System.out.println(tgid + " is being added");
        return newSchool;
    }

    public void populateUserSchools() {
        for (int i = 0; i < this.size(); i++) {
            School theSchool = this.get(i);
            int numOfUserGames = 0;
            for (int j = 0; j < theSchool.getSchedule().size(); j++) {
                if (theSchool.getSchedule().get(j).getUserGame() == 1) {
                    numOfUserGames++;
                }
            }
            if (numOfUserGames >= 10) {
                theSchool.setUserTeam(true);
            }
        }
    }
}
