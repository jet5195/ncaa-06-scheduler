package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.School;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class XlsxExportService {

    private static void writeConferencesSheet(List<Conference> conferenceList, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Conferences");
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Conference Name");
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Logo");
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Power Conference");
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Conf Games");
        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Start Week");
        headerCell = headerRow.createCell(5);
        headerCell.setCellValue("Division");
        headerCell = headerRow.createCell(6);
        headerCell.setCellValue("Division");

        // Fill data
        int rowNum = 1;
        for (Conference conference : conferenceList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(conference.getName());
            row.createCell(1).setCellValue(conference.getLogo());
            row.createCell(2).setCellValue(conference.isPowerConf());
            row.createCell(3).setCellValue(conference.getNumOfConfGames());
            row.createCell(4).setCellValue(conference.getConfGamesStartWeek());
            if (conference.getDivisions() != null && conference.getDivisions().size() == 2) {
                row.createCell(5).setCellValue(conference.getDivisions().get(0));
                row.createCell(6).setCellValue(conference.getDivisions().get(1));
            }
        }
    }

    public ResponseEntity<Resource> writeConferenceAlignment(List<Conference> conferenceList, List<School> schoolList) {
        Workbook workbook = new XSSFWorkbook();
        writeConferencesSheet(conferenceList, workbook);
        writeAlignmentSheet(schoolList, workbook);

        // Write the output to a file
        try (FileOutputStream outputStream = new FileOutputStream("ConferenceAlignment.xlsx")) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions properly
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ConferenceAlignment.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
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
        headerCell = headerRow.createCell(5);
        headerCell.setCellValue("NcaaDivision");

        int rowNum = 1;
        for (School school : schoolList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(school.getTgid());
            row.createCell(1).setCellValue(school.getName());
            row.createCell(2).setCellValue(school.getConferenceName());
            row.createCell(3).setCellValue(school.getDivision());
            if (school.getxDivRival() != null) {
                row.createCell(4).setCellValue(school.getxDivRival().getName());
            }
            if (school.getNcaaDivision() != null) {
                row.createCell(5).setCellValue(school.getNcaaDivision().toString());
            }
        }
    }
}
