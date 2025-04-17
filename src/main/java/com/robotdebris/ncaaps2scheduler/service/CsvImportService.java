package com.robotdebris.ncaaps2scheduler.service;

import com.opencsv.exceptions.CsvValidationException;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.parser.TeamCsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    @Autowired
    private TeamCsvParser teamCsvParser;
    @Autowired
    private SchoolService schoolService;

    public void loadTeamDataFromCsv(MultipartFile teamCsvFile) throws CsvValidationException, IOException {
        Map<Integer, School> schoolMap = teamCsvParser.parseAndCreateTeamData(teamCsvFile);
        //convert schoolMap to List
        List<School> schoolList = new ArrayList<>(schoolMap.values());
        schoolService.saveSchools(schoolList);
    }
}
