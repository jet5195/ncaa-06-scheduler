package com.robotdebris.ncaaps2scheduler.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.robotdebris.ncaaps2scheduler.ExcelReader;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.GameRepository;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;

@Service
public class SchoolService {

	private final SchoolRepository schoolRepository;
	private final GameRepository gameRepository;
	private final ExcelReader excelReader;
	private final CollegeFootballDataService dataService;

	@Autowired
	public SchoolService(SchoolRepository schoolRepository, GameRepository gameRepository, ExcelReader excelReader,
			CollegeFootballDataService dataService) {
		this.schoolRepository = schoolRepository;
		this.gameRepository = gameRepository;
		this.excelReader = excelReader;
		this.dataService = dataService;
	}

	public List<School> getAllSchools() {
		return schoolRepository.findAll();
	}

	public void setSchoolList(List<School> schoolList) {
		schoolRepository.saveAll(schoolList);
	}

	/**
	 * @param name the String name of the School you are searching for
	 * @return School with the same name as the parameter inputted
	 */
	public School schoolSearch(String name) {
		// TODO: consider returning optional<school>
		return schoolRepository.findByName(name);
	}

	/**
	 * @param conferenceName the name of a conference
	 * @return all schools in a given conference conf
	 */
	public List<School> getAllSchoolsInConference(String conferenceName) {
		List<School> allSchools = schoolRepository.findAll();
		return allSchools.stream()
				.filter(school -> school.getConference() != null
						&& school.getConference().getName().equalsIgnoreCase(conferenceName))
				.collect(Collectors.toList());
	}

	/**
	 * @param tgid the tgid of the school you are searching for
	 * @return School with the same tgid as the parameter inputted
	 */
	public School schoolSearch(int tgid) {
		return schoolRepository.findById(tgid);
	}

	/**
	 * Searches for user schools and adds that flag to every user school in the list
	 */
	public void populateUserSchools() {
		List<School> allSchools = schoolRepository.findAll();
		for (School school : allSchools) {
			long numOfUserGames = gameRepository.findGamesByTeam(school).stream().filter(game -> game.isUserGame())
					.count();
			if (numOfUserGames >= 10) {
				school.setUserTeam(true);
			}
		}
		schoolRepository.saveAll(allSchools);
	}

	public void saveSchool(School school) {
		schoolRepository.save(school);
	}

	public void loadSchoolDataFromFile(MultipartFile multipartFile) throws IOException {

		File file = excelReader.convertMultipartFileToFile(multipartFile);

		try {
			List<School> schoolList = excelReader.populateSchoolsFromExcel(file);
			Collections.sort(schoolList);
			setSchoolList(schoolList);
			excelReader.populateRivalsFromExcel(file);
			dataService.loadSchoolData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
