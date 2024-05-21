package com.robotdebris.ncaaps2scheduler.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.DivisionService;
import com.robotdebris.ncaaps2scheduler.service.ScheduleService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import com.robotdebris.ncaaps2scheduler.service.XlsxExportService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("conferences")
public class ConferenceController {

	@Autowired
	private ConferenceService conferenceService;
	@Autowired
	private ScheduleService scheduleService;
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

	@PostMapping(value = "{name}/add-school")
	private void addSchool(@PathVariable String name, @RequestBody School s1) {
		conferenceService.addSchool(name, s1);
	}

	@GetMapping(value = "{name}/division/{division}/schools")
	public List<School> getSchoolsByDivision(@PathVariable String name, @PathVariable String division) {
		Division divisionObj = divisionService.findByName(division);
		return conferenceService.getSchoolsByDivision(name, divisionObj);
	}

	@GetMapping(value = "download")
	public ResponseEntity<?> downloadConferenceAlignment() {
		try {
			ResponseEntity<ByteArrayResource> response = exportService
					.writeConferenceAlignment(conferenceService.getConferenceList(), schoolService.getAllSchools());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
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
		conferenceService.saveConferences(conferences);
		List<Division> divisionList = conferences.stream().flatMap(c -> c.getDivisions().stream()).toList();
		divisionService.saveDivisions(divisionList);
		for (Conference conference : conferences) {
			for (School school : conference.getSchools()) {
				// why don't we have to do this for conference???? unsure
				Optional<Division> optionalDiv = divisionService.findById(school.getDivisionId());
				if (optionalDiv.isPresent()) {
					school.setDivision(optionalDiv.get());
				} else {
					school.setDivision(null);
				}
				schoolService.saveSchool(school);
			}
		}
	}

}
