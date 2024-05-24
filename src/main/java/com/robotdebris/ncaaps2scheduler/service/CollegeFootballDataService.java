package com.robotdebris.ncaaps2scheduler.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotdebris.ncaaps2scheduler.model.CollegeFootballDataTeam;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:api.properties")
public class CollegeFootballDataService {
	@Autowired
	SchoolRepository schoolRepository;
	private RestTemplate restTemplate = new RestTemplate();

	@Value("${collegefootballdata.api.url:''}")
	private String apiUrl;

	@Value("${collegefootballdata.api.key:''}")
	private String apiKey;

	@Autowired
	ObjectMapper mapper;

	private ResponseEntity<String> fetchData() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + apiKey); // Replace 'yourApiKey' with the actual key
		HttpEntity<String> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(apiUrl + "/teams/fbs", HttpMethod.GET, entity, String.class);
	}

	public void loadSchoolData() throws JsonProcessingException {
		if (apiUrl == null || apiKey == null) {
			System.out.println("apiUrl or apiKey are null");
		} else {
			try {
//				SimpleModule module = new SimpleModule();
//				module.addDeserializer(School.class, new ApiSchoolDeserializer());
//				ObjectMapper mapper = new ObjectMapper();
//				mapper.registerModule(module);
				CollegeFootballDataTeam[] apiData = mapper.readValue(fetchData().getBody(),
						CollegeFootballDataTeam[].class);
				List<School> schools = schoolRepository.findAll();

				for (School school : schools) {
					for (CollegeFootballDataTeam data : apiData) {
						if (matchSchool(school, data)) {
							school.setAbbreviation(data.getAbbreviation());
							school.setCity(data.getLocation().getCity());
							school.setLogo(data.getLogos().getFirst());
							school.setColor(data.getColor());
							school.setAltColor(data.getAltColor());
							school.setLatitude(data.getLocation().getLatitude());
							school.setLongitude(data.getLocation().getLongitude());
							school.setStadiumCapacity(data.getLocation().getCapacity());
							school.setStadiumName(data.getLocation().getName());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean matchSchool(School school, CollegeFootballDataTeam data) {
		if (school.getName().equalsIgnoreCase(data.getSchool()) || school.getName().equalsIgnoreCase(data.getAltName1())
				|| school.getName().equalsIgnoreCase(data.getAltName2())
				|| school.getName().equalsIgnoreCase(data.getAltName3())) {
			return true;
		} else {
			return false;
		}
	}
}