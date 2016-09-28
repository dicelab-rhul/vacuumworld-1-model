package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.view.ModelUpdate;

public class JsonForControllerBuilder {
	private JsonForControllerBuilder() {}
	
	public static ModelUpdate createModelUpdate(VacuumWorldSpace space) {
		Map<LocationKey, Location> map = space.getGrid();
		
		JsonObject stateRepresentation = buildStateRepresentation(space.getDimensions(), map);
		return new ModelUpdate(stateRepresentation.toString());
	}

	private static JsonObject buildStateRepresentation(int[] dimensions, Map<LocationKey, Location> map) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("size", dimensions[0]);
		
		JsonArray locations = buildLocationsRepresentation(map);
		builder.add("notable_locations", locations);
		
		return builder.build();
	}

	private static JsonArray buildLocationsRepresentation(Map<LocationKey, Location> map) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(Location location : map.values()) {
			JsonObject loc = buildLocationIfNecessary(location);
			
			if(loc != null) {
				builder.add(loc);
			}
		}
		
		return builder.build();
	}

	private static JsonObject buildLocationIfNecessary(Location location) {
		if(!((VacuumWorldLocation) location).isFree()) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("x", ((VacuumWorldLocation) location).getCoordinates().getX()).add("y", ((VacuumWorldLocation) location).getCoordinates().getY());
			
			JsonObject agent = buildAgentIfPresent(location);
			
			if(agent != null) {
				builder.add("agent", agent);
			}
			
			String dirt = buildDirtIfPresent(location);
			
			if(dirt != null) {
				builder.add("dirt", dirt);
			}
			
			return builder.build();
		}
		else {
			return null;
		}
	}

	private static String buildDirtIfPresent(Location location) {
		if(((VacuumWorldLocation) location).isDirtPresent()) {
			return ((DirtAppearance)((VacuumWorldLocation) location).getDirt().getExternalAppearance()).getDirtType().toString().toLowerCase();
		}
		else {
			return null;
		}
	}

	private static JsonObject buildAgentIfPresent(Location location) {
		if(((VacuumWorldLocation) location).isAnAgentPresent()) {
			return buildAgent((VacuumWorldLocation) location);
		}
		else {
			return null;
		}
	}

	private static JsonObject buildAgent(VacuumWorldLocation location) {
		JsonObjectBuilder agentBuilder = Json.createObjectBuilder();
		
		VacuumWorldCleaningAgent agent = location.getAgent();
		String id = (String) agent.getId();
		String name = agent.getExternalAppearance().getName();
		String color = ((VacuumWorldAgentAppearance) agent.getExternalAppearance()).getType().toString().toLowerCase();
		int sensorsNumber = 2;
		int actuatorsNumber = 2;
		int width = 1;
		int height = 1;
		String facingDirection = agent.getFacingDirection().toString().toLowerCase();
		
		agentBuilder.add("id", id).add("name", name).add("color", color).add("sensors", sensorsNumber).add("actuators", actuatorsNumber).add("width", width).add("height", height).add("facing_direction", facingDirection);
		
		return agentBuilder.build();
	}
}