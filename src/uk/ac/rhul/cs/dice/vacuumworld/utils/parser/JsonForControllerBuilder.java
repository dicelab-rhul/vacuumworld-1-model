package uk.ac.rhul.cs.dice.vacuumworld.utils.parser;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelMessagesEnum;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelUpdate;

/**
 * This class creates a ModelUpdate for the View (to be transmitted through the Controller).
 * Only the relevant locations are included and, for each location, only the relevant information about agent/user/dirt are included.
 * A location is relevant if it contains an agent/dirt/user (in other words, if it is not empty).
 * For a VacuumWorldLocation, the relevant information are the x and y coordinates, the agent (if present), the user (if present) and the dirt (if present).
 * For a VacuumWorldCleaningAgent, the relevant information are its color and its facing direction.
 * For a User, only the facing direction is relevant.
 * For a Dirt, only the color is relevant.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana
 *
 */
public class JsonForControllerBuilder {
	private JsonForControllerBuilder() {}
	
	/**
	 * Creates the ModelUpdate.
	 * 
	 * @param space: the VacuumWorldSpace.
	 * @return the ModelUpdate.
	 */
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
			if(location.isNotFree()) {
				builder.add(buildLocation(location));
			}
		}
		
		return builder.build();
	}

	private static JsonObject buildLocation(VacuumWorldLocation location) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("x", location.getCoordinates().getX()).add("y", location.getCoordinates().getY());
		
		addAgentIfPresent(builder, location);
		addUserIfPresent(builder, location);
		addDirtIfPresent(builder, location);
		
		return builder.build();
	}

	private static void addDirtIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		if(location.isDirtPresent()) {
			builder.add("dirt", buildDirt(location));
		}
	}

	private static void addUserIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		if(location.isAUserPresent()) {
			builder.add("user", buildUser(location));
		}
	}

	private static void addAgentIfPresent(JsonObjectBuilder builder, VacuumWorldLocation location) {
		if(location.isAnAgentPresent()) {
			builder.add("agent", buildAgent(location));
		}
	}

	private static String buildUser(VacuumWorldLocation location) {
		return location.getUser().getFacingDirection().toString().toLowerCase();
	}

	private static String buildDirt(VacuumWorldLocation location) {
		return location.getDirt().getExternalAppearance().getDirtType().toString().toLowerCase();
	}

	private static JsonObject buildAgent(VacuumWorldLocation location) {
		JsonObjectBuilder agentBuilder = Json.createObjectBuilder();
		
		VacuumWorldCleaningAgent agent = location.getAgent();
		String color = agent.getExternalAppearance().getType().toString().toLowerCase();
		String facingDirection = agent.getFacingDirection().toString().toLowerCase();
		
		agentBuilder.add("color", color).add("facing_direction", facingDirection);
		
		return agentBuilder.build();
	}
}