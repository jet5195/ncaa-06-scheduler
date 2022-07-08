package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.Bowl;
import com.robotdebris.ncaaps2scheduler.model.SeasonSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class BowlService {

    @Autowired
    List<Bowl> bowlList;
    @Autowired
    ExcelReader excelReader;
    public void setBowlFile(MultipartFile bowlFile) throws IOException {
        File file = excelReader.multipartFileToFile(bowlFile);
        try {
            bowlList = excelReader.getBowlData(file);
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
}
