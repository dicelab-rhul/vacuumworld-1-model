package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static final Logger LOGGER = Logger.getGlobal();
	private static int cycleNumber = 1;
	
	private Utils(){}
	
	public static final Logger fileLogger(String file) {
		FileHandler fileHandler = null;
		Logger logger = null;
		
		try {
			fileHandler = new FileHandler(file);
			logger = Logger.getAnonymousLogger();
			fileHandler.setFormatter(new InfoLogFormatter());
			logger.addHandler(fileHandler);
			logger.setUseParentHandlers(false);
			return logger;
		}
		catch (SecurityException | IOException e) {
			log(e);
			return null;
		}
	}

	public static int getCycleNumber() {
		return Utils.cycleNumber;
	}
	
	public static void increaseCycleNumber() {
		Utils.cycleNumber++;
	}
	
	public static void log(String message) {
		log(Level.INFO, message);
	}
	
	public static void log(Exception e) {
		log(e.getMessage(), e);
	}

	public static void log(String message, Exception e) {
		log(Level.SEVERE, message, e);
	}

	public static void log(Level level, String message) {
		LOGGER.log(level, message);
	}

	public static void log(Level level, String message, Exception e) {
		LOGGER.log(level, message, e);
	}

	public static void doWait(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		}
		catch(Exception e) {
			Utils.log(e);
		}
	}
}