package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserBrain;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserSensor;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentBrain;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequest;

public class InitialStateParser {
	
	private InitialStateParser() {}

	public static VacuumWorldSpace parseInitialState(InputStream input) throws IOException {
		JsonObject json = null;
		
		if(input instanceof ObjectInputStream) {
			json = parseInitialStateFromController((ObjectInputStream) input);
		}
		else if(input instanceof FileInputStream) {
			json = parseInitialStateFromFile((FileInputStream) input);
		}
		
		return parseInitialState(json);
	}

	private static VacuumWorldSpace parseInitialState(JsonObject json) throws IOException {
		VacuumWorldSpace space = createInitialState(json);
		checkAgentsNumber(space);
		
		return space;
	}

	private static void checkAgentsNumber(VacuumWorldSpace space) throws IOException {
		if(space.getAgents() == null) {
			throw new IOException(VWUtils.INVALID_INITIAL_STATE);
		}
		
		if(space.getAgents().isEmpty()) {
			throw new IOException(VWUtils.INVALID_INITIAL_STATE);
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

	private static VacuumWorldSpace packState(Map<LocationKey, Location> spaceMap, int width, int height, boolean userPresent, boolean monitoring) {
		int[] dimensions = new int[] {width, height};
		User user = createAndInitUser(userPresent, spaceMap);
		
		return new VacuumWorldSpace(dimensions, spaceMap, user, monitoring);
	}

	private static User createAndInitUser(boolean userPreent, Map<LocationKey, Location> spaceMap) {
		User user = createUser(userPreent);
		
		return user == null ? null : setupUser(user, spaceMap);
	}

	private static User setupUser(User user, Map<LocationKey, Location> spaceMap) {
		VacuumWorldCoordinates coordinates = getRandomFreeCoordinates(spaceMap);
		
		if(coordinates != null) {
			user.setCurrentLocation(coordinates);
			((VacuumWorldLocation) spaceMap.get(coordinates)).addUser(user);
			
			return user;
		}
		else {
			return null;
		}
	}

	private static VacuumWorldCoordinates getRandomFreeCoordinates(Map<LocationKey, Location> spaceMap) {
		List<VacuumWorldCoordinates> coordinatesList = spaceMap.values().stream().map((Location location) -> (VacuumWorldLocation) location).filter(VacuumWorldLocation::isFree).map(VacuumWorldLocation::getCoordinates).collect(Collectors.toList());
		Collections.shuffle(coordinatesList);
		
		try {
			return coordinatesList.get(0);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			VWUtils.fakeLog(e);
			
			return null;
		}
	}

	private static User createUser(boolean userPresent) {
		if(!userPresent) {
			return null;
		}
		
		String id = "User-" + UUID.randomUUID().toString();
		UserMind mind = new UserMind(id);
		UserBrain brain = new UserBrain();
		UserSensor seeingSensor = new UserSensor(id, VacuumWorldSensorRole.SEEING_SENSOR);
		UserSensor listeningSensor = new UserSensor(id, VacuumWorldSensorRole.LISTENING_SENSOR);
		UserActuator physicalActuator = new UserActuator(id, VacuumWorldActuatorRole.PHYSICAL_ACTUATOR);
		UserActuator speakingActuator = new UserActuator(id, VacuumWorldActuatorRole.SPEAKING_ACTUATOR);
		UserAppearance appearance = new UserAppearance(id, new Double[] {(double) 1, (double) 1});
		
		User user = new User(appearance, Arrays.asList(seeingSensor, listeningSensor), Arrays.asList(physicalActuator, speakingActuator), mind, brain, ActorFacingDirection.random());
		user.setId(id);
		
		return user;
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

		ActorFacingDirection agentFacingDirection = ActorFacingDirection.fromString(agentObject.getString("facing_direction"));
		Double[] dimensions = new Double[] {(double) width, (double) height};

		return createAgent(id, name, color, sensorsNumber, actuatorsNumber, dimensions, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, String name, String color, int sensorsNumber, int actuatorsNumber, Double[] dimensions, ActorFacingDirection agentFacingDirection) {
		List<Sensor<VacuumWorldSensorRole>> sensors = createSensors(sensorsNumber, id, VacuumWorldDefaultSensor.class);
		List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> actuators = createActuators(actuatorsNumber, id);

		VacuumWorldAgentType type = VacuumWorldAgentType.fromString(color);
		VacuumWorldAgentAppearance appearance = new VacuumWorldAgentAppearance(name, dimensions, type);

		return createAgent(id, appearance, sensors, actuators, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, VacuumWorldAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> actuators, ActorFacingDirection agentFacingDirection) {
		try {
			String color = appearance.getType().toString().toLowerCase();
			VacuumWorldDefaultMind mind = ConfigData.getMindClassFromColor(color).getConstructor(String.class).newInstance(id);
			VacuumWorldDefaultBrain brain = new VacuumWorldDefaultBrain(mind.getClass());
			VacuumWorldCleaningAgent agent = new VacuumWorldCleaningAgent(appearance, sensors, actuators, mind, brain, agentFacingDirection);
			agent.setId(id);

			return agent;
		}
		catch (Exception e) {
			VWUtils.log(e);
			
			return null;
		}
	}

	private static List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> createActuators(int actuatorsNumber, String bodyId) {
		List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> actuators = new ArrayList<>();

		if (actuatorsNumber <= 0) {
			return actuators;
		}
		
		try {
			if(actuatorsNumber >= 1) {
				actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.PHYSICAL_ACTUATOR));
			}
			
			if(actuatorsNumber >= 2) {
				actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.SPEAKING_ACTUATOR));
			}
			
			if(actuatorsNumber >= 3) {
				addOtherActuators(actuatorsNumber, bodyId, actuators);
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
		}

		return actuators;
	}

	private static void addOtherActuators(int actuatorsNumber, String bodyId, List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> actuators) {
		for (int i = 0; i < actuatorsNumber - 2; i++) {
			actuators.add(new VacuumWorldDefaultActuator(bodyId, VacuumWorldActuatorRole.UNDEFINED));
		}
	}
	
	private static List<Actuator<VacuumWorldActuatorRole, VacuumWorldMonitoringPerception>> createMonitoringActuators(int actuatorsNumber, String bodyId) {
		List<Actuator<VacuumWorldActuatorRole, VacuumWorldMonitoringPerception>> actuators = new ArrayList<>();

		if (actuatorsNumber <= 0) {
			return actuators;
		}
		
		try {
			if(actuatorsNumber >= 1) {
				actuators.add(new VacuumWorldMonitoringAgentActuator(bodyId, VacuumWorldActuatorRole.PHYSICAL_ACTUATOR));
			}
			
			if(actuatorsNumber >= 2) {
				actuators.add(new VacuumWorldMonitoringAgentActuator(bodyId, VacuumWorldActuatorRole.SPEAKING_ACTUATOR));
			}
			
			if(actuatorsNumber >= 3) {
				addOtherMonitoringActuators(actuatorsNumber, bodyId, actuators);
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
		}

		return actuators;
	}

	private static void addOtherMonitoringActuators(int actuatorsNumber, String bodyId, List<Actuator<VacuumWorldActuatorRole, VacuumWorldMonitoringPerception>> actuators) {
		for (int i = 0; i < actuatorsNumber - 2; i++) {
			actuators.add(new VacuumWorldMonitoringAgentActuator(bodyId, VacuumWorldActuatorRole.UNDEFINED));
		}
	}

	private static List<Sensor<VacuumWorldSensorRole>> createSensors(int sensorsNumber, String bodyId, Class<? extends Sensor<VacuumWorldSensorRole>> realClass) {
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();

		if (sensorsNumber <= 0) {
			return sensors;
		}
		
		try {
			if(sensorsNumber >= 1) {
				sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.SEEING_SENSOR));
			}
			
			if(sensorsNumber >= 2) {
				sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.LISTENING_SENSOR));
			}
			
