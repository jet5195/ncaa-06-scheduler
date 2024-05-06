package com.robotdebris.ncaaps2scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.model.Bowl;
import com.robotdebris.ncaaps2scheduler.model.Conference;
import com.robotdebris.ncaaps2scheduler.model.DayOfWeek;
import com.robotdebris.ncaaps2scheduler.model.Division;
import com.robotdebris.ncaaps2scheduler.model.Game;
import com.robotdebris.ncaaps2scheduler.model.GameResult;
import com.robotdebris.ncaaps2scheduler.model.NCAADivision;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.service.CollegeFootballDataService;
import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.DivisionService;
import com.robotdebris.ncaaps2scheduler.service.SchoolService;
import com.robotdebris.ncaaps2scheduler.service.SwapService;

import jakarta.annotation.PostConstruct;

@Component
public class ExcelReader {

	@Autowired
	ConferenceService conferenceService;
	@Autowired
	SchoolService schoolService;
	@Autowired
	List<Bowl> bowlList;
	@Autowired
	SwapService swapService;
	@Autowired
	CollegeFootballDataService dataService;

	private final Logger LOGGER = Logger.getLogger(ExcelReader.class.getName());
	@Autowired
	private DivisionService divisionService;

	@PostConstruct
	public void init() {

		// final String schoolsFile = "src/main/resources/School_Data.xlsx";
		final String schoolsFile = "resources/app/School_Data.xlsx";

		try {
			List<School> schoolList = populateSchoolsFromExcel(schoolsFile);
			Collections.sort(schoolList);
			schoolService.setSchoolList(schoolList);
			populateRivalsFromExcel(schoolsFile);
			dataService.loadSchoolData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Workbook readExcel(String path) throws IOException {

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		return WorkbookFactory.create(new File(path));
		// workbook.close();
	}

	private Workbook readExcel(File file) throws IOException {

		if (file.toString().endsWith("csv")) {
			return convertCsvToXLSX(file);
		}

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		return WorkbookFactory.create(file);
		// workbook.close();
	}

	public Workbook convertCsvToXLSX(File file) {
		try {
			String csvFileAddress = file.getPath(); // csv file address
			String xlsxFileAddress = file.getPath() + ".xlsx"; // xlsx file address
			XSSFWorkbook workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("sheet1");
			String currentLine = null;
			int rowNum = 0;
			BufferedReader br = new BufferedReader(new FileReader(csvFileAddress));
			while ((currentLine = br.readLine()) != null) {
				String str[] = currentLine.split(",");
				rowNum++;
				XSSFRow currentRow = sheet.createRow(rowNum);
				for (int i = 0; i < str.length; i++) {
					currentRow.createCell(i).setCellValue(str[i]);
				}
			}

			FileOutputStream fileOutputStream = new FileOutputStream(xlsxFileAddress);
			workBook.write(fileOutputStream);
			fileOutputStream.close();
			br.close();
			return workBook;
		} catch (Exception ex) {
			System.out.println(ex.getMessage() + "Exception in try");
			return null;
		}
	}

	public List<Conference> populateConferencesFromExcel(File file) throws IOException {
		List<Conference> conferences = new ArrayList<>();
		Workbook workbook = readExcel(file);
		Sheet sheet = workbook.getSheet("Conferences");
		DataFormatter dataFormatter = new DataFormatter();
		int r = 0;
		for (Row row : sheet) {
			if (r > 0) {// disregard the headers
				Integer conferenceId = null;
				String conferenceName = "";
				String conferenceShortName = "";
				String abbreviation = "";
				NCAADivision classification = NCAADivision.FCS;
				boolean powerConf = false;
				String logo = "";
				int confGames = 0;
				int startWeek = 0;
				if (!dataFormatter.formatCellValue(row.getCell(0)).isEmpty()) {
					for (int i = 0; i <= 8; i++) {
						Cell cell = row.getCell(i);
						String cellValue = dataFormatter.formatCellValue(cell);
						switch (i) {
						case 0 -> conferenceId = Integer.parseInt(cellValue);
						case 1 -> conferenceName = cellValue;
						case 2 -> {
							if (!isStringBlank(cellValue)) {
								conferenceShortName = cellValue;
							}
						}
						case 3 -> {
							if (!isStringBlank(cellValue)) {
								abbreviation = cellValue;
							}
						}
						case 4 -> {
							if (!isStringBlank(cellValue)) {
								classification = NCAADivision.valueOf(cellValue);
							} else {
								classification = NCAADivision.FCS;
							}
						}
						case 5 -> {
							powerConf = cell.getBooleanCellValue();
						}
						case 6 -> {
							if (!isStringBlank(cellValue)) {
								try {
									confGames = Integer.parseInt(cellValue);
								} catch (Exception e) {
									confGames = 0;
									LOGGER.warn("No integer found for " + conferenceShortName
											+ " confGames. Defaulting to 0");
								}
							}
						}
						case 7 -> {
							if (!isStringBlank(cellValue)) {
								try {
									startWeek = Integer.parseInt(cellValue);
								} catch (Exception e) {
									startWeek = 0;
									LOGGER.warn("No integer found for " + conferenceShortName
											+ " startWeek. Defaulting to 0");
								}
							}
						}
						case 8 -> {
							if (!isStringBlank(cellValue)) {
								logo = cellValue;
							}
						}
						default -> {
						}
						}// end of switch
					} // end col iterator
					conferences.add(new Conference(conferenceId, conferenceName, conferenceShortName, abbreviation,
							classification, powerConf, confGames, startWeek, logo));

				} // end of if not null
			} // end of row iterator
			r++;
		}
		return conferences;
	}

	// String.isBlank doesn't work on older versions of java?
	private boolean isStringBlank(String string) {
		if (string == null || string.equals("") || string.equals(" ")) {
			return true;
		} else
			return false;
	}

	public void populateAlignmentFromExcel(File file) throws IOException {
		Workbook workbook = readExcel(file);
		Sheet sheet = workbook.getSheet("Alignment");
		DataFormatter dataFormatter = new DataFormatter();

		int r = 0;
		for (Row row : sheet) {
			if (r > 0) {// disregard the headers
				int tgid = 0;
				String schoolName = "";
				String conferenceShortName = "";
				String divisionName = "";
				String xDivRival = "";
				int c = 0;
				if (dataFormatter.formatCellValue(row.getCell(0)) != "") {
					for (Cell cell : row) {
						String cellValue = dataFormatter.formatCellValue(cell);
						// System.out.print(cellValue + "\t");
						switch (c) {
						case 0 -> tgid = Integer.parseInt(cellValue);
						case 1 -> schoolName = cellValue;
						case 2 -> conferenceShortName = cellValue;
						case 3 -> divisionName = cellValue;
						case 4 -> xDivRival = cellValue;
						default -> {
						}
						}// end of switch
						c++;
					} // end col iterator
					School school = schoolService.schoolSearch(tgid);
					if (school != null) {
						School xDivRivalSchool = null;
						if (!xDivRival.isEmpty()) {
							xDivRivalSchool = schoolService.schoolSearch(xDivRival);
						}
						Conference conference = conferenceService.findByShortName(conferenceShortName);
						Division division = divisionService.findByName(divisionName);
						school.updateAlignment(conference, division, xDivRivalSchool);
					} else {
						System.out
								.println("Failed at schoolName = " + schoolName + ". TGID " + tgid + " was not found.");
					}
				} // end of if not null
			} // end of row iterator
			r++;
		}
		Conference blankConference = Conference.blankConference;
		for (School school : schoolService.getAllSchools()) {
			if (school.getConference() == null) {
				school.updateAlignment(blankConference, null, null);
			}
		}
	}

	/**
	 * Populates a school list of all schools in your excel file
	 *
	 * @param path the path of your custom conferences excel file
	 * @return List<School> of all schools in your excel file
	 * @throws IOException
	 */
	public List<School> populateSchoolsFromExcel(String path) throws IOException {
		List<School> schoolList = new ArrayList<School>();
		Workbook workbook = readExcel(path);
		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();
		// LinkedList<School> schools = new LinkedList<School>();
		// 2. Or you can use a for-each loop to iterate over the rows and columns
		// List<School> schoolList = new ArrayList<School>();
		int r = 0;
		for (Row row : sheet) {
			if (r > 0) {// disregard the headers
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
						// System.out.print(cellValue + "\t");
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
						}// end of switch
						c++;
					} // end col iterator
					School school = new School.Builder().withTgid(tgid).withName(university).withNickname(nickname)
							.withState(state).withColor(color).withAltColor(altColor).withLogo(logo).build();
					schoolList.add(school);
					// System.out.println();
				} // end of if not null
			} // end of row iterator
			r++;
		}

