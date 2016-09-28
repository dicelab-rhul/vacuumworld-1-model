package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldRandomMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSmartRandomSocialMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.AgentAwarenessRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.view.ViewRequest;

public class InitialStateParser {
	private static final String MIND_TO_USE = "RANDOM_SOCIAL";
	private static Map<String, Class<? extends VacuumWorldDefaultMind>> admissibleMindTypes = initAdmissibleMindTypes();
	private static Class<? extends VacuumWorldDefaultMind> mindClassToUse = InitialStateParser.admissibleMindTypes.get(InitialStateParser.MIND_TO_USE);

	
	private InitialStateParser() {}
	
	private static Map<String, Class<? extends VacuumWorldDefaultMind>> initAdmissibleMindTypes() {
		Map<String, Class<? extends VacuumWorldDefaultMind>>  mindTypes = new HashMap<>();
		
		mindTypes.put("RANDOM_SOCIAL", VacuumWorldSmartRandomSocialMind.class);
		mindTypes.put("RANDOM", VacuumWorldRandomMind.class);
		
		return mindTypes;
	}

	public static VacuumWorldMonitoringContainer parseInitialState(InputStream input) throws IOException {
		JsonObject json = null;
		
		if(input instanceof ObjectInputStream) {
			json = parseInitialStateFromController((ObjectInputStream) input);
		}
		else if(input instanceof FileInputStream) {
			json = parseInitialStateFromFile((FileInputStream) input);
		}
		
		return parseInitialState(json);
	}

	private static VacuumWorldMonitoringContainer parseInitialState(JsonObject json) {
		VacuumWorldSpace space = createInitialState(json);
		VacuumWorldMonitoringPhysics monitoringPhysics = new VacuumWorldMonitoringPhysics();

		return new VacuumWorldMonitoringContainer(monitoringPhysics, space);
	}

	private static VacuumWorldSpace createInitialState(JsonObject json) {
		if(json == null) {
			return null;
		}
		else {
			return createInitialStateHelper(json);
		}
	}

	private static VacuumWorldSpace createInitialStateHelper(JsonObject json) {
		int width = json.getInt("width");
		int height = json.getInt("height");
		boolean user = json.getBoolean("user");
		boolean monitoring = json.getBoolean("monitoring");

		List<VacuumWorldLocation> notableLocations = getNotableLocations(json, width, height);

		return createInitialState(notableLocations, width, height, user, monitoring);
	}

	private static VacuumWorldSpace createInitialState(List<VacuumWorldLocation> notableLocations, int width, int height, boolean user, boolean monitoring) {
		Map<LocationKey, Location> spaceMap = new HashMap<>();

		for (VacuumWorldLocation location : notableLocations) {
			spaceMap.put(location.getCoordinates(), location);
		}

		return fillState(spaceMap, width, height, user, monitoring);
	}