			if(sensorsNumber >= 3) {
				addOtherSensors(sensorsNumber, bodyId, sensors, realClass);
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
		}
		
		return sensors;
	}

	private static void addOtherSensors(int sensorsNumber, String bodyId, List<Sensor<VacuumWorldSensorRole>> sensors, Class<? extends Sensor<VacuumWorldSensorRole>> realClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (int i = 0; i < sensorsNumber - 2; i++) {
			sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.UNDEFINED));
		}
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
			return VWUtils.parseJsonObjectFromString((String) viewRequest.getPayload());
		case LOAD_TEMPLATE_FROM_FILE: //for now this cannot happen.
			return parseJsonObjectFromFile(input);
		default:
			return null;
		}
	}

	private static JsonObject parseJsonObjectFromFile(ObjectInputStream input) {
		try {
			input.readObject();
			//this is incomplete.
		}
		catch(Exception e) {
			VWUtils.log(e);
		}
		
		return null; //change this upon implementation.
	}

	public static VacuumWorldMonitoringContainer createMonitoringContainer() {
		VacuumWorldMonitoringContainer container = new VacuumWorldMonitoringContainer();
		List<VacuumWorldMonitoringAgent> monitoringAgents = new ArrayList<>();
		
		for(int i=0; i < ConfigData.getMonitoringAgentsNumber(); i++) {
			VacuumWorldMonitoringAgent agent = createMonitoringAgent();
			monitoringAgents.add(agent);
		}
		
		container.addMonitoringAgents(monitoringAgents);
		
		return container;
	}

	private static VacuumWorldMonitoringAgent createMonitoringAgent() {
		String bodyId = "Monitor-" + UUID.randomUUID().toString();
		VacuumWorldMonitoringAgentMind mind = new VacuumWorldMonitoringAgentMind(bodyId);
		VacuumWorldMonitoringAgentBrain brain = new VacuumWorldMonitoringAgentBrain();
		List<Sensor<VacuumWorldSensorRole>> sensors = createSensors(2, bodyId, VacuumWorldMonitoringAgentSensor.class);
		List<Actuator<VacuumWorldActuatorRole, VacuumWorldMonitoringPerception>> actuators = createMonitoringActuators(2, bodyId);
		Double[] dimensions = new Double[] {(double) 1, (double) 1};
		VacuumWorldMonitoringAgentAppearance appearance = new VacuumWorldMonitoringAgentAppearance(bodyId, dimensions);
		
		VacuumWorldMonitoringAgent agent = new VacuumWorldMonitoringAgent(appearance, sensors, actuators, mind, brain);
		agent.setId(bodyId);
		
		return agent;
	}
}