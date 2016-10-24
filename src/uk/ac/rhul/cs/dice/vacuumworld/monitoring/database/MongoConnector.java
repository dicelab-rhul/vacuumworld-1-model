package uk.ac.rhul.cs.dice.vacuumworld.monitoring.database;

import java.util.List;

import javax.json.JsonObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class MongoConnector {
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<JsonObject> agentsHistories;
	
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
			this.agentsHistories = this.database.getCollection(ConfigData.getAgentsCollection(), JsonObject.class);
			
			return check();
		}
		catch(Exception e) {
			VWUtils.log(e);
			
			return false;
		}
	}

	private boolean check() {
		return this.mongoClient != null && this.database != null && this.agentsHistories != null;
	}
	
	public boolean updateAgentsHistories(List<JsonObject> agentsHistories) {
		if(!connect()) {
			return false;
		}
		
		return updateAgentsHistoriesHelper(agentsHistories);
	}
	
	private boolean updateAgentsHistoriesHelper(List<JsonObject> agentsHistories) {
		//TODO
		return false;
	}
	
	/*private JsonObject mergeDocuments(JsonObject first, JsonObject second) {
		//TODO
		return null;
	}*/
}