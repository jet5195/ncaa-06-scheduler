package com.robotdebris.ncaaps2scheduler;

import java.io.IOException;
import java.util.List;

import com.robotdebris.ncaaps2scheduler.model.School;

public interface SchoolDataProvider {
	List<School> getSchoolData(String schoolsFile) throws IOException;

	School schoolSearch(int tgid);

	School schoolSearch(String xDivRival);

	List<School> getSchoolList();

	void resetAllSchoolsSchedules();

	void populateUserSchools();
}