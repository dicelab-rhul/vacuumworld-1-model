package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.security.Security;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.AbstractActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.VacuumWorldLogFormatter;

public class VWUtils {
    private static final Logger LOGGER = initLogger();
    private static int cycleNumber = 1;
    public static final String ACTOR = "Actor ";

    public static final String INVALID_INITIAL_STATE = "The received initial state is not valid.";

    private VWUtils() {}

    private static Logger initLogger() {
	Logger logger = Logger.getAnonymousLogger();
	logger.setUseParentHandlers(false);
	ConsoleHandler handler = new ConsoleHandler();
	handler.setFormatter(new VacuumWorldLogFormatter());
	logger.addHandler(handler);

	return logger;
    }

    public static int getCycleNumber() {
	return VWUtils.cycleNumber;
    }

    public static void increaseCycleNumber() {
	VWUtils.cycleNumber++;
    }

    public static void log(String message) {
	log(Level.INFO, message);
    }

    public static void log(Exception e) {
	if (e.getMessage() != null) {
	    log(e.getMessage(), e);
	}
	else {
	    log(e.getClass().getSimpleName(), e);
	}
    }

    public static void log(Exception e, String className) {
	if (e.getMessage() != null) {
	    log(className + ": " + e.getMessage(), e);
	}
	else {
	    log(className + ": " + e.getClass().getSimpleName(), e);
	}
    }

    public static void fakeLog(Exception e) {
	// this exception does not need to be logged
    }

    public static void log(String message, Exception e) {
	log(Level.SEVERE, e.getClass().getCanonicalName() + ": " + message, e);
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
	catch (Exception e) {
	    VWUtils.log(e);
	}
    }

    public static JsonObject parseJsonObjectFromString(String payload) {
	try (JsonReader reader = Json.createReader(new StringReader(payload))) {
	    return reader.readObject();
	}
	catch (Exception e) {
	    VWUtils.log(e);

	    return null;
	}
    }

    public static boolean isCollectionNotNullAndNotEmpty(Collection<?> collection) {
	if (collection == null) {
	    return false;
	}

	return !collection.isEmpty();
    }

    public static void dumpJson(JsonObject json, String filePath) {
	try (FileOutputStream output = new FileOutputStream(filePath); JsonWriter writer = Json.createWriter(output)) {
	    writer.write(json);
	}
	catch (Exception e) {
	    VWUtils.log(e);
	}
    }

    public static void dumpInitialState(JsonObject initialState) {
	dumpJson(initialState, ConfigData.getLogPath("initial.json"));
    }

    public static boolean checkObjectsEquality(Object first, Object second) {
	if (first == null) {
	    if (second != null) {
		return false;
	    }
	}
	else if (!first.equals(second)) {
	    return false;
	}

	return true;
    }

    public static boolean isInitialStateInvalid(Exception e) {
	return VWUtils.INVALID_INITIAL_STATE.equals(e.getMessage());
    }

    public static Map<String, AbstractActionReport> duplicateStringMap(Map<String, AbstractActionReport> map) {
	Map<String, AbstractActionReport> toReturn = new HashMap<>();
	map.forEach((key, value) -> toReturn.put(key, value.duplicate()));

	return toReturn;
    }

    public static void checkProvider() {
	if (Security.getProvider("BC") == null) {
	    Security.addProvider(new BouncyCastleProvider());
	}
    }
}