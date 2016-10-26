package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class StateRepresentationBuilder {
	
	private StateRepresentationBuilder(){}
	
	public static JsonObject buildStateRepresentation(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("size", getSize(grid));
		builder.add("user", isUserPresent(grid));
		builder.add("locations", representLocations(grid));
		
		return builder.build();
	}
	
	private static JsonArray representLocations(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(VacuumWorldLocation location : grid.values()) {
			builder.add(buildLocation(location));
		}
		
		return builder.build();
	}

	private static JsonObject buildLocation(VacuumWorldLocation location) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("x", location.getCoordinates().getX());
		builder.add("y", location.getCoordinates().getY());
		builder.add("on_north", location.getNorthernLocationType().toString());
		builder.add("on_south", location.getSouthernLocationType().toString());
		builder.add("on_west", location.getWesternLocationType().toString());
		builder.add("on_east", location.getEasternLocationType().toString());
		
		if(location.isNotFree()) {
			addInfo(builder, location);
		}
		
		return builder.build();
	}

	private static void addInfo(JsonObjectBuilder builder, VacuumWorldLocation location) {
		if(location.isAnAgentPresent()) {
			builder.add("agent", buildAgent(location.getAgent()));
		}
		else if(location.isAUserPresent()) {
			builder.add("user", buildUser(location.getUser()));
		}
		
		if(location.isDirtPresent()) {
			builder.add("dirt", buildDirt(location.getDirt()));
		}
	}

	private static JsonObject buildAgent(VacuumWorldCleaningAgent agent) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("id", agent.getId());
		builder.add("name", agent.getExternalAppearance().getName());
		builder.add("mind", agent.getMind().getClass().getCanonicalName());
		builder.add("brain", agent.getBrain().getClass().getCanonicalName());
		builder.add("sensors", buildSensors(agent.getSensors()));
		builder.add("actuators", buildActuators(agent.getActuators()));
	
		return builder.build();
	}

	private static JsonObject buildUser(User user) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("id", user.getId());
		builder.add("name", user.getExternalAppearance().getName());
		builder.add("mind", user.getMind().getClass().getCanonicalName());
		builder.add("brain", user.getBrain().getClass().getCanonicalName());
		builder.add("sensors", buildSensors(user.getSensors()));
		builder.add("actuators", buildActuators(user.getActuators()));
	
		return builder.build();
	}

	private static JsonArray buildSensors(List<Sensor<VacuumWorldSensorRole>> sensors) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(Sensor<VacuumWorldSensorRole> sensor : sensors) {
			builder.add(buildSensor(sensor));
		}
		
		return builder.build();
	}

	private static JsonObject buildSensor(Sensor<VacuumWorldSensorRole> sensor) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("purpose", sensor.getRole().toString());
		
		return builder.build();
	}

	private static JsonArray buildActuators(List<Actuator<VacuumWorldActuatorRole>> actuators) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(Actuator<VacuumWorldActuatorRole> actuator : actuators) {
			builder.add(buildActuator(actuator));
		}
		
		return builder.build();
	}

	private static JsonObject buildActuator(Actuator<VacuumWorldActuatorRole> actuator) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("purpose", actuator.getRole().toString());
		
		return builder.build();
	}

	private static JsonObject buildDirt(Dirt dirt) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("color", dirt.getExternalAppearance().getDirtType().toString());
		builder.add("in_initial_state", dirt.isInInitialState());
		builder.add("dropped_in_cycle", dirt.getDropCycle());
		
		return builder.build();
	}

	private static boolean isUserPresent(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		return grid.values().stream().filter((VacuumWorldLocation location) -> location.isAUserPresent()).findAny().isPresent();
	}

	private static int getSize(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		if(grid.isEmpty()) {
			return 0;
		}
		else {
			return getSizeHelper(grid);
		}
	}

	private static int getSizeHelper(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		int maxValue = 0;
		int candidate;
		
		for(VacuumWorldCoordinates key : grid.keySet()) {
			candidate = Math.max(key.getX(), key.getY());
			
			if(candidate > maxValue) {
				maxValue = candidate;
			}
		}
		
		return maxValue; //need to add 1?
	}

	public static JsonObject buildCompactStateRepresentation(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("size", getSize(grid));
		builder.add("user", isUserPresent(grid));
		builder.add("locations", representNonEmptyLocations(grid));
		
		return builder.build();
	}

	private static JsonValue representNonEmptyLocations(Map<VacuumWorldCoordinates, VacuumWorldLocation> grid) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(VacuumWorldLocation location : grid.values()) {
			if(location.isNotFree()) {
				builder.add(buildLocation(location));
			}
		}
		
		return builder.build();
	}
}