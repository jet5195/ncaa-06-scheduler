package com.robotdebris.ncaaps2scheduler;
import com.robotdebris.ncaaps2scheduler.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelReader {
	
	@Autowired
	ConferenceList conferenceList;
	@Autowired
	SchoolList schoolList;
	@Autowired
	SeasonSchedule seasonSchedule;
	@Autowired
	SwapList swaplist;
    @Autowired
    List<Bowl> bowlList;
	
    private Workbook readExcel(String path) throws IOException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        return WorkbookFactory.create(new File(path));
        //workbook.close();
    }
    
    private Workbook readExcel(File file) throws IOException {
    	
    	if(file.toString().endsWith("csv")) {
    		return csvToXLSX(file);
    	}

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        return WorkbookFactory.create(file);
        //workbook.close();
    }
    
    public Workbook csvToXLSX(File file) {
        try {
            String csvFileAddress = file.getPath(); //csv file address
            String xlsxFileAddress = file.getPath() + ".xlsx"; //xlsx file address
            XSSFWorkbook workBook = new XSSFWorkbook();
            XSSFSheet sheet = workBook.createSheet("sheet1");
            String currentLine=null;
            int rowNum=0;
            BufferedReader br = new BufferedReader(new FileReader(csvFileAddress));
            while ((currentLine = br.readLine()) != null) {
                String str[] = currentLine.split(",");
                rowNum++;
                XSSFRow currentRow=sheet.createRow(rowNum);
                for(int i=0;i<str.length;i++){
                    currentRow.createCell(i).setCellValue(str[i]);
                }
            }

            FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
            br.close();
            return workBook;
        } catch (Exception ex) {
            System.out.println(ex.getMessage()+"Exception in try");
            return null;
        }
    }
    
    public ConferenceList getConferenceData(File file) throws IOException {
    	conferenceList.clear();
    	swaplist.clear();
    	Workbook workbook = readExcel(file);
    	Sheet sheet = workbook.getSheetAt(0);
    	DataFormatter dataFormatter = new DataFormatter();
    	int r = 0;
        for (Row row : sheet) {
            if (r > 0) {//disregard the headers
                String conferenceName = "";
                boolean powerConf = false;
                String division1 = "";
                String division2 = "";
                String logo = "";
                int confGames = 0;
                int startWeek = 0;
                if (!dataFormatter.formatCellValue(row.getCell(0)).equals("")) {
                    for (int i = 0; i <= 6; i++) {
                    	Cell cell = row.getCell(i);
                        String cellValue = dataFormatter.formatCellValue(cell);
                        switch (i) {
                            case 0:
                            	conferenceName = cellValue;
                                break;
                            case 1:
                                logo = cellValue;
                                break;
                            case 2:
                            	if(!isStringBlank(cellValue)) {
                            		powerConf = Boolean.parseBoolean(cellValue);
                            	}
                                break;
                            case 3:
                                if(!isStringBlank(cellValue)) {
                                    confGames = Integer.parseInt(cellValue);
                                }
                                break;
                            case 4:
                                if(!isStringBlank(cellValue)) {
                                    startWeek = Integer.parseInt(cellValue);
                                }
                                break;
                            case 5:
                                division1 = cellValue;
                                break;
                            case 6:
                                division2 = cellValue;
                                break;
                            default:
                                break;
                        }//end of switch
                    }//end col iterator
                    conferenceList.add(new Conference(conferenceName, powerConf, division1, division2, logo, confGames, startWeek));
                }//end of if not null
            }//end of row iterator
            r++;
        }
    	return conferenceList;
    }
    
    //String.isBlank doesn't work on older versions of java?
    private boolean isStringBlank(String string) {
    	if(string == null || string.equals("") || string.equals(" ")) {
    		return true;
    	}
    	else return false;
    }
    
    public void setAlignmentData(File file, SchoolList allSchools, ConferenceList conferenceList) throws IOException {
    	Workbook workbook = readExcel(file);
    	Sheet sheet = workbook.getSheetAt(1);
    	DataFormatter dataFormatter = new DataFormatter();
    	
    	int r = 0;
        for (Row row : sheet) {
            if (r > 0) {//disregard the headers
                int tgid = 0;
                String schoolName = "";
                String conf = "";
                String div = "";
                String xDivRival = "";
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
                            case 1:
                            	schoolName = cellValue;
                            	break;
                            case 2:
                                conf = cellValue;
                                break;
                            case 3:
                                div = cellValue;
                                break;
                            case 4:
                                xDivRival = cellValue;
                                break;
                            case 5:
                                ncaaDiv = cellValue;
                                break;
                            default:
                                break;
                        }//end of switch
                        c++;
                    }//end col iterator
                    School school = allSchools.schoolSearch(tgid);
                    if(school != null) {
	                    School xDivRivalSchool = allSchools.schoolSearch(xDivRival);
	                    Conference conference = conferenceList.conferenceSearch(conf);
	                    school.updateAlignment(conference, div, ncaaDiv, xDivRivalSchool);
                    } else {
                    	System.out.println("Failed at schoolName = " + schoolName + ". TGID " + tgid + " was not found.");
                    }
                }//end of if not null
            }//end of row iterator
            r++;
        }
        Conference blankConference = new Conference("null", false, null, null, null, 0, 0);
        for (School school: schoolList) {
            if(school.getConference() == null ) {
                school.updateAlignment(blankConference, null, "fcs", null);
            }
        }
    }

    /**
     * Populates a school list of all schools in your excel file
     * @param path the path of your custom conferences excel file
     * @return SchoolList of all schools in your excel file
     * @throws IOException
     */
    public SchoolList getSchoolData(String path) throws IOException {
        Workbook workbook = readExcel(path);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        //SchoolList schoolList = new SchoolList();
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
                    schoolList.add(new School(tgid, university, nickname, state, color, altColor, logo));
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
                        School rival = schoolList.schoolSearch(cellValue);
                        if (rival != null) {
                            rivals.add(rival);
                        }
                    }
                    i++;
                }
                schoolList.schoolSearch(tgid).setRivals(rivals);
            }
            iterator++;
        }
        return schoolList;
    }

    /**
     * Populates the season's schedule as well as all school's schedules. Also adds FCS schools to the school list as they are found.
     * @param file the path of the Schedule excel file
     * @param allSchools the list of all schools
     * @return SeasonSchedule a list of all games in a season
     * @throws IOException
     */
    public SeasonSchedule getScheduleData(File file, SchoolList allSchools) throws IOException, NumberFormatException {
    	//if the first school has a schedule already, empty it.
    	//eventually it may make sense to autowire seasonschedule and check it
        seasonSchedule = new SeasonSchedule();
//    	if(!allSchools.get(0).getSchedule().isEmpty()) {
    		allSchools.resetAllSchoolsSchedules();
//    	}
        School bowlSchool = new School(511, "Bowl", "Bowl", "Bowl", "Bowl", "Bowl", "Bowl");
        Workbook workbook = readExcel(file);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        /*System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");*/
        //SeasonSchedule seasonSchedule = new SeasonSchedule();
        SeasonSchedule bowlSchedule = new SeasonSchedule();
        int r = 0;
        for (Row row : sheet) {
        	int gasc = 0;//away score 1
        	int ghsc = 0;//home score 2
            int gtod = 0;//time of day 3
            int gatg = 0;//away team tgid 4
            School awaySchool;
            int ghtg = 0;//home team tgid 5
            School homeSchool;
            int sgnm = 0;//game of week number 6
            int sewn = 0;//week number 7, sometimes sewt
            int gdat = 0;//day of week 8
            int gfot = 0;//game went into OT? 9
            int sewt = 0;//game weight?? 10
            int gffu = 0;//user game 11 12
            int gmfx = 0;//conference game 13

            int c = 0;
            if (r > 0) {
                for (Cell cell : row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    //System.out.print(cellValue + "\t");
                    switch (c) {
                    
                    	case 1:
                    		gasc = Integer.parseInt(cellValue);
                    		break;
                    	case 2:
                    		ghsc = Integer.parseInt(cellValue);
                    		break;
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
                        case 9:
                        	gfot = Integer.parseInt(cellValue);
                        	break;
                        case 10:
                        	sewt = Integer.parseInt(cellValue);
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
                        awaySchool = new School(gatg, "null", "null", "null", new Conference("null", false, null, null, null, 0,0), null, "FCS", "null", "null", "null");
                        awaySchool.setRivals(new SchoolList());
                        allSchools.add(awaySchool);
                    }
                    if (homeSchool == null) {
                        homeSchool = new School(ghtg, "null", "null", "null", new Conference("null", false, null, null, null, 0, 0), null, "FCS", "null", "null", "null");
                        homeSchool.setRivals(new SchoolList());
                        allSchools.add(homeSchool);
                    }
                    GameResult gameResult = new GameResult(gasc, ghsc, gfot);
                    Game newGame = new Game(gameResult, gtod, awaySchool, homeSchool, sgnm, sewn, gdat, gffu, gmfx);
                    awaySchool.addGame(newGame);
                    homeSchool.addGame(newGame);
                    seasonSchedule.add(newGame);
                } else {
                	GameResult gameResult = new GameResult(gasc, ghsc, gfot);
                    bowlSchedule.add(new Game(gameResult, gtod, bowlSchool, bowlSchool, sgnm, sewn, gdat, gffu, gmfx));
                }
            }
            r++;
        }

        allSchools.populateUserSchools();
        seasonSchedule.setBowlSchedule(bowlSchedule);
        return seasonSchedule;
    }

    /**
     * Populates the the bowl schedule
     * @param file the path of the bowl excel file
     * @return bowlList a list of all games in a season
     * @throws IOException
     */
    public List<Bowl> getBowlData(File file) throws IOException, NumberFormatException {

        bowlList = new ArrayList<>();
        Workbook workbook = readExcel(file);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        int r = 0;
        for (Row row : sheet) {
            int bci1 = 0;//conference 1 id 1
            int bcr1 = 0;//conference 1 rank 2
            int bci2 = 0;//conference 2 id 3
            int bcr2 = 0;//conference 2 rank 4
            int bmfd = 0;//? 5
            int sgid = 0;//stadium id 6
            int utid = 0;//trophy id 7
            int gtod = 0;//time of day 8
            String bnme = "";//bowl name 9
            int sgnm = 0;//game of week number 10
            int bmon = 0;//bowl month 11
            int sewn = 0;//week number 12
            int blgo = 0;//bowl logo 13
            int bplo = 0;//? 14
            int bidx = 0;//bowl index 15
            int bday = 0;//bowl day 16

            int c = 0;
            if (r > 0) {
                for (Cell cell : row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    //System.out.print(cellValue + "\t");
                    switch (c) {

                        case 0:
                            bci1 = Integer.parseInt(cellValue);
                            break;
                        case 1:
                            bcr1 = Integer.parseInt(cellValue);
                            break;
                        case 2:
                            bci2 = Integer.parseInt(cellValue);
                            break;
                        case 3:
                            bcr2 = Integer.parseInt(cellValue);
                            break;
                        case 4:
                            bmfd = Integer.parseInt(cellValue);
                            break;
                        case 5:
                            sgid = Integer.parseInt(cellValue);
                            break;
                        case 6:
                            utid = Integer.parseInt(cellValue);
                            break;
                        case 7:
                            gtod = Integer.parseInt(cellValue);
                            break;
                        case 8:
                            bnme = cellValue;
                            break;
                        case 9:
                            sgnm = Integer.parseInt(cellValue);
                            break;
                        case 10:
                            bmon = Integer.parseInt(cellValue);
                            break;
                        case 11:
                            sewn = Integer.parseInt(cellValue);
                            break;
                        case 12:
                            blgo = Integer.parseInt(cellValue);
                            break;
                        case 13:
                            bplo = Integer.parseInt(cellValue);
                            break;
                        case 14:
                            bidx = Integer.parseInt(cellValue);
                            break;
                        case 15:
                            bday = Integer.parseInt(cellValue);
                            break;
                        default:
                            break;
                    }
                    c++;
                }
            }
            Bowl newBowl = new Bowl(bci1, bcr1, bci2, bcr2, bmfd, sgid, utid, gtod, bnme, sgnm, bmon, sewn, blgo, bplo, bidx, bday);
            bowlList.add(newBowl);
            r++;
        }
        return bowlList;
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}