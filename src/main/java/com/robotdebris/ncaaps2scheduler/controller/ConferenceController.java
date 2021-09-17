package com.robotdebris.ncaaps2scheduler.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.robotdebris.ncaaps2scheduler.model.*;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.robotdebris.ncaaps2scheduler.service.ConferenceService;

@CrossOrigin(origins = "*")
@RestController
public class ConferenceController {
	
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
	
	@GetMapping(value = "/conferences/get-swap")
	private SwapList getSwapList() {
		return conferenceService.getSwapList();
	}
	
	@GetMapping(value = "/conferences/{name}/division/{division}/schools")
	public SchoolList getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
		SchoolList schools = conferenceService.getSchoolsByDivision(name, division);
		return schools;
	}
	
	@GetMapping(value = "conferences/download")
	public void downloadSwapList(HttpServletResponse response) throws IOException {
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=swap_list.xlsx");
        ByteArrayInputStream stream = conferenceService.downloadSwapFile();
        IOUtils.copy(stream, response.getOutputStream());
	}
	
}
