package com.robotdebris.ncaaps2scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.service.CollegeFootballDataService;
import com.robotdebris.ncaaps2scheduler.service.ConferenceService;
import com.robotdebris.ncaaps2scheduler.service.DivisionService;

@Component
public class ExcelReader {

	ConferenceService conferenceService;
	SchoolRepository schoolRepository;
	CollegeFootballDataService dataService;

	private final Logger LOGGER = Logger.getLogger(ExcelReader.class.getName());
	private DivisionService divisionService;

	@Autowired
	public ExcelReader(SchoolRepository schoolRepostiory, ConferenceService conferenceService,
			DivisionService divisionService, CollegeFootballDataService dataService) {
		this.schoolRepository = schoolRepostiory;
		this.conferenceService = conferenceService;
		this.divisionService = divisionService;
		this.dataService = dataService;
	}

	private Workbook readExcel(File file) throws IOException {
		if (file.toString().endsWith("csv")) {
			return convertCsvToXLSX(file);
		}
		return WorkbookFactory.create(file);
	}

	private Workbook convertCsvToXLSX(File file) {
		String csvFileAddress = file.getPath();
		String xlsxFileAddress = csvFileAddress + ".xlsx";

		try (XSSFWorkbook workBook = new XSSFWorkbook();
				BufferedReader br = new BufferedReader(new FileReader(csvFileAddress));
				FileOutputStream fileOutputStream = new FileOutputStream(xlsxFileAddress)) {

			XSSFSheet sheet = workBook.createSheet("sheet1");
			String currentLine;
			int rowNum = 0;

			while ((currentLine = br.readLine()) != null) {
				String[] str = currentLine.split(",");
				XSSFRow currentRow = sheet.createRow(rowNum++);

				for (int i = 0; i < str.length; i++) {
					currentRow.createCell(i).setCellValue(str[i]);
				}
			}

			workBook.write(fileOutputStream);
			return workBook;

		} catch (Exception ex) {
			LOGGER.error("Error converting CSV to XLSX: " + ex.getMessage(), ex);
			return null;
		}
	}

