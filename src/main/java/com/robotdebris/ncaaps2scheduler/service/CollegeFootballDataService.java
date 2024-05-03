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
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import com.robotdebris.ncaaps2scheduler.serializer.ApiSchoolDeserializer;

import jakarta.validation.constraints.NotNull;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:api.properties")
public class CollegeFootballDataService {
	@Autowired
	SchoolRepository schoolRepository;
	private RestTemplate restTemplate = new RestTemplate();

	@NotNull
	@Value("${collegefootballdata.api.url}")
	private String apiUrl;

	@NotNull
	@Value("${collegefootballdata.api.key}")
	private String apiKey;

	private ResponseEntity<String> fetchData() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + apiKey); // Replace 'yourApiKey' with the actual key
		HttpEntity<String> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(apiUrl + "/teams", HttpMethod.GET, entity, String.class);
	}

	public void loadSchoolData() throws JsonProcessingException {
		if (apiUrl == null || apiKey == null) {
			System.out.println("apiUrl or apiKey are null");
		} else {
			try {
				SimpleModule module = new SimpleModule();
				module.addDeserializer(School.class, new ApiSchoolDeserializer());
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(module);
				School[] schoolData = mapper.readValue(fetchData().getBody(), School[].class);
				List<School> schools = schoolRepository.findAll();

				for (School school : schools) {
					for (School data : schoolData) {
						if (school.getName().equals(data.getName())) {
							school.setAbbreviation(data.getAbbreviation());
							school.setCity(data.getCity());
							school.setLogo(data.getLogo());
							school.setColor(data.getColor());
							school.setAltColor(data.getAltColor());
							school.setLatitude(data.getLatitude());
							school.setLongitude(data.getLongitude());
							school.setStadiumCapacity(data.getStadiumCapacity());
							school.setStadiumName(data.getStadiumName());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}