package com.robotdebris.ncaaps2scheduler.controller;

import com.opencsv.exceptions.CsvValidationException;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SuggestedGameResponse;
import com.robotdebris.ncaaps2scheduler.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schools")
public class SchoolController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private XlsxExportService exportService;
    @Autowired
    private CsvExportService csvExportService;
    @Autowired
    private CsvImportService csvImportService;

    @GetMapping
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping(value = "{tgid}")
    public School getSchoolByTgid(@PathVariable int tgid) {
        return schoolService.findById(tgid);
    }

    @GetMapping(value = "{tgid}/suggest-game")
    public SuggestedGameResponse getSuggestedGame(@PathVariable int tgid) {
        return scheduleService.getSuggestedGame(tgid);
    }

    @GetMapping(value = "{tgid}/schedule")
    public List<Game> getSchoolSchedule(@PathVariable Integer tgid) {
        School school = schoolService.findById(tgid);
        return scheduleService.getScheduleBySchool(school);
    }

    @GetMapping(value = "{tgid}/rivals")
    public List<School> getSchoolRivals(@PathVariable int tgid) {
        School school = schoolService.findById(tgid);
        return school.getRivals();
    }

    @GetMapping(value = "{tgid}/schedule/week/{week}/available-opponents")
    public List<School> getAvailableOpponents(@PathVariable int tgid, @PathVariable int week) {
        return scheduleService.findOpenOpponentsForWeek(tgid, week);
    }

    @GetMapping(value = "{tgid}/schedule/week/{week}/available-rivals")
    public List<School> getAvailableRivals(@PathVariable int tgid, @PathVariable int week) {
        return scheduleService.getOpenNonConferenceRivals(tgid, week);
    }

    @PostMapping(value = "{tgid}/schedule/week/{week}/remove-game")
    public void removeGame(@PathVariable int tgid, @PathVariable int week) {
        School school = schoolService.findById(tgid);
        Game game = scheduleService.getGameBySchoolAndWeek(school, week);
        scheduleService.removeGame(game);
    }

    @GetMapping(value = "{tgid}/schedule/empty-weeks")
    public List<Integer> getEmptyWeeks(@PathVariable int tgid) {
        School school = schoolService.findById(tgid);
        return scheduleService.findEmptyWeeks(school);
    }

    @GetMapping(value = "{tgid}/schedule/empty-weeks/{tgid2}")
    public List<Integer> getEmptyWeeks(@PathVariable int tgid, @PathVariable int tgid2) {
        School school = schoolService.findById(tgid);
        School school2 = schoolService.findById(tgid2);
        return scheduleService.findEmptyWeeks(school, school2);
    }

    @PostMapping(value = "set-by-file")
    public void uploadSchoolData(@RequestParam("file") MultipartFile file) throws IOException {
        schoolService.loadSchoolDataFromFile(file);
    }

    @PostMapping(value = "upload-teams")
    public ResponseEntity<String> uploadTeamCsv(@RequestParam("file") MultipartFile teamCsvFile) {
        if (teamCsvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Team CSV file is empty");
        }
        try {
            csvImportService.loadTeamDataFromCsv(teamCsvFile);
            return ResponseEntity.ok().body("Team CSV file successfully imported");
        } catch (IOException | CsvValidationException e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process Team CSV: " + e.getMessage());
        } catch (Exception e) {
            // Catch other potential errors during processing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Endpoint for downloading the amended team data
    @GetMapping("/download-teams")
    public void downloadTeamCsv(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=amended_teams.csv");
        // Assuming DataLoadingService or TeamDataService has the export method
        // dataLoadingService.exportTeamDataToCsv(response.getWriter()); // Adapt as needed
    }

    @GetMapping(value = "download")
    public ResponseEntity<?> downloadSchoolData() {
        try {
            return exportService
                    .writeSchoolData(schoolService.getAllSchools());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
