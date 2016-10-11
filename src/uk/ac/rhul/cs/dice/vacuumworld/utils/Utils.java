package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.VacuumWorldLogFormatter;

public class Utils {
	private static final Logger LOGGER = initLogger();
	private static int cycleNumber = 1;
	public static final String AGENT = "Agent ";
	
	public static final String INVALID_INITIAL_STATE = "The received initial state is not valid.";
	
	private Utils(){}
	
	private static Logger initLogger() {
		Logger logger = Logger.getAnonymousLogger();
		logger.setUseParentHandlers(false);
		VacuumWorldLogFormatter formatter = new VacuumWorldLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		
		return logger;
	}

	public static final Logger fileLogger(String file, boolean append) {
		FileHandler fileHandler = null;
		Logger logger = null;
		
		try {
			fileHandler = new FileHandler(file, append);
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
	
	public static void fakeLog(Exception e) {
		//this exception does not need to be logged
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
	
	public static void logWithClass(String source, String message) {
		log(source + ": " + message);
	}
	
	public static void logState(String s) {
		log("\n\n" + s + "\n\n");
	}

	public static void doWait(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		}
		catch(Exception e) {
			Utils.log(e);
		}
	}
	
	public static JsonObject parseJsonObjectFromString(String payload) {
		JsonReader reader = Json.createReader(new StringReader(payload));
		JsonObject toReturn = reader.readObject();
		reader.close();
		
		return toReturn;
	}
	
	public static boolean isCollectionNotNullAndNotEmpty(Collection<?> collection) {
		if(collection == null) {
			return false;
		}
		
		return !collection.isEmpty();
	}
}