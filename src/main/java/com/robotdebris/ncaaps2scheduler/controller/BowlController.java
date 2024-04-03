package com.robotdebris.ncaaps2scheduler.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Bowl;
import com.robotdebris.ncaaps2scheduler.service.BowlService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("bowls")
public class BowlController {

	@Autowired
	private BowlService bowlService;

	@PostMapping(value = "set-by-file")
	public void setScheduleFile(@RequestParam("file") MultipartFile scheduleFile) throws IOException {
		bowlService.setBowlFile(scheduleFile);
	}

	@GetMapping()
	public List<Bowl> getBowls() {
		return bowlService.getBowlList();
	}

	@PutMapping()
	public void setBowls(List<Bowl> bowlList) {
		bowlService.setBowlList(bowlList);
	}
}
