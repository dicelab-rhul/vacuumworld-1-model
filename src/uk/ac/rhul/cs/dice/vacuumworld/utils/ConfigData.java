package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.manhattan.VacuumWorldManhattanMind;

public class ConfigData {
    private static int modelPort;

    private static int monitoringAgentsNumber;
    private static int observingAgentsNumber;
    private static int evaluatingAgentsNumber;

    private static boolean log;
    private static boolean printGrid;
    private static String logsPath;
    private static int timeoutInSeconds;

    private static Map<String, String> colorToMindMap;
    private static Map<String, Class<? extends VacuumWorldDefaultMind>> admissibleMindTypes;
    private static List<Class<? extends EnvironmentalAction>> cleaningAgentActions;
    private static List<Class<? extends EnvironmentalAction>> userActions;
    private static List<Class<? extends EnvironmentalAction>> monitoringAgentActions;

    private static String dbName;
    private static String dbHostname;
    private static int dbPort;
    private static String stateActionsCollection;
    private static String systemStatesCollection;

    private ConfigData() {
    }

    public static int getModelPort() {
	return ConfigData.modelPort;
    }

    public static int getMonitoringAgentsNumber() {
	return ConfigData.monitoringAgentsNumber;
    }

    public static int getObservingAgentsNumber() {
	return ConfigData.observingAgentsNumber;
    }

    public static int getEvaluatingAgentsNumber() {
	return ConfigData.evaluatingAgentsNumber;
    }

    public static boolean getLoggingFlag() {
	return ConfigData.log;
    }

    public static boolean getPrintGridFlag() {
	return ConfigData.printGrid;
    }

    public static String getLogsPath() {
	return ConfigData.logsPath;
    }

    public static int getTimeoutInSeconds() {
	return ConfigData.timeoutInSeconds;
    }

    public static Map<String, String> getColorToMindMap() {
	return ConfigData.colorToMindMap;
    }

    public static Map<String, Class<? extends VacuumWorldDefaultMind>> getAdmissibleMindTypes() {
	return ConfigData.admissibleMindTypes;
    }

    public static Class<? extends VacuumWorldDefaultMind> getMindClassFromColor(String color) {
	String key = ConfigData.colorToMindMap.get(color);

	if (key == null) {
	    return VacuumWorldManhattanMind.class;
	}

	Class<? extends VacuumWorldDefaultMind> toReturn = ConfigData.admissibleMindTypes.get(key);

	if (toReturn == null) {
	    return VacuumWorldManhattanMind.class;
	}
	else {
	    return toReturn;
	}
    }

    public static List<Class<? extends EnvironmentalAction>> getCleaningAgentActions() {
	return ConfigData.cleaningAgentActions;
    }

    public static List<Class<? extends EnvironmentalAction>> getUserActions() {
	return ConfigData.userActions;
    }

    public static List<Class<? extends EnvironmentalAction>> getMonitoringAgentActions() {
	return ConfigData.monitoringAgentActions;
    }

    public static String getLogPath(String filename) {
	return ConfigData.logsPath + filename;
    }

    public static String getDbName() {
	return ConfigData.dbName;
    }

    public static String getDbHostname() {
	return ConfigData.dbHostname;
    }

    public static int getDbPort() {
	return ConfigData.dbPort;
    }

    public static String getStateActionsCollection() {
	return ConfigData.stateActionsCollection;
    }

    public static String getSystemStatesCollection() {
	return ConfigData.systemStatesCollection;
    }

    public static boolean initConfigData(String configFilePath) {
	try (JsonReader reader = Json.createReader(new FileReader(configFilePath))) {
	    return initData(reader);
	}
	catch (Exception e) {
	    VWUtils.log(e);

	    return false;
	}
    }

    private static boolean initData(JsonReader reader) {
	JsonObject config = reader.readObject();

	ConfigData.modelPort = config.getInt("model_port");
	ConfigData.log = config.getBoolean("log");
	ConfigData.printGrid = config.getBoolean("print_grid");
	ConfigData.logsPath = config.getString("logs_path");
	ConfigData.timeoutInSeconds = config.getInt("timeout_in_seconds");

	return initMonitoring(config) && initMaps(config) && initActions(config) && initDatabaseData(config);
    }

    private static boolean initMonitoring(JsonObject config) {
	JsonObject monitoringConfig = config.getJsonObject("monitor_observe_evaluate");

	ConfigData.monitoringAgentsNumber = monitoringConfig.getInt("monitoring_agents_number");
	ConfigData.observingAgentsNumber = monitoringConfig.getInt("observing_agents_number");
	ConfigData.evaluatingAgentsNumber = monitoringConfig.getInt("evaluating_agents_number");

	return true;
    }

    private static boolean initActions(JsonObject config) {
	return initCleaningAgentActions(config) && initUserActions(config) && initMonitoringAgentActions(config);
    }

