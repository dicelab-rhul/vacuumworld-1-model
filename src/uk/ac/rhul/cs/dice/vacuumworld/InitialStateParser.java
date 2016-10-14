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
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldRandomMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSmartRandomSocialMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.manhattan.VacuumWorldManhattanMind;
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
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequest;

public class InitialStateParser {
	private static Map<String, String> colorToMindMap = initColorToMindMap();
	private static Map<String, Class<? extends VacuumWorldDefaultMind>> admissibleMindTypes = initAdmissibleMindTypes();
	
	private InitialStateParser() {}
	
	private static Map<String, String> initColorToMindMap() {
		Map<String, String> colorsToMindMap = new HashMap<>();
		
		colorsToMindMap.put("green", VacuumWorldManhattanMind.getName());
		colorsToMindMap.put("orange", VacuumWorldManhattanMind.getName());
		colorsToMindMap.put("white", VacuumWorldSmartRandomSocialMind.getName());
		
		return colorsToMindMap;
	}

	private static Map<String, Class<? extends VacuumWorldDefaultMind>> initAdmissibleMindTypes() {
		Map<String, Class<? extends VacuumWorldDefaultMind>>  mindTypes = new HashMap<>();
		
		mindTypes.put(VacuumWorldSmartRandomSocialMind.getName(), VacuumWorldSmartRandomSocialMind.class);
		mindTypes.put(VacuumWorldRandomMind.getName(), VacuumWorldRandomMind.class);
		mindTypes.put(VacuumWorldManhattanMind.getName(), VacuumWorldManhattanMind.class);
		
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

	private static VacuumWorldMonitoringContainer parseInitialState(JsonObject json) throws IOException {
		VacuumWorldSpace space = createInitialState(json);
		checkAgentsNumber(space);
		
		VacuumWorldMonitoringPhysics monitoringPhysics = new VacuumWorldMonitoringPhysics();

		return new VacuumWorldMonitoringContainer(monitoringPhysics, space);
	}

	private static void checkAgentsNumber(VacuumWorldSpace space) throws IOException {
		if(space.getAgents() == null) {
			throw new IOException(Utils.INVALID_INITIAL_STATE);
		}
		
		if(space.getAgents().isEmpty()) {
			throw new IOException(Utils.INVALID_INITIAL_STATE);
		}
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
		List<Sensor<VacuumWorldSensorRole>> sensors = createSensors(sensorsNumber, id);
		List<Actuator<VacuumWorldActuatorRole>> actuators = createActuators(actuatorsNumber, id);

		VacuumWorldAgentType type = VacuumWorldAgentType.fromString(color);
		AbstractAgentAppearance appearance = new VacuumWorldAgentAppearance(name, dimensions, type);

		return createAgent(id, appearance, sensors, actuators, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, AbstractAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, AgentFacingDirection agentFacingDirection) {
		try {
			String color = ((VacuumWorldAgentAppearance) appearance).getType().toString().toLowerCase();
			VacuumWorldDefaultMind mind = InitialStateParser.admissibleMindTypes.get(InitialStateParser.colorToMindMap.get(color)).getConstructor().newInstance();
			mind.setBodyId(id);		
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
	
	protected static Object createAwareness(List<Actuator<VacuumWorldActuatorRole>> actuators, List<Sensor<VacuumWorldSensorRole>> sensors, String id, VacuumWorldAgentType type) {
		List<String> aids = createAids(actuators);
		List<String> earids = createEarids(sensors);
		List<String> eyeids = createEyeids(sensors);
		
		return new AgentAwarenessRepresentation(id, aids, earids, eyeids, type);
	}

	private static List<String> createEyeids(List<Sensor<VacuumWorldSensorRole>> sensors) {
		return createSids(sensors);
	}

	private static List<String> createEarids(List<Sensor<VacuumWorldSensorRole>> sensors) {
		return createSids(sensors);
	}

	private static List<String> createSids(List<Sensor<VacuumWorldSensorRole>> sensors) {
		List<String> sids = new ArrayList<>();
		sensors.forEach((Sensor<VacuumWorldSensorRole> sensor) -> sids.add(((VacuumWorldDefaultSensor) sensor).getSensorId()));
		
		return sids;
	}

	private static List<String> createAids(List<Actuator<VacuumWorldActuatorRole>> actuators) {
		List<String> aids = new ArrayList<>();
		actuators.forEach((Actuator<VacuumWorldActuatorRole> actuator) -> aids.add(((VacuumWorldDefaultActuator) actuator).getActuatorId()));
		
		return aids;
	}

	private static List<Actuator<VacuumWorldActuatorRole>> createActuators(int actuatorsNumber, String bodyId) {
		List<Actuator<VacuumWorldActuatorRole>> actuators = new ArrayList<>();

		if (actuatorsNumber <= 0) {
			return actuators;
		}
		
		if(actuatorsNumber >= 1) {
			actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.PHYSICAL_ACTUATOR));
		}
		
		if(actuatorsNumber >= 2) {
			actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.SPEAKING_ACTUATOR));
		}
		
		if(actuatorsNumber >= 3) {
			addOtherActuators(actuatorsNumber, bodyId, actuators);
		}

		return actuators;
	}

	private static void addOtherActuators(int actuatorsNumber, String bodyId, List<Actuator<VacuumWorldActuatorRole>> actuators) {
		for (int i = 0; i < actuatorsNumber - 2; i++) {
			actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.UNDEFINED));
		}
	}

	private static List<Sensor<VacuumWorldSensorRole>> createSensors(int sensorsNumber, String bodyId) {
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();

		if (sensorsNumber <= 0) {
			return sensors;
		}
		
		if(sensorsNumber >= 1) {
			sensors.add(new VacuumWorldDefaultSensor(bodyId, VacuumWorldSensorRole.SEEING_SENSOR));
		}
		
		if(sensorsNumber >= 2) {
			sensors.add(new VacuumWorldDefaultSensor(bodyId, VacuumWorldSensorRole.LISTENING_SENSOR));
		}
		
		if(sensorsNumber >= 3) {
			addOtherSensors(sensorsNumber, bodyId, sensors);
		}
		
		return sensors;
	}

	private static List<Sensor<VacuumWorldSensorRole>> addOtherSensors(int sensorsNumber, String bodyId, List<Sensor<VacuumWorldSensorRole>> sensors) {
		for (int i = 0; i < sensorsNumber - 2; i++) {
			sensors.add(new VacuumWorldDefaultSensor(bodyId, VacuumWorldSensorRole.UNDEFINED));
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
		case LOAD_TEMPLATE_FROM_FILE:
			return parseJsonObjectFromFile(input);
		default:
			return null;
		}
	}

	private static JsonObject parseJsonObjectFromFile(ObjectInputStream input) {
		try {
			input.readObject();
			//TODO
		}
		catch(Exception e) {
			Utils.log(e);
		}
		
		return null;
	}
}