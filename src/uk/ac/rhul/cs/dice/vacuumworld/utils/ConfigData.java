package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.FileReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ConfigData {	
	private static int modelPort;
	
	private static boolean monitor;
	private static boolean observe;
	private static boolean evaluate;

	private static boolean log;
	private static boolean printGrid;

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
		
		return initDatabaseData(config);
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