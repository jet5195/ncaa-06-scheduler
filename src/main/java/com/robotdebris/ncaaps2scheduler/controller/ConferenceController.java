package com.robotdebris.ncaaps2scheduler.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.robotdebris.ncaaps2scheduler.model.*;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("conferences")
public class ConferenceController {
	
	@Autowired
	private ConferenceService conferenceService;
	@Autowired
	private ScheduleService  scheduleService;
	
	@GetMapping
	public ConferenceList getAllConferences() {
		ConferenceList conferenceList = scheduleService.getConferenceList();
		return conferenceList;
	}
	
	@GetMapping(value = "{name}")
	public Conference getConferenceByName(@PathVariable String name) {
		Conference conference = scheduleService.searchConferenceByName(name);
		return conference;
	}
	
	@GetMapping(value = "{name}/schools")
	public SchoolList getSchoolsByConference(@PathVariable String name) {
		SchoolList schools = scheduleService.getSchoolsByConference(name);
		return schools;
	}
	
	@PostMapping(value = "swap-schools")
	private void swapSchools(@RequestBody School s1, @RequestBody School s2) {
		conferenceService.swapSchools(s1, s2);
	}
	
	@PostMapping(value = "swap-schools/{tgid1}/{tgid2}")
	private void swapSchools(@PathVariable int tgid1, @PathVariable int tgid2) {
		conferenceService.swapSchools(tgid1, tgid2);
	}

	@PostMapping(value = "{name}/add-school")
	private void addSchool(@PathVariable String name, @RequestBody School s1) {
		conferenceService.addSchool(name, s1);
	}
	
	@PostMapping(value = "{name}/rename/{newName}")
	private void renameConference(@PathVariable String name, @PathVariable String newName) {
		conferenceService.renameConference(name, newName);
	}
	
	@PostMapping(value = "{name}/division/{divisionName}/rename/{newName}")
	private void renameDivision(@PathVariable String name, @PathVariable String divisionName, @PathVariable String newName) {
		conferenceService.renameDivision(name, divisionName, newName);
	}
	
	@GetMapping(value = "swap")
	private SwapList getSwapList() {
		return conferenceService.getSwapList();
	}
	
	@GetMapping(value = "{name}/division/{division}/schools")
	public SchoolList getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
		SchoolList schools = conferenceService.getSchoolsByDivision(name, division);
		return schools;
	}
	
	@GetMapping(value = "download")
	public void downloadSwapList(HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=swaplist.csv");
        conferenceService.downloadSwapFile(response.getWriter());
	}
	
	@PostMapping(value = "{name}/add-games")
	public int autoAddConferenceGames(@PathVariable String name) {
		return scheduleService.autoAddConferenceGames(name, 0);
	}

	@PostMapping(value = "{name}/remove-games")
	public int removeConferenceGames(@PathVariable String name) {
		return scheduleService.removeConferenceGames(name);
	}
	
	@PostMapping(value = "set-by-file")
	public void setAlignmentFile(@RequestParam("file") MultipartFile alignmentFile) throws IOException {
		scheduleService.setAlignmentFile(alignmentFile);
	}
	
}
