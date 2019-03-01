
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExcelReader {
    public static final String SAMPLE_XLSX_FILE_PATH = "src/main/resources/SCHED.xlsx";

    private static Workbook readExcel(String path) throws IOException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(path));

        // 2. Or you can use a for-each loop
        System.out.println("Retrieving Sheets using for-each loop");
        for(Sheet sheet: workbook) {
            System.out.println("=> " + sheet.getSheetName());
        }
        // Closing the workbook
        //workbook.close();
        return workbook;
    }

    public static SchoolList getSchoolData(String path) throws IOException {
        Workbook workbook = readExcel(path);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
        SchoolList allSchools = new SchoolList();
        int r = 0;
        for (Row row: sheet) {
            if(r>0) {//disregard the headers
                int tgid = 0;
                String university = "";
                String nickname = "";
                String state = "";
                String conf = "";
                String div = "";
                int c = 0;
                if(dataFormatter.formatCellValue(row.getCell(0))!="") {
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        System.out.print(cellValue + "\t");
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
                                conf = cellValue;
                            case 5:
                                div = cellValue;
                            default:
                                break;
                        }//end of switch
                        c++;
                    }//end col iterator
                    allSchools.add(new School(tgid, university, nickname, state, conf, div));
                    System.out.println();
                }//end of if not null
            }//end of row iterator
            r++;
        }
        int iterator = 0;
        for(Row row: sheet) {
            SchoolList rivals = new SchoolList();
            if (iterator != 0 && dataFormatter.formatCellValue(row.getCell(0))!="") {
                int i = 0;
                for (Cell cell: row) {
                    if (i>=6 && dataFormatter.formatCellValue(cell)!="") {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        School rival = allSchools.schoolSearch(cellValue);
                        if (rival!=null){
                            rivals.add(rival);
                        }
                    }
                    i++;
                }
                allSchools.get(iterator-1).setRivals(rivals);
            }
            iterator++;
        }
        return allSchools;
    }

    public static Schedule getScheduleData(String path, SchoolList allSchools) throws IOException {

        School nullSchool = new School(999, "null", "null", "null", "null", "null");


        Workbook workbook = readExcel(path);
        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        //LinkedList<School> schools = new LinkedList<School>();
        // 2. Or you can use a for-each loop to iterate over the rows and columns
        System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
        Schedule theSchedule = new Schedule();
        int r = 0;
        for (Row row: sheet) {
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
            if (r>0) {
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
                awaySchool = allSchools.schoolSearch(gatg);
                homeSchool = allSchools.schoolSearch(ghtg);

                //System.out.println();
                Game newGame = new Game(gtod, awaySchool, homeSchool, sgnm, sewn, gdat, gffu, gmfx);
                awaySchool.addGame(newGame);
                homeSchool.addGame(newGame);
                theSchedule.add(newGame);
            }
            r++;
        }

        allSchools.populateUserSchools();
        return theSchedule;
    }

    public static void addGame(){
        //code to add a game to the spreadsheet
    }

    public static void removeGame(){
        //code to remove game from the spreadsheet
    }

    public static void write(Schedule theSchedule) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        ArrayList<ArrayList> list = theSchedule.scheduleToList();
        for (int i = 0; i < list.size(); i++) {
            addLine(sheet, list.get(i), i);
        }
        FileOutputStream fileOut = new FileOutputStream("output.xlsx");
        workbook.write(fileOut);
        fileOut.close();
    }

    private static void addLine(Sheet sheet, ArrayList game, int r){
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