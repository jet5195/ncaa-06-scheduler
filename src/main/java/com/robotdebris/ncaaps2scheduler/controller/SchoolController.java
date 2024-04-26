package com.robotdebris.ncaaps2scheduler.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SuggestedGameResponse;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("schools")
public class SchoolController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SchoolService schoolService;

    @GetMapping
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping(value = "{tgid}")
    public School getSchoolByTgid(@PathVariable int tgid) {
        return schoolService.schoolSearch(tgid);
    }

    @GetMapping(value = "{tgid}/suggest-game")
    public SuggestedGameResponse getSuggestedGame(@PathVariable int tgid) {
        return scheduleService.getSuggestedGame(tgid);
    }

    @GetMapping(value = "{tgid}/schedule")
    public List<Game> getSchoolSchedule(@PathVariable Integer tgid) {
        School school = schoolService.schoolSearch(tgid);
        return scheduleService.getScheduleBySchool(school);
    }

    @GetMapping(value = "{tgid}/rivals")
    public List<School> getSchoolRivals(@PathVariable int tgid) {
        School school = schoolService.schoolSearch(tgid);
        return school.getRivals();
    }

    @GetMapping(value = "{tgid}/schedule/week/{week}/available-opponents")
    public List<School> getAvailableOpponents(@PathVariable int tgid, @PathVariable int week) {
        List<School> availableOpponents = scheduleService.findOpenOpponentsForWeek(tgid, week);
        return availableOpponents;
    }

    @GetMapping(value = "{tgid}/schedule/week/{week}/available-rivals")
    public List<School> getAvailableRivals(@PathVariable int tgid, @PathVariable int week) {
        List<School> availableRivals = scheduleService.getOpenNonConferenceRivals(tgid, week);
        return availableRivals;
    }

    @PostMapping(value = "{tgid}/schedule/week/{week}/remove-game")
    public void removeGame(@PathVariable int tgid, @PathVariable int week) {
        School school = schoolService.schoolSearch(tgid);
        Game game = scheduleService.getGameBySchoolAndWeek(school, week);
        scheduleService.removeGame(game);
    }

    @GetMapping(value = "{tgid}/schedule/empty-weeks")
    public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid) {
        School school = schoolService.schoolSearch(tgid);
        return scheduleService.findEmptyWeeks(school);
    }

    @GetMapping(value = "{tgid}/schedule/empty-weeks/{tgid2}")
    public ArrayList<Integer> getEmptyWeeks(@PathVariable int tgid, @PathVariable int tgid2) {
        School school = schoolService.schoolSearch(tgid);
        School school2 = schoolService.schoolSearch(tgid2);
        return scheduleService.findEmptyWeeks(school, school2);
    }
}
