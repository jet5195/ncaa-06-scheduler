package com.robotdebris.ncaaps2scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class NcaaPs2SchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NcaaPs2SchedulerApplication.class, args);
	}

}
