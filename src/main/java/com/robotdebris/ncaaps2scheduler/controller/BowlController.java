package com.robotdebris.ncaaps2scheduler.controller;

import com.robotdebris.ncaaps2scheduler.model.Bowl;
import com.robotdebris.ncaaps2scheduler.model.SeasonSchedule;
import com.robotdebris.ncaaps2scheduler.service.BowlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public List<Bowl> getBowls() {return bowlService.getBowlList();}

    @PutMapping()
    public void setBowls(List<Bowl> bowlList) {bowlService.setBowlList(bowlList);}
}
