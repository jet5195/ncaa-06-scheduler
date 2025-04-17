package com.robotdebris.ncaaps2scheduler.parser;


import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class TeamCsvParser {

    private final SchoolService schoolService;
    private static final Logger LOGGER = Logger.getLogger(TeamCsvParser.class);

    private static final String HEADER_ID = "TGID";
    private static final String HEADER_NAME = "TDNA";
    private static final String HEADER_NICKNAME = "TMNA";
    private static final String HEADER_ABBREVIATION = "TSNA";
    private static final String HEADER_CONFERENCE_ID = "CGID";   // Conference Global ID
    private static final String HEADER_DIVISION_ID = "DGID";     // Division Global ID
    private static final String HEADER_PRESTIGE = "TPRS";        // Team Prestige Rating
    private static final String HEADER_STADIUM_ID = "SGID";      // Stadium Global ID

    @Autowired
    public TeamCsvParser(SchoolService schoolService) {
        this.schoolService = schoolService;
    }


    public Map<Integer, School> parseAndCreateTeamData(MultipartFile teamCsvFile) throws IOException, CsvValidationException {
        Map<Integer, School> schoolMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(teamCsvFile.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(0).build()) {
            String[] headers = csvReader.readNext(); // Read the header row
            if (headers == null) {
                LOGGER.warn("Team CSV file is null or empty. Returning empty map.");
                return schoolMap;
            }

            // Map headers to their indices for efficient lookup
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                // Normalize header names (optional but good practice)
                headerMap.put(headers[i].trim(), i);
            }

            // Verify essential headers exist (optional but recommended)
            if (!headerMap.containsKey(HEADER_ID) || !headerMap.containsKey(HEADER_NAME)) {
                LOGGER.error("Team CSV file is missing required headers (TGID, TDNA). Cannot process.");
                return schoolMap; // Or throw specific exception
            }

            String[] line;
            int rowNum = 1; // Start after header
            while ((line = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    int tgid = parseInt(line, headerMap, HEADER_ID, rowNum);
                    if (tgid < 0) { // Use negative return from helper to indicate parsing error
                        // Error already logged by helper
                        continue;
                    }

                    // --- Get existing or create NEW School object ---
                    // Since this is the primary loader, we mostly create, but checking handles duplicates in the CSV.
                    School school = schoolMap.get(tgid);
                    if (school == null) {
                        school = new School();
                        school.setTgid(tgid);
                        // Initialize non-CSV fields to defaults
                        school.setRivals(new ArrayList<>());
                        // Set other defaults if needed (colors, location placeholders, etc.)
                        // These will be overwritten by later loaders if data is found.
                        schoolMap.put(tgid, school);
                    } else {
                        LOGGER.warn("Duplicate TGID " + tgid + " found at row " + rowNum + ". Updating existing entry.");
                    }

                    // --- Populate/Update fields from CSV ---
                    // Use default values if parsing fails within helpers
                    school.setName(getString(line, headerMap, HEADER_NAME, rowNum, school.getName())); // Keep old name on failure
                    school.setNickname(getString(line, headerMap, HEADER_NICKNAME, rowNum, school.getNickname()));
                    school.setAbbreviation(getString(line, headerMap, HEADER_ABBREVIATION, rowNum, school.getAbbreviation()));
                    school.setConferenceId(parseInt(line, headerMap, HEADER_CONFERENCE_ID, rowNum, school.getConferenceId())); // Keep old ID on failure
                    school.setDivisionId(parseInt(line, headerMap, HEADER_DIVISION_ID, rowNum, school.getDivisionId()));
                    school.setPrestige(parseInt(line, headerMap, HEADER_PRESTIGE, rowNum, school.getPrestige()));
                    school.setStadiumId(parseInt(line, headerMap, HEADER_STADIUM_ID, rowNum, school.getStadiumId()));

                    // Conference, Division objects (Conference/Division fields) are NOT linked here.
                    // Rivals are NOT set here.
                    // Location, Colors, Logos are NOT set here.

                } catch (Exception e) {
                    // Catch unexpected errors during row processing
                    LOGGER.error("Unexpected error processing team CSV row " + rowNum + ": " + e.getMessage(), e);
                }
            }
            LOGGER.info("Successfully parsed and created/updated " + schoolMap.size() + " unique schools from team CSV (" + (rowNum - 1) + " rows processed).");

        } // try-with-resources handles closing reader/csvReader
        return schoolMap;
    }

    // --- Helper methods with improved error logging and default value handling ---

    private String getString(String[] line, Map<String, Integer> headerMap, String headerName, int rowNum, String defaultValue) {
        Integer index = headerMap.get(headerName);
        if (index == null) {
            // Don't log every time if it's just an optional header missing
            LOGGER.trace("Header '" + headerName + "' not found for row " + rowNum);
            return defaultValue; // Return default if header doesn't exist
        }
        if (index >= line.length || line[index] == null) {
            LOGGER.warn("Null or out-of-bounds value for header '" + headerName + "' at row " + rowNum);
            return defaultValue; // Return default for missing value
        }
        return line[index].trim(); // Trim whitespace
    }

    private int parseInt(String[] line, Map<String, Integer> headerMap, String headerName, int rowNum, Integer defaultValue) {
        String valueStr = getString(line, headerMap, headerName, rowNum, null); // Get raw string or null

        if (valueStr == null || valueStr.isEmpty()) {
            // Don't log if default is provided, assume optional or handled
            // if (defaultValue == null) LOGGER.warn("Integer value for header '" + headerName + "' is missing at row " + rowNum);
            return defaultValue != null ? defaultValue : 0; // Use default or 0 if no default specified
        }
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to parse integer for header '" + headerName + "' at row " + rowNum + ". Value: '" + valueStr + "'");
            return defaultValue != null ? defaultValue : 0; // Use default or 0 on format error
        }
    }

    // Overload for required fields where default doesn't make sense (like TGID)
    private int parseInt(String[] line, Map<String, Integer> headerMap, String headerName, int rowNum) {
        String valueStr = getString(line, headerMap, headerName, rowNum, null);

        if (valueStr == null || valueStr.isEmpty()) {
            LOGGER.error("Required integer value for header '" + headerName + "' is missing at row " + rowNum);
            return -1; // Indicate error
        }
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse required integer for header '" + headerName + "' at row " + rowNum + ". Value: '" + valueStr + "'", e);
            return -1; // Indicate error
        }
    }

}
