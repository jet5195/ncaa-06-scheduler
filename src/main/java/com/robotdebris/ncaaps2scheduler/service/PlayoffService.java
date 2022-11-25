package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.NoWeeksAvailableException;
import com.robotdebris.ncaaps2scheduler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

@Service
public class PlayoffService {

    @Autowired
    SchoolList schoolList;
    @Autowired
    ConferenceList conferenceList;
    @Autowired
    SeasonSchedule seasonSchedule;

    void schedulePlayoff(){
        //check if bowl week 1 has games played
        //if not
        //move bowl games to week 1
        //
    }

}