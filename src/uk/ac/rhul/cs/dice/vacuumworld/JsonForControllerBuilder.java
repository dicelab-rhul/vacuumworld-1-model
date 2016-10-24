package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelMessagesEnum;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelUpdate;

public class JsonForControllerBuilder {
	private JsonForControllerBuilder() {}
	
	public static ModelUpdate createModelUpdate(VacuumWorldSpace space) {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> map = space.getFullGrid();
		
		JsonObject stateRepresentation = buildStateRepresentation(space.getDimensions(), map);
		return new ModelUpdate(ModelMessagesEnum.STATE_UPDATE, stateRepresentation.toString());
	}

	private static JsonObject buildStateRepresentation(int[] dimensions, Map<VacuumWorldCoordinates, VacuumWorldLocation> map) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("size", dimensions[0]);
		
		JsonArray locations = buildLocationsRepresentation(map);
		builder.add("notable_locations", locations);
		
		return builder.build();
	}

	private static JsonArray buildLocationsRepresentation(Map<VacuumWorldCoordinates, VacuumWorldLocation> map) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(VacuumWorldLocation location : map.values()) {
			JsonObject loc = buildLocationIfNecessary(location);
			
			if(loc != null) {
				builder.add(loc);
			}
		}
		
		return builder.build();
	}

	private static JsonObject buildLocationIfNecessary(VacuumWorldLocation location) {
		if(!location.isFree()) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("x", location.getCoordinates().getX()).add("y", location.getCoordinates().getY());
			
			addAgentIfPresent(builder, location);
			addUserIfPresent(builder, location);
			addDirtIfPresent(builder, location);
			
			return builder.build();
		}
		else {
			return null;
		}
	}

	private static void addDirtIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		String dirt = buildDirtIfPresent(location);
		
		if(dirt != null) {
			builder.add("dirt", dirt);
		}
	}

	private static void addUserIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		JsonObject user = buildUserIfPresent(location);
		
		if(user != null) {
			builder.add("user", user);
		}
	}

	private static void addAgentIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		JsonObject agent = buildAgentIfPresent(location);
		
		if(agent != null) {
			builder.add("agent", agent);
		}
	}

	private static JsonObject buildUserIfPresent(VacuumWorldLocation location) {
		if(location.isAUserPresent()) {
			return buildUser(location);
		}
		else {
			return null;
		}
	}

	private static JsonObject buildUser(VacuumWorldLocation location) {
		JsonObjectBuilder userBuilder = Json.createObjectBuilder();
		
		User user = location.getUser();
		String id = user.getId();
		String name = user.getExternalAppearance().getName();
		String facingDirection = user.getFacingDirection().toString().toLowerCase();
		
		userBuilder.add("id", id).add("name", name).add("facing_direction", facingDirection);
		
		return userBuilder.build();
	}

	private static String buildDirtIfPresent(VacuumWorldLocation location) {
		if(location.isDirtPresent()) {
			return location.getDirt().getExternalAppearance().getDirtType().toString().toLowerCase();
		}
		else {
			return null;
		}
	}

	private static JsonObject buildAgentIfPresent(VacuumWorldLocation location) {
		if(location.isAnAgentPresent()) {
			return buildAgent(location);
		}
		else {
			return null;
		}
	}

	private static JsonObject buildAgent(VacuumWorldLocation location) {
		JsonObjectBuilder agentBuilder = Json.createObjectBuilder();
		
		VacuumWorldCleaningAgent agent = location.getAgent();
		String id = agent.getId();
		String name = agent.getExternalAppearance().getName();
		String color = agent.getExternalAppearance().getType().toString().toLowerCase();
		int sensorsNumber = 2;
		int actuatorsNumber = 2;
		int width = 1;
		int height = 1;
		String facingDirection = agent.getFacingDirection().toString().toLowerCase();
		
		agentBuilder.add("id", id).add("name", name).add("color", color).add("sensors", sensorsNumber).add("actuators", actuatorsNumber).add("width", width).add("height", height).add("facing_direction", facingDirection);
		
		return agentBuilder.build();
	}
}