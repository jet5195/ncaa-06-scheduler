package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.School;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class XlsxExportService {

    public ResponseEntity<ByteArrayResource> writeSchoolData(List<School> schoolList) {
        Workbook workbook = new XSSFWorkbook();
        // Write the output to a byte array
        writeSchoolDataSheet(schoolList, workbook);
        return createResonseEntity(workbook, "SchoolData");
    }

    private void writeSchoolDataSheet(List<School> schoolList, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Schools");
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("TGID");
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("School");
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Nickname");
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("State");
        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Color");
        headerCell = headerRow.createCell(5);
        headerCell.setCellValue("Alt Color");
        headerCell = headerRow.createCell(6);
        headerCell.setCellValue("Logo");
        headerCell = headerRow.createCell(7);
        headerCell.setCellValue("Rival 1");
        headerCell = headerRow.createCell(8);
        headerCell.setCellValue("Rival 2");
        headerCell = headerRow.createCell(9);
        headerCell.setCellValue("Rival 3");
        headerCell = headerRow.createCell(10);
        headerCell.setCellValue("Rival 4");
        headerCell = headerRow.createCell(11);
        headerCell.setCellValue("Rival 5");
        headerCell = headerRow.createCell(12);
        headerCell.setCellValue("Rival 6");
        headerCell = headerRow.createCell(13);
        headerCell.setCellValue("Rival 7");
        headerCell = headerRow.createCell(14);
        headerCell.setCellValue("Rival 8");

        headerCell = headerRow.createCell(15);
        headerCell.setCellValue("Latitude");
        headerCell = headerRow.createCell(16);
        headerCell.setCellValue("Longitude");
        headerCell = headerRow.createCell(17);
        headerCell.setCellValue("Abbreviation");
        headerCell = headerRow.createCell(18);
        headerCell.setCellValue("Stadium Name");
        headerCell = headerRow.createCell(19);
        headerCell.setCellValue("City");
        headerCell = headerRow.createCell(20);
        headerCell.setCellValue("Stadium Capacity");

        // Fill data
        int rowNum = 1;
        for (School school : schoolList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(school.getTgid());
            row.createCell(1).setCellValue(school.getName());
            row.createCell(2).setCellValue(school.getNickname());
            row.createCell(3).setCellValue(school.getState());
            row.createCell(4).setCellValue(school.getColor());
            row.createCell(5).setCellValue(school.getAltColor());
            row.createCell(6).setCellValue(school.getLogo());
            // Set rival information
            if (school.getRivals() != null) {
                for (int i = 0; i < 8; i++) {
                    if (i < school.getRivals().size()) {
                        row.createCell(7 + i).setCellValue(school.getRivals().get(i).getName());
                    } else {
                        row.createCell(7 + i).setCellValue(""); // If no rival, set empty string
                    }
                }
            }

            // Fill new attributes
            row.createCell(15).setCellValue(school.getLatitude());
            row.createCell(16).setCellValue(school.getLongitude());
            row.createCell(17).setCellValue(school.getAbbreviation());
            row.createCell(18).setCellValue(school.getStadiumName());
            row.createCell(19).setCellValue(school.getCity());
            row.createCell(20).setCellValue(school.getStadiumCapacity());

        }
    }

    private ResponseEntity<ByteArrayResource> createResonseEntity(Workbook workbook, String fileName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions properly
        }

        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    public ResponseEntity<ByteArrayResource> writeConferenceAlignment(List<Conference> conferenceList,
                                                                      List<School> schoolList) {
        Workbook workbook = new XSSFWorkbook();
        writeConferencesSheet(conferenceList, workbook);
        List<Division> divisionList = conferenceList.stream().map(Conference::getDivisions).flatMap(List::stream)
                .toList();
        writeDivisionsSheet(divisionList, workbook);
        writeAlignmentSheet(schoolList, workbook);

        return createResonseEntity(workbook, "ConferenceAlignment");
    }

    private static void writeConferencesSheet(List<Conference> conferenceList, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Conferences");
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Conference ID");
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Conference Name");
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Short Name");
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Abbreviation");
        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Classification");
        headerCell = headerRow.createCell(5);
        headerCell.setCellValue("Power Conference");
        headerCell = headerRow.createCell(6);
        headerCell.setCellValue("Conf Games");
        headerCell = headerRow.createCell(7);
        headerCell.setCellValue("Start Week");
        headerCell = headerRow.createCell(8);
        headerCell.setCellValue("Logo");

        // Fill data
        int rowNum = 1;
        for (Conference conference : conferenceList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(conference.getConferenceId());
            row.createCell(1).setCellValue(conference.getName());
            row.createCell(2).setCellValue(conference.getShortName());
            row.createCell(3).setCellValue(conference.getAbbreviation());
            row.createCell(4).setCellValue(conference.getClassification().toString());
            row.createCell(5).setCellValue(conference.isPowerConf());
            row.createCell(6).setCellValue(conference.getNumOfConfGames());
            row.createCell(7).setCellValue(conference.getConfGamesStartWeek());
            row.createCell(8).setCellValue(conference.getLogo());
            // if (conference.getDivisions() != null && conference.getDivisions().size() ==
            // 2) {
            // row.createCell(5).setCellValue(conference.getDivisions().get(0));
            // row.createCell(6).setCellValue(conference.getDivisions().get(1));
            // }
        }
    }

    private static void writeDivisionsSheet(List<Division> divisionList, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Divisions");
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Division ID");
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Conference ID");
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Name");
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Short Name");

        // Fill data
        int rowNum = 1;
        for (Division division : divisionList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(division.getDivisionId());
            row.createCell(1).setCellValue(division.getConference().getConferenceId());
            row.createCell(2).setCellValue(division.getName());
            row.createCell(3).setCellValue(division.getShortName());
        }
    }

    private void writeAlignmentSheet(List<School> schoolList, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Alignment");

        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("TGID");
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("School");
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Conference");
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Division");
        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("X-Div Rival");

        int rowNum = 1;
        for (School school : schoolList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(school.getTgid());
            row.createCell(1).setCellValue(school.getName());
            row.createCell(2).setCellValue(school.getConference().getShortName());
            if (school.getDivision() != null) {
                row.createCell(3).setCellValue(school.getDivision().getName());
            }
            if (school.getXDivRival() != null) {
                row.createCell(4).setCellValue(school.getXDivRival().getName());
            }
        }
    }
}
