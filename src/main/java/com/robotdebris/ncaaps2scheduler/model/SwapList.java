package com.robotdebris.ncaaps2scheduler.model;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Component;
import java.util.LinkedList;

@Component
public class SwapList extends LinkedList<Swap> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1082717548552261220L;

	static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    private final Logger LOGGER = Logger.getLogger(SwapList.class.getName());

}