    @SuppressWarnings("unchecked")
    private static boolean initUserActions(JsonObject config) {
	ConfigData.userActions = new ArrayList<>();
	JsonObject userActions = config.getJsonObject("user_actions");

	try {
	    for (Entry<String, JsonValue> entry : userActions.entrySet()) {
		Class<?> temp = Class.forName(entry.getValue().toString().replaceAll("\"", ""));

		if (Class.forName(EnvironmentalAction.class.getCanonicalName()).isAssignableFrom(temp)) {
		    ConfigData.userActions.add((Class<EnvironmentalAction>) temp);
		}
	    }

	    return true;
	}
	catch (ClassNotFoundException e) {
	    return manageClassNotFoundExceptionInParsing(e);
	}
    }

    @SuppressWarnings("unchecked")
    private static boolean initMonitoringAgentActions(JsonObject config) {
	ConfigData.monitoringAgentActions = new ArrayList<>();
	JsonObject monitoringAgentActions = config.getJsonObject("monitoring_agent_actions");

	try {
	    for (Entry<String, JsonValue> entry : monitoringAgentActions.entrySet()) {
		Class<?> temp = Class.forName(entry.getValue().toString().replaceAll("\"", ""));

		if (Class.forName(EnvironmentalAction.class.getCanonicalName()).isAssignableFrom(temp)) {
		    ConfigData.monitoringAgentActions.add((Class<EnvironmentalAction>) temp);
		}
	    }

	    return true;
	}
	catch (ClassNotFoundException e) {
	    return manageClassNotFoundExceptionInParsing(e);
	}
    }

    @SuppressWarnings("unchecked")
    private static boolean initCleaningAgentActions(JsonObject config) {
	ConfigData.cleaningAgentActions = new ArrayList<>();
	JsonObject cleaningAgentActions = config.getJsonObject("cleaning_agent_actions");

	try {
	    for (Entry<String, JsonValue> entry : cleaningAgentActions.entrySet()) {
		Class<?> temp = Class.forName(entry.getValue().toString().replaceAll("\"", ""));

		if (Class.forName(EnvironmentalAction.class.getCanonicalName()).isAssignableFrom(temp)) {
		    ConfigData.cleaningAgentActions.add((Class<EnvironmentalAction>) temp);
		}
	    }

	    return true;
	}
	catch (ClassNotFoundException e) {
	    return manageClassNotFoundExceptionInParsing(e);
	}
    }

    private static boolean initMaps(JsonObject config) {
	return initColorToMindMap(config) && initAdmissibleMindTypes(config);
    }

    private static boolean initColorToMindMap(JsonObject config) {
	ConfigData.colorToMindMap = new HashMap<>();
	JsonObject colorsToMind = config.getJsonObject("colors_to_mind");

	ConfigData.colorToMindMap.put("green", colorsToMind.getString("green"));
	ConfigData.colorToMindMap.put("orange", colorsToMind.getString("orange"));
	ConfigData.colorToMindMap.put("white", colorsToMind.getString("white"));

	return true;
    }

    private static boolean initAdmissibleMindTypes(JsonObject config) {
	ConfigData.admissibleMindTypes = new HashMap<>();
	JsonObject mindTypes = config.getJsonObject("mind_types");

	try {
	    return fillMindTypesMap(mindTypes);
	}
	catch (ClassNotFoundException e) {
	    return manageClassNotFoundExceptionInParsing(e);
	}
    }

    @SuppressWarnings("unchecked")
    private static boolean fillMindTypesMap(JsonObject mindTypes) throws ClassNotFoundException {
	for (Entry<String, JsonValue> entry : mindTypes.entrySet()) {
	    Class<?> temp = Class.forName(entry.getValue().toString().replaceAll("\"", ""));

	    if (Class.forName(VacuumWorldDefaultMind.class.getCanonicalName()).isAssignableFrom(temp)) {
		ConfigData.admissibleMindTypes.put(entry.getKey(), (Class<VacuumWorldDefaultMind>) temp);
	    }
	}

	return true;
    }

    private static boolean initDatabaseData(JsonObject config) {
	JsonObject db = config.getJsonObject("database");

	ConfigData.dbName = db.getString("name");
	ConfigData.dbHostname = db.getString("hostname");
	ConfigData.dbPort = Integer.valueOf(db.getString("port"));
	ConfigData.stateActionsCollection = db.getString("state_actions_collection");
	ConfigData.systemStatesCollection = db.getString("system_states_collection");

	return true;
    }

    private static boolean manageClassNotFoundExceptionInParsing(ClassNotFoundException e) {
	VWUtils.log(e, ConfigData.class.getSimpleName());
	VWUtils.logWithClass(ConfigData.class.getSimpleName(), "The specified class does not exist. Check the configuration file for typos and errors...\n   ...and remember to specify the full package path [uk.ac.rhul.(...).<TargetClass>] ...\n   ...where <TargetClass> is the class simple name without [.java / .class].");

	return false;
    }
}