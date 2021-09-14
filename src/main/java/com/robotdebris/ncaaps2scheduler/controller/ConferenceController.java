package com.robotdebris.ncaaps2scheduler.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.*;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;

@CrossOrigin(origins = "*")
@RestController
public class ConferenceController {

	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private ConferenceService conferenceService;
	
	@PutMapping(value = "/conferences/swap-schools")
	private void swapSchools(@RequestBody School s1, @RequestBody School s2) {
		conferenceService.swapSchools(s1, s2);
	}
	
	@PutMapping(value = "/conferences/swap-schools/{tgid1}/{tgid2}")
	private void swapSchools(@PathVariable int tgid1, @PathVariable int tgid2) {
		conferenceService.swapSchools(tgid1, tgid2);
	}
	
	@PutMapping(value = "/conferences/{name}/rename/{newName}")
	private void renameConference(@PathVariable String name, @PathVariable String newName) {
		conferenceService.renameConference(name, newName);
	}
	
	@PutMapping(value = "/conferences/{name}/division/{divisionName}/rename/{newName}")
	private void renameDivision(@PathVariable String name, @PathVariable String divisionName, @PathVariable String newName) {
		conferenceService.renameDivision(name, divisionName, newName);
	}
	
//	@PutMapping(value = "/conferences/swap-conferences/{name1}/{name2}")
//	private void swapSchools(@PathVariable String name1, @PathVariable String name2) {
//		conferenceService.swapConferences(name1, name2);
//	}
	
	@GetMapping(value = "/conferences/get-swap")
	private SwapList getSwapList() {
		return conferenceService.getSwapList();
	}
	
	@GetMapping(value = "/conferences/{name}/division/{division}/schools")
	public SchoolList getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
		SchoolList schools = conferenceService.getSchoolsByDivision(name, division);
		return schools;
	}
	
	
	//should these be moved here?
//	@GetMapping(value = "/conferences")
//	public ConferenceList getAllConferences() {
//		ConferenceList conferenceList = scheduleService.getConferenceList();
//		return conferenceList;
//	}
//	
//	@GetMapping(value = "/conferences/{name}")
//	public Conference getConferenceByName(@PathVariable String name) {
//		Conference conference = scheduleService.searchConferenceByName(name);
//		return conference;
//	}
//	
//	@GetMapping(value = "/conferences/{name}/schools")
//	public SchoolList getSchoolsByConference(@PathVariable String name) {
//		SchoolList schools = scheduleService.getSchoolsByConference(name);
//		return schools;
//	}
//	
//	@PostMapping(value = "/conferences/set-by-file")
//	public void setAlignmentFile(@RequestParam("file") MultipartFile alignmentFile) throws IOException {
//		scheduleService.setAlignmentFile(alignmentFile);
//	}
}
