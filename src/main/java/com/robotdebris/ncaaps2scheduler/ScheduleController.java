package com.robotdebris.ncaaps2scheduler;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;  
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {
	
	//mapping the getSchools( method to /product
	@GetMapping(value = "/schools")
	public List<School> getSchool(){
		
	}

}
