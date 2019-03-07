public class School {
    private int tgid;
    private String name;
    private String nickname;
    private String state;
    private String conference;
    private String division; //fbs or fcs
    private SchoolList rivals;
    private boolean userTeam;
    private SchoolSchedule schedule = new SchoolSchedule();
    private boolean powerConf;

    public School(int tgid, String name, String nickname, String state, String conference, String division) {
        this.tgid = tgid;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.conference = conference;
        this.division = division;
    }

    public SchoolList getRivals() {
        return this.rivals;
    }

    public void setRivals(SchoolList rivals) {
        this.rivals = rivals;
    }

    public int getTgid() {
        return tgid;
    }

    public void setTgid(int tgid) {
        this.tgid = tgid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public boolean isUserTeam() {
        return userTeam;
    }

    public void setUserTeam(boolean userTeam) {
        this.userTeam = userTeam;
    }

    public void addGame(Game theGame) {
        this.schedule.add(theGame);
    }

    public SchoolSchedule getSchedule() {
        return schedule;
    }

    public boolean isPowerConf() {
        return powerConf;
    }

    public void setPowerConf(boolean powerConf) {
        this.powerConf = powerConf;
    }

    /**
     * @return
     */
    public Game findRemovableGame() {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            Game theGame = this.getSchedule().get(i);
            //0 means non-con
            if (theGame.isRemovableGame()) {
                return theGame;
            }
        }
        return null;
    }

    public boolean isInConference(School school) {
        return this.getConference().equalsIgnoreCase(school.getConference());
    }

    public boolean isOpponent(School school) {
        for (int i = 0; i < this.getSchedule().size(); i++) {
            if (this.getSchedule().get(i).getHomeTeam() != null && this.getSchedule().get(i).getAwayTeam() != null) {
                if (this.getSchedule().get(i).getHomeTeam().getTgid() == school.getTgid() ||
                        this.getSchedule().get(i).getAwayTeam().getTgid() == school.getTgid()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPossibleOpponent(School school) {
        return !this.isInConference(school) && !this.isOpponent(school);
    }

    public void printSchedule() {
        int i = 0;
        int lastWeek = -1;
        while (i < this.getSchedule().size()) {
            int nextWeek = 100;//random high number
            for (int j = 0; j < this.getSchedule().size(); j++) {
                if (this.getSchedule().get(j).getWeek() < nextWeek && this.getSchedule().get(j).getWeek() > lastWeek) {
                    nextWeek = this.getSchedule().get(j).getWeek();
                }
            }

            System.out.print(i + 1 + ". ");
            Game game = this.getSchedule().getGame(nextWeek);
            System.out.print(this);
            if (this.getTgid() == game.getHomeTeam().getTgid()) {
                System.out.print(" vs " + game.getAwayTeam());
            } else {
                System.out.print(" at " + game.getHomeTeam());
            }
            System.out.println(" (week " + (nextWeek + 1) + ")");
            i++;
            lastWeek = nextWeek;
        }
    }

    public boolean isRival(School opponent) {
        for (int i = 0; i < this.getRivals().size(); i++) {
            if (this.getRivals().get(i).getName().equals(opponent.getName())) {
                return true;
            }
        }
        for (int i = 0; i < opponent.getRivals().size(); i++) {
            if (opponent.getRivals().get(i).getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
