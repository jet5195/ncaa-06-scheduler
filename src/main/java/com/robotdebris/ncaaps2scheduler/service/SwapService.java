package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.Swap;
import com.robotdebris.ncaaps2scheduler.repository.SwapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Service
public class SwapService {

    @Autowired
    SchoolService schoolService;
    @Autowired
    ConferenceService conferenceService;

    SwapRepository swapRepository;

    public SwapService(SwapRepository swapRepository) {
        this.swapRepository = swapRepository;
    }

    public List<Swap> getSwapList() {
        // right now setting the order when pulling the data, not sure if this makes
        // more sense
        // or setting the swap order while running
        List<Swap> swapList = swapRepository.findAll();
        for (int i = 0; i < swapList.size(); i++) {
            swapList.get(i).setSwapID(i);
        }
        return swapList;
    }

    public void downloadSwapFile(Writer writer) {
        try {
            CsvExportService csvExportService = new CsvExportService();
            csvExportService.writeSwapList(writer, getSwapList());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void swapSchools(int tgid1, int tgid2) {
        School s1 = schoolService.schoolSearch(tgid1);
        School s2 = schoolService.schoolSearch(tgid2);
        swapSchools(s1, s2);
    }

    public void swapSchools(School s1, School s2) {
        // if the conferences & divisions aren't already the same...
        if (!(s1.getConference().getName() == s2.getConference().getName() && s1.getDivision() == s2.getDivision())) {
            Conference tempConf = s1.getConference();
            String tempDiv = s1.getDivision();
            String tempNcaaDiv = s1.getNcaaDivision();

            s1.setConference(s2.getConference());
            s1.setDivision(s2.getDivision());
            s1.setNcaaDivision(s2.getNcaaDivision());
            // this could also be done recursively like conf swap
            s2.setConference(tempConf);
            s2.setDivision(tempDiv);
            s2.setNcaaDivision(tempNcaaDiv);

            // need to add schedule stuff here
            // need to reset the conferenceList, it isn't updating
            // in the future, optimize this by making it only set the updated confs instead
            // of all
            // TODO: this is ugly
            conferenceService.setConferencesSchoolList(schoolService.getAllSchools());

            getSwapList().add(new Swap(s1, s2));
        }
    }

}