	private static VacuumWorldSpace fillState(Map<LocationKey, Location> spaceMap, int width, int height, boolean user, boolean monitoring) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				putNewLocationIfNecessary(spaceMap, i, j, width, height);
			}
		}
		return packState(spaceMap, width, height, user, monitoring);
	}

	private static VacuumWorldSpace packState(Map<LocationKey, Location> spaceMap, int width, int height, boolean user, boolean monitoring) {
		int[] dimensions = new int[] {width, height};

		return new VacuumWorldSpace(dimensions, spaceMap, user, monitoring);
	}

	private static void putNewLocationIfNecessary(Map<LocationKey, Location> spaceMap, int i, int j, int width, int height) {
		VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);

		if (!spaceMap.containsKey(coordinates)) {
			VacuumWorldLocationType type = VacuumWorldLocationType.NORMAL;
			VacuumWorldLocation location = new VacuumWorldLocation(coordinates, type, width - 1, height - 1);
			spaceMap.put(coordinates, location);
		}
	}

	private static List<VacuumWorldLocation> getNotableLocations(JsonObject json, int width, int height) {
		List<VacuumWorldLocation> locations = new ArrayList<>();
		JsonArray notableLocations = json.getJsonArray("notable_locations");

		for (JsonValue value : notableLocations) {
			if (value instanceof JsonObject) {
				locations.add(parseLocation((JsonObject) value, width, height));
			}
		}

		return locations;
	}

	private static VacuumWorldLocation parseLocation(JsonObject value, int width, int height) {
		int x = value.getInt("x");
		int y = value.getInt("y");
		VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(x - 1, y - 1);
		
		VacuumWorldCleaningAgent agent = parseAgentIfPresent(value);
		Dirt dirt = parseDirtIfPresent(value);

		return createLocation(coordinates, agent, dirt, width, height);
	}

	private static VacuumWorldLocation createLocation(VacuumWorldCoordinates coordinates, VacuumWorldCleaningAgent agent, Dirt dirt, int width, int height) {
		VacuumWorldLocation location = new VacuumWorldLocation(coordinates, VacuumWorldLocationType.NORMAL, width - 1, height - 1);

		if (agent != null) {
			agent.setCurrentLocation(coordinates);
		}

		location.addAgent(agent);
		location.setDirt(dirt);

		return location;
	}

	private static Dirt parseDirtIfPresent(JsonObject value) {
		if(value.containsKey("dirt")) {
			JsonString dirtString = value.getJsonString("dirt");
			return parseDirt(dirtString);
		}
		else {
			return null;
		}
	}

	private static Dirt parseDirt(JsonString dirtString) {
		if (dirtString == null) {
			return null;
		}
		else {
			return parseDirt(dirtString.getString());
		}
	}

	private static Dirt parseDirt(String dirtString) {
		DirtType type = DirtType.fromString(dirtString);
		Double[] dimensions = new Double[] { (double) 1, (double) 1 };
		String name = "Dirt";
		DirtAppearance appearance = new DirtAppearance(name, dimensions, type);

		return new Dirt(appearance);
	}

	private static VacuumWorldCleaningAgent parseAgentIfPresent(JsonObject value) {
		if(value.containsKey("agent")) {
			JsonObject agentObject = value.getJsonObject("agent");
			return parseAgent(agentObject);
		}
		else {
			return null;
		}
	}

	private static VacuumWorldCleaningAgent parseAgent(JsonObject agentObject) {
		if (agentObject == null) {
			return null;
		}
		else {
			return parseNotNullAgent(agentObject);
		}
	}

	private static VacuumWorldCleaningAgent parseNotNullAgent(JsonObject agentObject) {
		String id = agentObject.getString("id");
		String name = agentObject.getString("name");
		String color = agentObject.getString("color");

		int sensorsNumber = agentObject.getInt("sensors");
		int actuatorsNumber = agentObject.getInt("actuators");
		int width = agentObject.getInt("width");
		int height = agentObject.getInt("height");

		AgentFacingDirection agentFacingDirection = AgentFacingDirection.fromString(agentObject.getString("facing_direction"));
		Double[] dimensions = new Double[] {(double) width, (double) height};

		return createAgent(id, name, color, sensorsNumber, actuatorsNumber, dimensions, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, String name, String color, int sensorsNumber, int actuatorsNumber, Double[] dimensions, AgentFacingDirection agentFacingDirection) {
		List<Sensor> sensors = createSensors(sensorsNumber);
		List<Actuator> actuators = createActuators(actuatorsNumber);

		VacuumWorldAgentType type = VacuumWorldAgentType.fromString(color);
		AbstractAgentAppearance appearance = new VacuumWorldAgentAppearance(name, dimensions, type);

		return createAgent(id, appearance, type, sensors, actuators, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, AbstractAgentAppearance appearance, VacuumWorldAgentType type, List<Sensor> sensors, List<Actuator> actuators, AgentFacingDirection agentFacingDirection) {
		try {
			VacuumWorldDefaultMind mind = InitialStateParser.mindClassToUse.getConstructor(AgentAwarenessRepresentation.class).newInstance(createAwareness(actuators, sensors, id, type));			
			VacuumWorldDefaultBrain brain = new VacuumWorldDefaultBrain(mind.getClass());

			VacuumWorldCleaningAgent agent = new VacuumWorldCleaningAgent(appearance, sensors, actuators, mind, brain, agentFacingDirection);
			agent.setId(id);

			return agent;
		}
		catch (Exception e) {
			Utils.log(e);
			return null;
		}
	}
	
	private static Object createAwareness(List<Actuator> actuators, List<Sensor> sensors, String id, VacuumWorldAgentType type) {
		List<String> aids = createAids(actuators);
		List<String> earids = createEarids(sensors);
		List<String> eyeids = createEyeids(sensors);
		
		return new AgentAwarenessRepresentation(id, aids, earids, eyeids, type);
	}

	private static List<String> createEyeids(List<Sensor> sensors) {
		return createSids(sensors);
	}

	private static List<String> createEarids(List<Sensor> sensors) {
		return createSids(sensors);
	}

	private static List<String> createSids(List<Sensor> sensors) {
		List<String> sids = new ArrayList<>();
		sensors.forEach((Sensor sensor) -> sids.add(((VacuumWorldDefaultSensor) sensor).getSensorId()));
		
		return sids;
	}

	private static List<String> createAids(List<Actuator> actuators) {
		List<String> aids = new ArrayList<>();
		actuators.forEach((Actuator actuator) -> aids.add(((VacuumWorldDefaultActuator) actuator).getActuatorId()));
		
		return aids;
	}

	private static List<Actuator> createActuators(int actuatorsNumber) {
		List<Actuator> actuators = new ArrayList<>();

		if (actuatorsNumber <= 0) {
			return actuators;
		}

		for (int i = 0; i < actuatorsNumber; i++) {
			actuators.add(new VacuumWorldDefaultActuator());
		}

		return actuators;
	}

	private static List<Sensor> createSensors(int sensorsNumber) {
		List<Sensor> sensors = new ArrayList<>();

		if (sensorsNumber <= 0) {
			return sensors;
		}

		for (int i = 0; i < sensorsNumber; i++) {
			sensors.add(new VacuumWorldDefaultSensor());
		}

		return sensors;
	}

	private static JsonObject parseInitialStateFromFile(FileInputStream input) {
		JsonReader reader = Json.createReader(input);
		JsonObject json = reader.readObject();
		reader.close();
		
		return json;
	}

	private static JsonObject parseInitialStateFromController(ObjectInputStream input) throws IOException {
		try {
			ViewRequest viewRequest = (ViewRequest) input.readObject();
			return parseInitialStateFromController(viewRequest, input);
		}
		catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	private static JsonObject parseInitialStateFromController(ViewRequest viewRequest, ObjectInputStream input) {
		switch(viewRequest.getCode()) {
		case NEW:
			return Utils.parseJsonObjectFromString((String) viewRequest.getPayload());
		case LOAD_TEMPLATE:
			return parseJsonObjectFromTemplate(input);
		case LOAD_TEMPLATE_FROM_FILE:
			return parseJsonObjectFromFile(input);
		default:
			return null;
		}
	}

	private static JsonObject parseJsonObjectFromTemplate(ObjectInputStream input) {
		// TODO
		
		return null;
	}

	private static JsonObject parseJsonObjectFromFile(ObjectInputStream input) {
		//TODO
		
		return null;
	}
}