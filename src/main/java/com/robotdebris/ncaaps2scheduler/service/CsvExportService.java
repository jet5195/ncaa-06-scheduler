package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.Swap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Service
public class CsvExportService {

    /**
     * Writes schedule to a new csv file
     *
     * @param seasonSchedule the schedule to write to a new csv file
     * @throws IOException
     */
    public void writeSchedule(Writer writer, List<List> seasonSchedule) throws IOException {

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
//        ArrayList<ArrayList> bowlList = new ArrayList<>();
//        if(seasonSchedule.getBowlSchedule() != null) {
//            bowlList = seasonSchedule.getBowlSchedule().scheduleToList(false);
//        }
        int i = 0;
        while (i < seasonSchedule.size()) {
            addGameToCSV(csvPrinter, seasonSchedule.get(i), i);
//            if (i == 0) {
//    			csvPrinter.print(list.size()+ bowlList.size() -1);
//    		}
            csvPrinter.println();
            i++;
        }

//        for (int j = 0; j < bowlList.size(); j++) {
//            addGameToCSV(csvPrinter, bowlList.get(j), i);
//            csvPrinter.println();
//            i++;
//        }
    }

    /**
     * Writes swap to a new excel file
     *
     * @throws IOException
     */
    public void writeSwapList(Writer writer, List<Swap> swaplist) throws IOException {

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

        csvPrinter.print("TGID");
        csvPrinter.print("TIDR");
        csvPrinter.print("SWOR");

        int i = 0;
        while (i < swaplist.size()) {
            csvPrinter.println();
            csvPrinter.print(swaplist.get(i).getSchool1().getTgid());
            csvPrinter.print(swaplist.get(i).getSchool2().getTgid());
            csvPrinter.print(i);
            i++;
        }
        csvPrinter.close();
    }

    private void addGameToCSV(CSVPrinter csvPrinter, List game, int r) throws IOException {
        for (int c = 0; c < game.size(); c++) {
            csvPrinter.print(game.get(c));
        }
    }

    public void writeTeamCsv(Writer writer, List<School> schoolList) throws IOException {
        
    }

}
