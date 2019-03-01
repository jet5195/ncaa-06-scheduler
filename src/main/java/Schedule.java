import java.util.ArrayList;
import java.util.LinkedList;

public class Schedule extends LinkedList<Game> {
    public Game getGame(int week){
        for (int i = 0; i < this.size(); i++) {
            if(this.get(i).getWeek()==week){
                return this.get(i);
            }
        }
        return null;
    }

    public ArrayList scheduleToList(){
        ArrayList<ArrayList> list = new ArrayList();
        ArrayList<String> firstLine = new ArrayList();
        firstLine.add("GSTA"); firstLine.add("GASC"); firstLine.add("GHSC"); firstLine.add("GTOD");
        firstLine.add("GATG"); firstLine.add("GHTG"); firstLine.add("SGNM"); firstLine.add("SEWN");
        firstLine.add("GDAT"); firstLine.add("GFOT"); firstLine.add("SEWT"); firstLine.add("GFFU");
        firstLine.add("GFHU"); firstLine.add("GMFX"); firstLine.add(String.valueOf(this.size()));
        list.add(firstLine);
        for (int i = 0; i < this.size(); i++) {
            list.add(this.get(i).gameToList());
        }
        return list;
    }


}