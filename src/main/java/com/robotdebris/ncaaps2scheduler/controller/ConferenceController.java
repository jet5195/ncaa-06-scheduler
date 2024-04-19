package com.robotdebris.ncaaps2scheduler.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.Swap;
import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SwapService;

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

	@GetMapping
	public List<Conference> getAllConferences() {
		List<Conference> conferenceList = conferenceService.getConferenceList();
		return conferenceList;
	}

	@GetMapping(value = "{name}")
	public Conference getConferenceByName(@PathVariable String name) {
		Conference conference = scheduleService.searchConferenceByName(name);
		return conference;
	}

	@GetMapping(value = "{name}/schools")
	public List<School> getSchoolsByConference(@PathVariable String name) {
		List<School> schools = scheduleService.getSchoolsByConference(name);
		return schools;
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

	@PostMapping(value = "{name}/rename/{newName}")
	private void renameConference(@PathVariable String name, @PathVariable String newName) {
		conferenceService.renameConference(name, newName);
	}

	@PostMapping(value = "{name}/division/{divisionName}/rename/{newName}")
	private void renameDivision(@PathVariable String name, @PathVariable String divisionName,
			@PathVariable String newName) {
		conferenceService.renameDivision(name, divisionName, newName);
	}

	@GetMapping(value = "swap")
	private List<Swap> getSwapList() {
		return swapService.getSwapList();
	}

	@GetMapping(value = "{name}/division/{division}/schools")
	public List<School> getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
		List<School> schools = conferenceService.getSchoolsByDivision(name, division);
		return schools;
	}

	@GetMapping(value = "download")
	public void downloadSwapList(HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=swaplist.csv");
		swapService.downloadSwapFile(response.getWriter());
	}

	@PostMapping(value = "{name}/add-games")
	public int autoAddConferenceGames(@PathVariable String name) throws Exception {
		return scheduleService.autoAddConferenceGames(name);
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