		return schoolList;
	}

	public void populateRivalsFromExcel(String path) throws IOException {
		Workbook workbook = readExcel(path);
		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		int iterator = 0;
		for (Row row : sheet) {
			List<School> rivals = new ArrayList<>();
			if (iterator != 0 && dataFormatter.formatCellValue(row.getCell(0)) != "") {
				int i = 0;
				School school = null;
				for (Cell cell : row) {
					if (i == 0) {
						String cellValue = dataFormatter.formatCellValue(cell);
						int tgid = Integer.parseInt(cellValue);
						school = schoolService.schoolSearch(tgid);
					}
					if (i >= 7 && dataFormatter.formatCellValue(cell) != "") {
						String cellValue = dataFormatter.formatCellValue(cell);
						School rival = schoolService.schoolSearch(cellValue);
						if (rival != null) {
							rivals.add(rival);
						}
					}
					i++;
				}
				if (school != null) {
					school.setRivals(rivals);
				}
			}
			iterator++;
		}
	}

	/**
	 * Populates the season's schedule as well as all school's schedules. Also adds
	 * FCS schools to the school list as they are found.
	 *
	 * @param file       the path of the Schedule excel file
	 * @param allSchools the list of all schools
	 * @return SeasonSchedule a list of all games in a season
	 * @throws IOException
	 */
	public List<Game> populateSeasonScheduleFromExcel(File file, List<School> allSchools)
			throws IOException, NumberFormatException {
		// if the first school has a schedule already, empty it.
		// eventually it may make sense to autowire seasonschedule and check it
		List<Game> seasonSchedule = new ArrayList<Game>();
//    	if(!allSchools.get(0).getSchedule().isEmpty()) {
		// schoolService.resetAllSchoolsSchedules();
//    	}

		School bowlSchool = new School.Builder().withTgid(511).withName("Bowl").withNickname("Bowl").build();
		Workbook workbook = readExcel(file);
		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();
		// LinkedList<School> schools = new LinkedList<School>();
		// 2. Or you can use a for-each loop to iterate over the rows and columns
		/*
		 * System.out.
		 * println("\n\nIterating over Rows and Columns using for-each loop\n");
		 */
		// SeasonSchedule seasonSchedule = new SeasonSchedule();
		// SeasonSchedule bowlSchedule = new SeasonSchedule();
		int r = 0;
		for (Row row : sheet) {
			int gasc = 0;// away score 1
			int ghsc = 0;// home score 2
			int gtod = 0;// time of day 3
			int gatg = 0;// away team tgid 4
			School awaySchool;
			int ghtg = 0;// home team tgid 5
			School homeSchool;
			int sgnm = 0;// game of week number 6
			int sewn = 0;// week number 7, sometimes sewt
			int gdat = 0;// day of week 8
			int gfot = 0;// game went into OT? 9
			int sewt = 0;// game weight?? 10
			int gffu = 0;// user game 11 12
			boolean userGame = false;
			int gmfx = 0;// conference game 13
			boolean confGame = false;

			int c = 0;
			if (r > 0) {
				for (Cell cell : row) {
					String cellValue = dataFormatter.formatCellValue(cell);
					// System.out.print(cellValue + "\t");
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
						userGame = gffu == 1;
						break;
					case 13:
						gmfx = Integer.parseInt(cellValue);
						confGame = gmfx == 1;
						break;
					default:
						break;
					}
					c++;
				}
				awaySchool = schoolService.schoolSearch(gatg);
				homeSchool = schoolService.schoolSearch(ghtg);
				Conference blankConference = Conference.blankConference;
				if (sewn <= 15 || awaySchool != null) {// only add bowl games if games already are set!
					if (awaySchool == null) {
						homeSchool = new School.Builder().withTgid(gatg).withConference(blankConference)
								.withRivals(new ArrayList<>()).build();
						allSchools.add(awaySchool);
					}
					if (homeSchool == null) {
						homeSchool = new School.Builder().withTgid(ghtg).withConference(blankConference)
								.withRivals(new ArrayList<>()).build();
						allSchools.add(homeSchool);
					}
					GameResult gameResult = new GameResult(gasc, ghsc, gfot);
					Game newGame = new Game(gameResult, gtod, awaySchool, homeSchool, sgnm, sewn,
							DayOfWeek.toEnum(gdat), userGame, confGame);
					seasonSchedule.add(newGame);
				} else {
					GameResult gameResult = new GameResult(gasc, ghsc, gfot);
					// bowlSchedule.add(new Game(gameResult, gtod, bowlSchool, bowlSchool, sgnm,
					// sewn, gdat, gffu, gmfx));
					seasonSchedule.add(new Game(gameResult, gtod, bowlSchool, bowlSchool, sgnm, sewn,
							DayOfWeek.toEnum(gdat), userGame, confGame));
				}
			}
			r++;
		}

		schoolService.populateUserSchools();
		// seasonSchedule.setBowlSchedule(bowlSchedule);
		return seasonSchedule;
	}

	/**
	 * Populates the bowl schedule
	 *
	 * @param file the path of the bowl excel file
	 * @return bowlList a list of all games in a season
	 * @throws IOException
	 */
	public List<Bowl> populateBowlsFromExcel(File file) throws IOException, NumberFormatException {

		bowlList = new ArrayList<>();
		Workbook workbook = readExcel(file);
		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();
		// 2. Or you can use a for-each loop to iterate over the rows and columns
		int r = 0;
		for (Row row : sheet) {
			int bci1 = 0;// conference 1 id 1
			int bcr1 = 0;// conference 1 rank 2
			int bci2 = 0;// conference 2 id 3
			int bcr2 = 0;// conference 2 rank 4
			int bmfd = 0;// ? 5
			int sgid = 0;// stadium id 6
			int utid = 0;// trophy id 7
			int gtod = 0;// time of day 8
			String bnme = "";// bowl name 9
			int sgnm = 0;// game of week number 10
			int bmon = 0;// bowl month 11
			int sewn = 0;// week number 12
			int blgo = 0;// bowl logo 13
			int bplo = 0;// ? 14
			int bidx = 0;// bowl index 15
			int bday = 0;// bowl day 16

			int c = 0;
			if (r > 0) {
				for (Cell cell : row) {
					String cellValue = dataFormatter.formatCellValue(cell);
					// System.out.print(cellValue + "\t");
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
			Bowl newBowl = new Bowl(bci1, bcr1, bci2, bcr2, bmfd, sgid, utid, gtod, bnme, sgnm, bmon, sewn, blgo, bplo,
					bidx, bday);
			bowlList.add(newBowl);
			r++;
		}
		return bowlList;
	}

	public File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(multipartFile.getBytes());
		fos.close();
		return file;
	}

	public List<Division> populateDivisionsFromExcel(File file) throws IOException {
		List<Division> divisions = new ArrayList<>();
		Sheet sheet;
		try (Workbook workbook = readExcel(file)) {
			sheet = workbook.getSheet("Divisions");

			DataFormatter dataFormatter = new DataFormatter();
			int r = 0;
			for (Row row : sheet) {
				if (r > 0) {// disregard the headers
					int dgid = 0;
					int cgid = 0;
					String name = "";
					String shortName = "";
					if (!dataFormatter.formatCellValue(row.getCell(0)).isEmpty()) {
						for (int i = 0; i <= 8; i++) {
							Cell cell = row.getCell(i);
							String cellValue = dataFormatter.formatCellValue(cell);
							switch (i) {
							case 0:
								dgid = Integer.parseInt(cellValue);
								break;
							case 1:
								cgid = Integer.parseInt(cellValue);
								break;
							case 2:
								name = cellValue;
								break;
							case 3:
								shortName = cellValue;
								break;
							default:
								break;
							}// end of switch
						} // end col iterator
						Conference conference = conferenceService.findConferenceById(cgid);
						Division division = new Division(dgid, name, shortName, conference);
						divisions.add(division);
						conference.getDivisions().add(division);
					} // end of if not null
				} // end of row iterator
				r++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return divisions;
	}
}