	public List<Conference> populateConferencesFromExcel(File file) throws IOException {
		List<Conference> conferences = new ArrayList<>();
		try (Workbook workbook = readExcel(file)) {
			Sheet sheet = workbook.getSheet("Conferences");
			DataFormatter dataFormatter = new DataFormatter();
			for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
				Row row = sheet.getRow(r);
				if (row != null && !isStringBlank(dataFormatter.formatCellValue(row.getCell(0)))) {
					Conference conference = parseConferenceRow(row, dataFormatter);
					if (conference != null) {
						conferences.add(conference);
					}
				}
			}
		}
		return conferences;
	}

	private Conference parseConferenceRow(Row row, DataFormatter formatter) {
		Integer conferenceId = parseInteger(formatter, row, 0, "parseConferenceRow");
		String conferenceName = formatter.formatCellValue(row.getCell(1));
		String conferenceShortName = formatter.formatCellValue(row.getCell(2));
		String abbreviation = formatter.formatCellValue(row.getCell(3));
		NCAADivision classification = parseClassification(formatter, row, 4);
		boolean powerConf = row.getCell(5).getBooleanCellValue();
		int confGames = parseInteger(formatter, row, 6, conferenceShortName);
		int startWeek = parseInteger(formatter, row, 7, conferenceShortName);
		String logo = formatter.formatCellValue(row.getCell(8));

		return new Conference(conferenceId, conferenceName, conferenceShortName, abbreviation,
				classification, powerConf, confGames, startWeek, logo);
	}

	private NCAADivision parseClassification(DataFormatter formatter, Row row, int cellIndex) {
		String value = formatter.formatCellValue(row.getCell(cellIndex));
		return isStringBlank(value) ? NCAADivision.FCS : NCAADivision.valueOf(value);
	}

	private int parseInteger(DataFormatter formatter, Row row, int cellIndex, String context) {
		try {
			return Integer.parseInt(formatter.formatCellValue(row.getCell(cellIndex)));
		} catch (NumberFormatException e) {
			LOGGER.warn(
					"Invalid integer for " + context + " in cell " + cellIndex + ". Found " + row.getCell(cellIndex));
			return 0;
		}
	}

	// String.isBlank doesn't work on older versions of java?
	private boolean isStringBlank(String string) {
		return string == null || string.trim().isEmpty();
	}

	public void populateAlignmentFromExcel(File file) throws IOException {
		try (Workbook workbook = readExcel(file)) {
			Sheet sheet = workbook.getSheet("Alignment");
			DataFormatter dataFormatter = new DataFormatter();

			for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
				Row row = sheet.getRow(r);
				if (row != null && !isStringBlank(dataFormatter.formatCellValue(row.getCell(0)))) {
					updateSchoolAlignment(row, dataFormatter);
				}
			}
		}
		updateSchoolsWithBlankConference();
	}

	private void updateSchoolAlignment(Row row, DataFormatter formatter) {
		int tgid = parseInteger(formatter, row, 0, "updateSchoolAlignment");
		String schoolName = formatter.formatCellValue(row.getCell(1));
		String conferenceShortName = formatter.formatCellValue(row.getCell(2));
		String divisionName = formatter.formatCellValue(row.getCell(3));
		String xDivRival = formatter.formatCellValue(row.getCell(4));

		School school = schoolRepository.findById(tgid);
		if (school != null) {
			School xDivRivalSchool = schoolRepository.findByName(xDivRival);
			Conference conference = conferenceService.findByShortName(conferenceShortName);
			Division division = divisionService.findByName(divisionName);
			school.updateAlignment(conference, division, xDivRivalSchool);
		} else {
			LOGGER.warn("School with TGID " + tgid + " not found for " + schoolName);
		}
	}

	private void updateSchoolsWithBlankConference() {
		Conference blankConference = Conference.blankConference;
		for (School school : schoolRepository.findAll()) {
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
	public List<School> populateSchoolsFromExcel(File file) throws IOException {
		List<School> schoolList = new ArrayList<>();
		try (Workbook workbook = readExcel(file)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
				Row row = sheet.getRow(r);
				if (row != null && !isStringBlank(dataFormatter.formatCellValue(row.getCell(0)))) {
					School school = parseSchoolRow(row, dataFormatter);
					if (school != null) {
						schoolList.add(school);
					}
				}
			}
		}
		return schoolList;
	}

	private School parseSchoolRow(Row row, DataFormatter formatter) {
		int tgid = parseInteger(formatter, row, 0, "parseSchoolRow");
		String university = formatter.formatCellValue(row.getCell(1));
		String nickname = formatter.formatCellValue(row.getCell(2));
		String state = formatter.formatCellValue(row.getCell(3));
		String color = formatter.formatCellValue(row.getCell(4));
		String altColor = formatter.formatCellValue(row.getCell(5));
		String logo = formatter.formatCellValue(row.getCell(6));

		return new School.Builder()
				.withTgid(tgid)
				.withName(university)
				.withNickname(nickname)
				.withState(state)
				.withColor(color)
				.withAltColor(altColor)
				.withLogo(logo)
				.build();
	}

	public void populateRivalsFromExcel(File file) throws IOException {
		try (Workbook workbook = readExcel(file)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
				Row row = sheet.getRow(r);
				if (row != null && !isStringBlank(dataFormatter.formatCellValue(row.getCell(0)))) {
					updateRivals(row, dataFormatter);
				}
			}
		}
	}

	private void updateRivals(Row row, DataFormatter formatter) {
		int tgid = parseInteger(formatter, row, 0, "updateRivals");
		School school = schoolRepository.findById(tgid);
		if (school != null) {
			// columns 7-14 are reserved for rival names
			for (int i = 7; i <= 14; i++) {
				String rivalName = formatter.formatCellValue(row.getCell(i));
				if (!rivalName.isBlank()) {
					School rival = schoolRepository.findByName(rivalName);
					if (rival != null) {
						school.addRival(rival);
					} else {
						LOGGER.warn("Rival school " + rivalName + " not found for school " + school.getName());
					}
				}
			}
		} else {
			LOGGER.warn("School with TGID " + tgid + " not found");
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
	public List<Game> populateSeasonScheduleFromExcel(File file, List<School> allSchools) throws IOException {
		List<Game> seasonSchedule = new ArrayList<>();
		School bowlSchool = new School.Builder().withTgid(511).withName("Bowl").withNickname("Bowl").build();

		try (Workbook workbook = readExcel(file)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			for (int r = 0; r < sheet.getPhysicalNumberOfRows(); r++) {
				if (r == 0)
					continue; // Skip header row

				Row row = sheet.getRow(r);
				if (row == null || isStringBlank(dataFormatter.formatCellValue(row.getCell(0)))) {
					continue;
				}
				Game game = parseRowToGame(row, dataFormatter, allSchools, bowlSchool);
				if (game != null) {
					seasonSchedule.add(game);
				}
			}
		}

		return seasonSchedule;
	}

	private Game parseRowToGame(Row row, DataFormatter formatter, List<School> allSchools, School bowlSchool) {
		int[] gameData = new int[14];
		boolean userGame = false;
		boolean confGame = false;

		for (int c = 0; c < row.getPhysicalNumberOfCells(); c++) {
			String cellValue = formatter.formatCellValue(row.getCell(c));
			if (c == 11) {
				userGame = "1".equals(cellValue);
			} else if (c == 13) {
				confGame = "1".equals(cellValue);
			} else {
				try {
					gameData[c] = Integer.parseInt(cellValue);
				} catch (NumberFormatException e) {
					gameData[c] = 0;
				}
			}
		}

		School awaySchool = findOrAddSchool(allSchools, gameData[4]);
		School homeSchool = findOrAddSchool(allSchools, gameData[5]);

		if (gameData[7] <= 15 || awaySchool != null) {
			if (awaySchool == null)
				awaySchool = createAndAddSchool(allSchools, gameData[4]);
			if (homeSchool == null)
				homeSchool = createAndAddSchool(allSchools, gameData[5]);

			GameResult gameResult = new GameResult(gameData[1], gameData[2], gameData[9]);
			return new Game(gameResult, gameData[3], awaySchool, homeSchool, gameData[6], gameData[7],
					DayOfWeek.toEnum(gameData[8]), userGame, confGame);
		} else {
			GameResult gameResult = new GameResult(gameData[1], gameData[2], gameData[9]);
			return new Game(gameResult, gameData[3], bowlSchool, bowlSchool, gameData[6], gameData[7],
					DayOfWeek.toEnum(gameData[8]), userGame, confGame);
		}
	}

	private School findOrAddSchool(List<School> allSchools, int tgid) {
		return allSchools.stream()
				.filter(school -> school.getTgid() == tgid)
				.findFirst()
				.orElse(null);
	}

	private School createAndAddSchool(List<School> allSchools, int tgid) {
		School newSchool = new School.Builder()
				.withTgid(tgid)
				.withConference(Conference.blankConference)
				.withRivals(new ArrayList<>())
				.build();
		allSchools.add(newSchool);
		return newSchool;
	}

	/**
	 * Populates the bowl schedule
	 *
	 * @param file the path of the bowl excel file
	 * @return bowlList a list of all games in a season
	 * @throws IOException
	 */
	public List<Bowl> populateBowlsFromExcel(File file) throws IOException, NumberFormatException {

		List<Bowl> bowlList = new ArrayList<>();
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