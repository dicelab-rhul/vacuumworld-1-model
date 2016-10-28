package uk.ac.rhul.cs.dice.vacuumworld.monitoring.database;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.bson.BsonDocument;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class MongoConnector {
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<BsonDocument> systemStates;
	private MongoCollection<BsonDocument> statesActions;
	
	public boolean connect() {
		if(check()) {
			return true;
		}
		else {
			return doConnection();
		}
	}
	
	private boolean doConnection() {
		try {
			this.mongoClient = new MongoClient(ConfigData.getDbHostname(), ConfigData.getDbPort());
			this.database = this.mongoClient.getDatabase(ConfigData.getDbName());
			this.systemStates = this.database.getCollection(ConfigData.getSystemStatesCollection(), BsonDocument.class);
			this.statesActions = this.database.getCollection(ConfigData.getStateActionsCollection(), BsonDocument.class);
			
			return check();
		}
		catch(Exception e) {
			VWUtils.log(e);
			
			return false;
		}
	}

	private boolean check() {
		return this.mongoClient != null && this.database != null && this.systemStates != null;
	}
	
	public boolean updateSystemStates(List<JsonObject> states) {
		if(!connect()) {
			return false;
		}

		return updateSystemStatesHelper(states);
	}
	
	private boolean updateSystemStatesHelper(List<JsonObject> states) {
		long statesNumber = this.systemStates.count();
		states.forEach(this::insertState);
		
		return this.systemStates.count() == statesNumber + states.size();
	}
	
	private void insertState(JsonObject state) {
		this.systemStates.insertOne(BsonDocument.parse(state.toString()));
	}
	
	public List<JsonObject> getStates() {
		if(!connect()) {
			return new ArrayList<>();
		}
		
		return this.systemStates.find().map(document -> document.toJson()).into(new ArrayList<>()).stream().map(this::readFromString).filter(object -> object != null).collect(Collectors.toList());
	}
	
	public JsonObject readFromString(String string) {
		try (JsonReader reader = Json.createReader(new StringReader(string))) {
			return reader.readObject();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return null;
		}
	}

	public boolean updateSystemStatesActions(List<JsonObject> actionReports) {
		if(!connect()) {
			return false;
		}

		return updateSystemStatesActionsHelper(actionReports);
	}

	private boolean updateSystemStatesActionsHelper(List<JsonObject> actionReports) {
		long statesNumber = this.statesActions.count();
		actionReports.forEach(this::insertActionsReport);
		
		return this.statesActions.count() == statesNumber + actionReports.size();
	}
	
	private void insertActionsReport(JsonObject actionsReport) {
		this.statesActions.insertOne(BsonDocument.parse(actionsReport.toString()));
	}
}