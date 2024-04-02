package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.Bowl;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.SeasonSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BowlService {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    List<Bowl> bowlList;
    @Autowired
    ExcelReader excelReader;
    public void setBowlFile(MultipartFile bowlFile) throws IOException {
        File file = excelReader.convertMultipartFileToFile(bowlFile);
        try {
            bowlList = excelReader.populateBowlsFromExcel(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public List<Bowl> getBowlList() {
        return this.bowlList;
    }

    public void setBowlList(List<Bowl> bowlList) {
        this.bowlList = bowlList;
    }

    public Bowl findChampionship(){
        //championship game is always the last game in the last week
        int maxWeek = 0;
        int maxSgnm = 0;//game number
        Bowl result = new Bowl();
        for (Bowl bowl: this.bowlList) {
            if (bowl.getWeek() > maxWeek || (bowl.getWeek() == maxWeek && bowl.getGameNumber() > maxSgnm)){
                maxWeek = bowl.getWeek();
                maxSgnm = bowl.getGameNumber();
                result = bowl;
            }
        }
        return result;
    }

    public Bowl findBowl(String bowlName){
        Bowl result = new Bowl();
        for (Bowl bowl: this.bowlList) {
            if(bowl.getBowlName().contains(bowlName)){
                result = bowl;
            }
        }
        return result;
    }

    public Bowl findBowl(int blgo){
        Bowl emptyBowl = new Bowl();
        for (Bowl bowl: this.bowlList) {
            if(bowl.getBowlLogo() == blgo){
                return bowl;
            }
        }
        return emptyBowl;
    }

    public void recalculateGameNumbers() {
        for (int weekNum = 17; weekNum < 23; weekNum++){
            ArrayList<Game> weeklySchedule = this.scheduleService.seasonSchedule.getScheduleByWeek(weekNum);
            for (int gameNum = 0; gameNum < weeklySchedule.size(); gameNum++ ){
                weeklySchedule.get(gameNum).setGameNumber(gameNum);
            }
        }

        for (int weekNum = 17; weekNum < 23; weekNum++){
            int bowlIndex = 0;
            ArrayList<Bowl> weeklySchedule = this.getScheduleByWeek(weekNum);
            for (int gameNum = 0; gameNum < weeklySchedule.size(); gameNum++ ){
                weeklySchedule.get(gameNum).setGameNumber(gameNum);
                weeklySchedule.get(gameNum).setBowlIndex(bowlIndex);
                bowlIndex++;
            }
        }
    }

    public ArrayList<Bowl> getScheduleByWeek(int week) {
        ArrayList<Bowl> weeklySchedule = new ArrayList<>();
        for (Bowl bowl : this.bowlList) {
            if (bowl.getWeek() == week) {
                weeklySchedule.add(bowl);
            }
        }
        return weeklySchedule;
    }
}
