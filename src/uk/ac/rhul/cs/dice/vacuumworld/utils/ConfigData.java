package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.manhattan.VacuumWorldManhattanMind;

public class ConfigData {	
	private static int modelPort;
	
	private static boolean monitor;
	private static boolean observe;
	private static boolean evaluate;

	private static boolean log;
	private static boolean printGrid;
	private static String logsPath;
	
	private static Map<String, String> colorToMindMap;
	private static Map<String, Class<? extends VacuumWorldDefaultMind>> admissibleMindTypes;

	private static String dbName;
	private static String dbHostname;
	private static String dbPort;
	private static String agentsCollection;
	private static String dirtsCollection;
	
	private ConfigData(){}
	
	public static int getModelPort() {
		return ConfigData.modelPort;
	}
	
	public static boolean getMonitoringFlag() {
		return ConfigData.monitor;
	}
	
	public static boolean getObserveFlag() {
		return ConfigData.observe;
	}

	public static boolean getEvaluateFlag() {
		return ConfigData.evaluate;
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
	
	public static Map<String, String> getColorToMindMap() {
		return ConfigData.colorToMindMap;
	}
	
	public static Map<String, Class<? extends VacuumWorldDefaultMind>> getAdmissibleMindTypes() {
		return ConfigData.admissibleMindTypes;
	}
	
	public static Class<? extends VacuumWorldDefaultMind> getMindClassFromColor(String color) {
		String key = ConfigData.colorToMindMap.get(color);
		
		if(key == null) {
			return VacuumWorldManhattanMind.class;
		}
		
		Class<? extends VacuumWorldDefaultMind> toReturn = ConfigData.admissibleMindTypes.get(key);
		
		if(toReturn == null) {
			return VacuumWorldManhattanMind.class;
		}
		else {
			return toReturn;
		}
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

	public static String getDbPort() {
		return ConfigData.dbPort;
	}

	public static String getAgentsCollection() {
		return ConfigData.agentsCollection;
	}

	public static String getDirtsCollection() {
		return ConfigData.dirtsCollection;
	}

	public static boolean initConfigData(String configFilePath) {
		try(JsonReader reader = Json.createReader(new FileReader(configFilePath))) {
			return initData(reader);
		}
		catch(Exception e) {
			Utils.log(e);
			
			return false;
		}
	}

	private static boolean initData(JsonReader reader) {
		JsonObject config = reader.readObject();
		
		ConfigData.modelPort = config.getInt("model_port");
		ConfigData.monitor = config.getBoolean("monitor");
		ConfigData.observe = config.getBoolean("observe");
		ConfigData.evaluate = config.getBoolean("evaluate");
		ConfigData.log = config.getBoolean("log");
		ConfigData.printGrid = config.getBoolean("print_grid");
		ConfigData.logsPath = config.getString("logs_path");
		
		return initColorToMindMap(config) && initAdmissibleMindTypes(config) && initDatabaseData(config);
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
		catch(ClassNotFoundException e) {
			Utils.log(e);
			
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean fillMindTypesMap(JsonObject mindTypes) throws ClassNotFoundException {
		for(Entry<String, JsonValue> entry : mindTypes.entrySet()) {
			Class<?> temp = Class.forName(entry.getValue().toString().replaceAll("\"", ""));
			
			if(Class.forName(VacuumWorldDefaultMind.class.getCanonicalName()).isAssignableFrom(temp)) {
				ConfigData.admissibleMindTypes.put(entry.getKey(), (Class<VacuumWorldDefaultMind>) temp);
			}
		}
		
		return true;
	}

	private static boolean initDatabaseData(JsonObject config) {
		JsonObject db = config.getJsonObject("database");
		
		ConfigData.dbName = db.getString("name");
		ConfigData.dbHostname = db.getString("hostname");
		ConfigData.dbPort = db.getString("port");
		ConfigData.agentsCollection = db.getString("agents_collection");
		ConfigData.dirtsCollection = db.getString("dirts_collection");
		
		return true;
	}
}