package com.robotdebris.ncaaps2scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class NcaaPs2SchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NcaaPs2SchedulerApplication.class, args);
	}

}
