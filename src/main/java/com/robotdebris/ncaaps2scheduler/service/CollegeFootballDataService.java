package com.robotdebris.ncaaps2scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.model.SchoolDeserializer;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CollegeFootballDataService {
    @Autowired
    SchoolRepository schoolRepository;
    private RestTemplate restTemplate = new RestTemplate();
    private String apiUrl = "https://api.collegefootballdata.com/teams";

    private ResponseEntity<String> fetchData() {
        HttpHeaders headers = new HttpHeaders();
        String apiKey = "";
        headers.set("Authorization", "Bearer " + apiKey); // Replace 'yourApiKey' with the actual key
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> data = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        return data;
    }

    public void loadSchoolData() throws JsonMappingException, JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(School.class, new SchoolDeserializer());
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
    }
}