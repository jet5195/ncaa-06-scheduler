package com.robotdebris.ncaaps2scheduler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.ConferenceList;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolList;
import com.robotdebris.ncaaps2scheduler.model.SeasonSchedule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExcelReader {
    private static Workbook readExcel(String path) throws IOException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        return WorkbookFactory.create(new File(path));
        //workbook.close();
    }
    
    private static Workbook readExcel(File file) throws IOException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        return WorkbookFactory.create(file);
        //workbook.close();
    }
    
    public static ConferenceList getConferenceData(File file) throws IOException {
    	//Workbook workbook = readExcel(path);
    	Workbook workbook = readExcel(file);
    	Sheet sheet = workbook.getSheetAt(0);
    	DataFormatter dataFormatter = new DataFormatter();
    	ConferenceList conferenceList = new ConferenceList();
    	int r = 0;
        for (Row row : sheet) {
            if (r > 0) {//disregard the headers
                String conferenceName = "";
                boolean powerConf = false;
                String division1 = "";
                String division2 = "";
                String color = "";
                String altColor = "";
                String logo = "";
                int c = 0;
                if (dataFormatter.formatCellValue(row.getCell(0)) != "") {
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        //System.out.print(cellValue + "\t");
                        switch (c) {
                            case 0:
                            	conferenceName = cellValue;
                                break;
                            case 1:
                            	if(cellValue != null) {
                            		powerConf = Boolean.parseBoolean(cellValue);
                            	}
                                break;
                            case 2:
                                division1 = cellValue;
                                break;
                            case 3:
                                division2 = cellValue;
                                break;
                            case 4:
                                color = cellValue;
                                break;
                            case 5:
                                altColor = cellValue;
                            case 6:
                                logo = cellValue;
                            default:
                                break;
                        }//end of switch
                        c++;
                    }//end col iterator
                    conferenceList.add(new Conference(conferenceName, powerConf, division1, division2, color, altColor, logo));
                    //System.out.println();
                }//end of if not null
            }//end of row iterator
            r++;
        }
    	return conferenceList;
    }
    
    public static void setAlignmentData(File file, SchoolList allSchools, ConferenceList conferenceList) throws IOException {
    	Workbook workbook = readExcel(file);
    	Sheet sheet = workbook.getSheetAt(1);
    	DataFormatter dataFormatter = new DataFormatter();
    	
    	int r = 0;
        for (Row row : sheet) {
            if (r > 0) {//disregard the headers
                int tgid = 0;
                String university = "";
                String conf = "";
                String div = "";
                String ncaaDiv = "";
                int c = 0;
                if (dataFormatter.formatCellValue(row.getCell(0)) != "") {
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        //System.out.print(cellValue + "\t");
                        switch (c) {
                            case 0:
                                tgid = Integer.parseInt(cellValue);
                                break;
                            case 2:
                                conf = cellValue;
                                break;
                            case 3:
                                div = cellValue;
                                break;
                            case 4:
                                ncaaDiv = cellValue;
                                break;
                            default:
                                break;
                        }//end of switch
                        c++;
                    }//end col iterator
                    School school = allSchools.schoolSearch(tgid);
                    Conference conference = conferenceList.conferenceSearch(conf);
                    school.updateAlignment(conference, div, ncaaDiv);
                    //System.out.println();
                }//end of if not null
            }//end of row iterator
            r++;
        }
    }

    /**
     * Populates a school list of all schools in your excel file
     * @param path the path of your custom conferences excel file
     * @return SchoolList of all schools in your excel file
     * @throws IOException
     */
    public static SchoolList getSchoolData(String path) throws IOException {
        Workbook workbook = readExcel(path);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        SchoolList allSchools = new SchoolList();
        int r = 0;
        for (Row row : sheet) {
            if (r > 0) {//disregard the headers
                int tgid = 0;
                String university = "";
                String nickname = "";
                String state = "";
                String color = "";
                String altColor = "";
                String logo = "";
                int c = 0;
                if (dataFormatter.formatCellValue(row.getCell(0)) != "") {
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        //System.out.print(cellValue + "\t");
                        switch (c) {
                            case 0:
                                tgid = Integer.parseInt(cellValue);
                                break;
                            case 1:
                                university = cellValue;
                                break;
                            case 2:
                                nickname = cellValue;
                                break;
                            case 3:
                                state = cellValue;
                                break;
                            case 4:
                                color = cellValue;
                                break;
                            case 5:
                                altColor = cellValue;
                                break;
                            case 6:
                                logo = cellValue;
                                break;
                            default:
                                break;
                        }//end of switch
                        c++;
                    }//end col iterator
                    allSchools.add(new School(tgid, university, nickname, state, color, altColor, logo));
                    //System.out.println();
                }//end of if not null
            }//end of row iterator
            r++;
        }
        int iterator = 0;
        for (Row row : sheet) {
            SchoolList rivals = new SchoolList();
            if (iterator != 0 && dataFormatter.formatCellValue(row.getCell(0)) != "") {
                int i = 0;
                int tgid = 0;
                for (Cell cell : row) {
                	if(i == 0) {
                		String cellValue = dataFormatter.formatCellValue(cell);
                		tgid = Integer.parseInt(cellValue);
                	}
                    if (i >= 6 && dataFormatter.formatCellValue(cell) != "") {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        School rival = allSchools.schoolSearch(cellValue);
                        if (rival != null) {
                            rivals.add(rival);
                        }
                    }
                    i++;
                }
                allSchools.schoolSearch(tgid).setRivals(rivals);
            }
            iterator++;
        }
        return allSchools;
    }

    /**
     * Populates the season's schedule as well as all school's schedules. Also adds FCS schools to the school list as they are found.
     * @param path the path of the Schedule excel file
     * @param allSchools the list of all schools
     * @return SeasonSchedule a list of all games in a season
     * @throws IOException
     */
    public static SeasonSchedule getScheduleData(File file, SchoolList allSchools) throws IOException {
    	//if the first school has a schedule already, empty it.
    	//eventually it may make sense to autowire seasonschedule and check it
    	if(!allSchools.get(0).getSchedule().isEmpty()) {
    		allSchools.resetAllSchoolsSchedules();
    	}
        School bowlSchool = new School(511, "Bowl", "Bowl", "Bowl", "Bowl", "Bowl", "Bowl");
        Workbook workbook = readExcel(file);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        /*System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");*/
        SeasonSchedule seasonSchedule = new SeasonSchedule();
        SeasonSchedule bowlSchedule = new SeasonSchedule();
        int r = 0;
        for (Row row : sheet) {
            int gtod = 0;//time of day 3
            int gatg = 0;//away team tgid 4
            School awaySchool;
            int ghtg = 0;//home team tgid 5
            School homeSchool;
            int sgnm = 0;//game of week number 6
            int sewn = 0;//week number, also sewt 7 10
            int gdat = 0;//day of week 8
            int gffu = 0;//user game 11 12
            int gmfx = 0;//conference game 13

            int c = 0;
            if (r > 0) {
                for (Cell cell : row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    //System.out.print(cellValue + "\t");
                    switch (c) {
                        case 3:
                            gtod = Integer.parseInt(cellValue);
                            break;
                        case 4:
                            gatg = Integer.parseInt(cellValue);
                            break;
                        case 5:
                            ghtg = Integer.parseInt(cellValue);
                            break;
                        case 6:
                            sgnm = Integer.parseInt(cellValue);
                            break;
                        case 7:
                            sewn = Integer.parseInt(cellValue);
                            break;
                        case 8:
                            gdat = Integer.parseInt(cellValue);
                            break;
                        case 11:
                            gffu = Integer.parseInt(cellValue);
                            break;
                        case 13:
                            gmfx = Integer.parseInt(cellValue);
                            break;
                        default:
                            break;
                    }
                    c++;
                }
                
                if (ghtg != 511) {//don't add bowl games
                    awaySchool = allSchools.schoolSearch(gatg);
                    homeSchool = allSchools.schoolSearch(ghtg);
                    if (awaySchool == null) {
                        awaySchool = new School(gatg, "null", "null", "null", new Conference("null", false, null, null, null, null, null), null, "FCS", "null", "null", "null");
                        awaySchool.setRivals(new SchoolList());
                        allSchools.add(awaySchool);
                    }
                    if (homeSchool == null) {
                        homeSchool = new School(ghtg, "null", "null", "null", new Conference("null", false, null, null, null, null, null), null, "FCS", "null", "null", "null");
                        homeSchool.setRivals(new SchoolList());
                        allSchools.add(homeSchool);
                    }
                    Game newGame = new Game(gtod, awaySchool, homeSchool, sgnm, sewn, gdat, gffu, gmfx);
                    awaySchool.addGame(newGame);
                    homeSchool.addGame(newGame);
                    seasonSchedule.add(newGame);
                } else {
                    bowlSchedule.add(new Game(gtod, bowlSchool, bowlSchool, sgnm, sewn, gdat, gffu, gmfx));
                }
            }
            r++;
        }

        allSchools.populateUserSchools();
        seasonSchedule.setBowlSchedule(bowlSchedule);
        return seasonSchedule;
    }

    /**
     * Writes schedule to a new excel file
     * @param seasonSchedule the schedule to write to a new excel file
     * @throws IOException
     */
    public static ByteArrayInputStream write(SeasonSchedule seasonSchedule) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        ArrayList<ArrayList> list = seasonSchedule.scheduleToList(true);
        int i = 0;
        while (i < list.size()) {
            addLine(sheet, list.get(i), i);
            i++;
        }
        ArrayList<ArrayList> bowlList = seasonSchedule.getBowlSchedule().scheduleToList(false);
        for (int j = 0; j < bowlList.size(); j++) {
            addLine(sheet, bowlList.get(j), i);
            i++;
        }
        sheet.getRow(0).createCell(14).setCellValue(String.valueOf(i - 1));
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
//        fileOut.close();
    }

    private static void addLine(Sheet sheet, ArrayList game, int r) {
        Row row = sheet.createRow(r);
        for (int c = 0; c < game.size(); c++) {
            if (r == 0) {
                row.createCell(c).setCellValue((String) game.get(c));
            } else {
                row.createCell(c).setCellValue((Integer) game.get(c));
            }

        }
    }
}