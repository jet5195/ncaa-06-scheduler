package com.robotdebris.ncaaps2scheduler;

import com.opencsv.exceptions.CsvValidationException;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.parser.TeamCsvParser;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TeamCsvParserTest {

    private TeamCsvParser teamCsvParser;

    @Mock
    private SchoolService schoolService;

    @BeforeEach
    void setUp() {
        // We mock SchoolService because it is required by the constructor,
        // even though the parse method doesn't currently use it.
        teamCsvParser = new TeamCsvParser(schoolService);
    }

    @Test
    void parseAndCreateTeamData_ValidCsv_ReturnsPopulatedMap() throws IOException, CsvValidationException {
        // 1. Prepare a CSV string acting as a "Game Export"
        // Notice the headers match your static constants in the parser
        String csvContent = "TGID,TDNA,TMNA,TSNA,CGID,DGID,TPRS,SGID\n" +
                "100,Virginia Tech,Hokies,VT,5,0,95,200\n" +
                "101,Virginia,Cavaliers,UVA,5,1,80,201";

        // 2. Convert to MockMultipartFile
        MockMultipartFile file = new MockMultipartFile(
                "data",
                "TEAMS.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // 3. Run the method
        Map<Integer, School> result = teamCsvParser.parseAndCreateTeamData(file);

        // 4. Assertions
        assertEquals(2, result.size(), "Should find 2 schools");

        // Validate Virginia Tech
        assertTrue(result.containsKey(100));
        School vt = result.get(100);
        assertEquals("Virginia Tech", vt.getName());
        assertEquals("Hokies", vt.getNickname());
        assertEquals("VT", vt.getAbbreviation());
        assertEquals(5, vt.getConferenceId());
        assertEquals(95, vt.getPrestige());
        assertEquals(200, vt.getStadiumId());
    }

    @Test
    void parseAndCreateTeamData_ReorderedColumns_ParsesCorrectly() throws IOException, CsvValidationException {
        // DB Editor exports might reorder columns. Your parser handles this dynamically.
        // Here TGID is at the end.
        String csvContent = "TDNA,TMNA,TGID\n" +
                "Ohio State,Buckeyes,50";

        MockMultipartFile file = new MockMultipartFile("data", "TEAMS.csv", "text/csv", csvContent.getBytes());

        Map<Integer, School> result = teamCsvParser.parseAndCreateTeamData(file);

        assertEquals(1, result.size());
        School osu = result.get(50);
        assertEquals("Ohio State", osu.getName());
        assertEquals("Buckeyes", osu.getNickname());
    }

    @Test
    void parseAndCreateTeamData_MissingRequiredHeader_ReturnsEmptyMap() throws IOException, CsvValidationException {
        // Missing TGID header entirely
        String csvContent = "TDNA,TMNA,TSNA\n" +
                "Ohio State,Buckeyes,OSU";

        MockMultipartFile file = new MockMultipartFile("data", "TEAMS.csv", "text/csv", csvContent.getBytes());

        Map<Integer, School> result = teamCsvParser.parseAndCreateTeamData(file);

        // Your code logs an error and returns an empty map if TGID is missing
        assertTrue(result.isEmpty(), "Should return empty map if required TGID header is missing");
    }

    @Test
    void parseAndCreateTeamData_BadNumericData_HandlesGracefully() throws IOException, CsvValidationException {
        // Prestige (TPRS) is "High" instead of a number
        String csvContent = "TGID,TDNA,TPRS\n" +
                "10,Texas,High";

        MockMultipartFile file = new MockMultipartFile("data", "TEAMS.csv", "text/csv", csvContent.getBytes());

        Map<Integer, School> result = teamCsvParser.parseAndCreateTeamData(file);

        School texas = result.get(10);
        assertNotNull(texas);
        assertEquals("Texas", texas.getName());
        assertEquals(0, texas.getPrestige(), "Should default to 0 on number format exception");
    }

    @Test
    void parseAndCreateTeamData_EmptyFile_ReturnsEmptyMap() throws IOException, CsvValidationException {
        MockMultipartFile file = new MockMultipartFile("data", "".getBytes());
        Map<Integer, School> result = teamCsvParser.parseAndCreateTeamData(file);
        assertTrue(result.isEmpty());
    }
}