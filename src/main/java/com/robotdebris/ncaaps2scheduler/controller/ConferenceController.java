package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.Swap;
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
@RequestMapping("conferences")
public class ConferenceController {

    @Autowired
    private ConferenceService conferenceService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SwapService swapService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private XlsxExportService exportService;
    @Autowired
    private DivisionService divisionService;

    @GetMapping
    public List<Conference> getAllConferences() {
        List<Conference> conferenceList = conferenceService.getConferenceList();
        return conferenceList;
    }

    @GetMapping(value = "{name}")
    public Conference getConferenceByName(@PathVariable String name) {
        Conference conference = conferenceService.findByShortName(name);
        return conference;
    }

    @GetMapping(value = "{name}/schools")
    public List<School> getSchoolsByConference(@PathVariable String name) {
        if (name.equalsIgnoreCase("all")) {
            return schoolService.getAllSchools();
        }
        Conference conf = conferenceService.findByShortName(name);
        return conf.getSchools();
    }

    @PostMapping(value = "swap-schools")
    private void swapSchools(@RequestBody School s1, @RequestBody School s2) {
        swapService.swapSchools(s1, s2);
    }

    @PostMapping(value = "swap-schools/{tgid1}/{tgid2}")
    private void swapSchools(@PathVariable int tgid1, @PathVariable int tgid2) {
        swapService.swapSchools(tgid1, tgid2);
    }

    @PostMapping(value = "{name}/add-school")
    private void addSchool(@PathVariable String name, @RequestBody School s1) {
        conferenceService.addSchool(name, s1);
    }

    @GetMapping(value = "swap")
    private List<Swap> getSwapList() {
        return swapService.getSwapList();
    }

    @GetMapping(value = "{name}/division/{division}/schools")
    public List<School> getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
        Division divisionObj = divisionService.findByName(division);
        return conferenceService.getSchoolsByDivision(name, divisionObj);
    }

    @PostMapping(value = "download")
    public ResponseEntity<?> downloadConferenceAlignment() {
        try {
            return exportService.writeConferenceAlignment(conferenceService.getConferenceList(), schoolService.getAllSchools());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping(value = "swap-download")
    public void downloadSwapList(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=swaplist.csv");
        swapService.downloadSwapFile(response.getWriter());
    }

    @PostMapping(value = "{name}/add-games")
    public int autoAddConferenceGames(@PathVariable String name) throws Exception {
        int count = scheduleService.getSeasonSchedule().size();
        Conference conf = conferenceService.findByShortName(name);
        scheduleService.autoAddConferenceGames(conf);
        return scheduleService.getSeasonSchedule().size() - count;
    }

    @PostMapping(value = "{name}/remove-games")
    public int removeConferenceGames(@PathVariable String name) {
        Conference conf = conferenceService.findByShortName(name);
        return scheduleService.removeConfGamesByConference(conf);
    }

    @PostMapping(value = "set-by-file")
    public void setAlignmentFile(@RequestParam("file") MultipartFile alignmentFile) throws IOException {
        scheduleService.setAlignmentFile(alignmentFile);
    }

    @PutMapping()
    public void saveConferences(@RequestBody List<Conference> conferences) {
//        for (Conference c : conferences) {
//            for (School s : c.getSchools()) {
//                s.setConference(c);
//            }
//        }
        // TODO: probably will have to map conferences for each school
        conferenceService.saveConferences(conferences);
    }

}
