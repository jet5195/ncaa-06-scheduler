package com.robotdebris.ncaaps2scheduler.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.robotdebris.ncaaps2scheduler.service.SchoolService;

@Component
public class ConferenceList extends LinkedList<Conference> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7843984523561502029L;

	static {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");
}

private final Logger LOGGER = Logger.getLogger(ConferenceList.class.getName());
