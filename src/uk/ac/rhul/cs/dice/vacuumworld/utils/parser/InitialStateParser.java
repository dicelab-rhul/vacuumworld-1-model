package uk.ac.rhul.cs.dice.vacuumworld.utils.parser;

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
import javax.json.JsonValue;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
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
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
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
		JsonObject json = retrieveJsonObject(input);
		
		if(json != null) {
			return parseInitialState(json);
		}
		else {
			throw new IOException("Corrupted initial state.");
		}
	}

	private static JsonObject retrieveJsonObject(InputStream input) throws IOException {
		if(input instanceof ObjectInputStream) {
			return retrieveJsonObjectFromController((ObjectInputStream) input);
		}
		else if(input instanceof FileInputStream) {
			return retrieveJsonObjectFromFile((FileInputStream) input);
		}
		else {
			return null;
		}
	}

	private static VacuumWorldSpace parseInitialState(JsonObject json) throws IOException {
		VacuumWorldSpace space = createInitialState(json);
		space.setJsonRepresentation(json);
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
		int width = json.getInt("width");
		int height = json.getInt("height");
		boolean user = json.getBoolean("user");

		List<VacuumWorldLocation> notableLocations = getNotableLocations(json, width, height);

		return createInitialState(notableLocations, width, height, user);
	}

	private static VacuumWorldSpace createInitialState(List<VacuumWorldLocation> notableLocations, int width, int height, boolean user) {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap = new HashMap<>();

		for (VacuumWorldLocation location : notableLocations) {
			spaceMap.put(location.getCoordinates(), location);
		}

		return fillState(spaceMap, width, height, user);
	}

	private static VacuumWorldSpace fillState(Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap, int width, int height, boolean user) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				putNewLocationIfNecessary(spaceMap, i, j, width, height);
			}
		}
		return packState(spaceMap, width, height, user);
	}

	private static VacuumWorldSpace packState(Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap, int width, int height, boolean userPresent) {
		int[] dimensions = new int[] {width, height};
		User user = createAndInitUser(userPresent, spaceMap);
		
		return new VacuumWorldSpace(dimensions, spaceMap, user);
	}

	private static User createAndInitUser(boolean userPreent, Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap) {
		User user = createUser(userPreent);
		
		return user == null ? null : setupUser(user, spaceMap);
	}

	private static User setupUser(User user, Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap) {
		VacuumWorldCoordinates coordinates = getRandomFreeCoordinates(spaceMap);
		
		if(coordinates != null) {
			user.setCurrentLocation(coordinates);
			spaceMap.get(coordinates).addUser(user);
			
			return user;
		}
		else {
			return null;
		}
	}

	private static VacuumWorldCoordinates getRandomFreeCoordinates(Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap) {
		List<VacuumWorldCoordinates> coordinatesList = spaceMap.values().stream().filter(VacuumWorldLocation::isFree).map(VacuumWorldLocation::getCoordinates).collect(Collectors.toList());
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
		else {
			return createUser();
		}
	}

	private static User createUser() {
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

	private static void putNewLocationIfNecessary(Map<VacuumWorldCoordinates, VacuumWorldLocation> spaceMap, int i, int j, int width, int height) {
		VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);

		if (!spaceMap.containsKey(coordinates)) {
			VacuumWorldLocationType type = VacuumWorldLocationType.NORMAL;
			VacuumWorldLocation location = new VacuumWorldLocation(coordinates, type, width - 1, height - 1);
			spaceMap.put(coordinates, location);
		}
	}

	public static List<VacuumWorldLocation> getNotableLocations(JsonObject json, int width, int height) {
		JsonArray notableLocations = json.getJsonArray("notable_locations");
		
		return notableLocations.stream().filter(value -> value instanceof JsonObject).map(value -> parseLocation((JsonObject) value, width, height)).collect(Collectors.toList());
	}

	private static VacuumWorldLocation parseLocation(JsonObject value, int width, int height) {
		VacuumWorldCoordinates coordinates = parseCoordinates(value);		
		VacuumWorldCleaningAgent agent = parseAgentIfPresent(value);
		Dirt dirt = parseDirtIfPresent(value);

		return createLocation(coordinates, agent, dirt, width, height);
	}

	private static VacuumWorldCoordinates parseCoordinates(JsonObject value) {
		int x = value.getInt("x");
		int y = value.getInt("y");
		
		return new VacuumWorldCoordinates(x - 1, y - 1);
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
		JsonValue dirtType = value.getOrDefault("dirt", null);
		
		if(dirtType != null && dirtType.getValueType() == JsonValue.ValueType.STRING) {
			return parseDirt(dirtType.toString());
		}
		else {
			return null;
		}
	}

	private static Dirt parseDirt(String dirtString) {
		DirtType type = DirtType.fromString(dirtString.replaceAll("\"", ""));
		Double[] dimensions = new Double[] { (double) 1, (double) 1 };
		String name = "Dirt";
		DirtAppearance appearance = new DirtAppearance(name, dimensions, type);

		return new Dirt(appearance, true, 0);
	}

	private static VacuumWorldCleaningAgent parseAgentIfPresent(JsonObject value) {
		JsonValue agent = value.getOrDefault("agent", null);
		
		if(agent != null && agent.getValueType() == JsonValue.ValueType.OBJECT) {
			return parseAgent((JsonObject) agent);
		}
		else {
			return null;
		}
	}

	private static VacuumWorldCleaningAgent parseAgent(JsonObject agentObject) {
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
		List<Actuator<VacuumWorldActuatorRole>> actuators = createActuators(actuatorsNumber, id, VacuumWorldDefaultActuator.class);

		VacuumWorldAgentType type = VacuumWorldAgentType.fromString(color);
		VacuumWorldAgentAppearance appearance = new VacuumWorldAgentAppearance(name, dimensions, type);

		return createAgent(id, appearance, sensors, actuators, agentFacingDirection);
	}

	private static VacuumWorldCleaningAgent createAgent(String id, VacuumWorldAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, ActorFacingDirection agentFacingDirection) {
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

	private static List<Actuator<VacuumWorldActuatorRole>> createActuators(int actuatorsNumber, String bodyId, Class<? extends Actuator<VacuumWorldActuatorRole>> classToUse) {
		if (actuatorsNumber < 2) {
			return new ArrayList<>();
		}
		else {
			return createActuatorsHelper(actuatorsNumber, bodyId, classToUse);
		}
	}

	private static List<Actuator<VacuumWorldActuatorRole>> createActuatorsHelper(int actuatorsNumber, String bodyId, Class<? extends Actuator<VacuumWorldActuatorRole>> classToUse) {
		List<Actuator<VacuumWorldActuatorRole>> actuators = new ArrayList<>();
		
		try {
			actuators.add(classToUse.getConstructor(String.class, VacuumWorldActuatorRole.class).newInstance(bodyId, VacuumWorldActuatorRole.PHYSICAL_ACTUATOR));
			actuators.add(classToUse.getConstructor(String.class, VacuumWorldActuatorRole.class).newInstance(bodyId, VacuumWorldActuatorRole.SPEAKING_ACTUATOR));
			
			if(actuatorsNumber > 2) {
				actuators.add(classToUse.getConstructor(String.class, VacuumWorldActuatorRole.class).newInstance(bodyId, VacuumWorldActuatorRole.DATABASE_ACTUATOR));
			}
			
			if(actuatorsNumber > 3) {
				addOtherActuators(actuatorsNumber, bodyId, actuators, classToUse);
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
		}

		return actuators;
	}

	private static void addOtherActuators(int actuatorsNumber, String bodyId, List<Actuator<VacuumWorldActuatorRole>> actuators, Class<? extends Actuator<VacuumWorldActuatorRole>> classToUse) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (int i = 0; i < actuatorsNumber - 3; i++) {
			actuators.add(classToUse.getConstructor(String.class, VacuumWorldActuatorRole.class).newInstance(bodyId, VacuumWorldActuatorRole.UNDEFINED));
		}
	}

	private static List<Sensor<VacuumWorldSensorRole>> createSensors(int sensorsNumber, String bodyId, Class<? extends Sensor<VacuumWorldSensorRole>> realClass) {
		if (sensorsNumber < 2) {
			return new ArrayList<>();
		}
		else {
			return createSensorsHelper(sensorsNumber, bodyId, realClass);
		}
	}

	private static List<Sensor<VacuumWorldSensorRole>> createSensorsHelper(int sensorsNumber, String bodyId, Class<? extends Sensor<VacuumWorldSensorRole>> realClass) {
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();
		
		try {
			sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.SEEING_SENSOR));
			sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.LISTENING_SENSOR));
			
			if(sensorsNumber > 2) {
				sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.DATABASE_SENSOR));
			}
			
			if(sensorsNumber > 3) {
				addOtherSensors(sensorsNumber, bodyId, sensors, realClass);
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
		}
		
		return sensors;
	}

	private static void addOtherSensors(int sensorsNumber, String bodyId, List<Sensor<VacuumWorldSensorRole>> sensors, Class<? extends Sensor<VacuumWorldSensorRole>> realClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (int i = 0; i < sensorsNumber - 3; i++) {
			sensors.add(realClass.getConstructor(String.class, VacuumWorldSensorRole.class).newInstance(bodyId, VacuumWorldSensorRole.UNDEFINED));
		}
	}

	private static JsonObject retrieveJsonObjectFromFile(FileInputStream input) {
		JsonReader reader = Json.createReader(input);
		JsonObject json = reader.readObject();
		reader.close();
		
		return json;
	}

	private static JsonObject retrieveJsonObjectFromController(ObjectInputStream input) throws IOException {
		try {
			ViewRequest viewRequest = (ViewRequest) input.readObject();
			return retrieveJsonObjectFromController(viewRequest, input);
		}
		catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	private static JsonObject retrieveJsonObjectFromController(ViewRequest viewRequest, ObjectInputStream input) {
		switch(viewRequest.getCode()) {
		case NEW:
			return VWUtils.parseJsonObjectFromString((String) viewRequest.getPayload());
		case LOAD_TEMPLATE_FROM_FILE: //for now this cannot happen.
			return retrieveJsonObjectFromTemplateFile(input);
		default:
			return null;
		}
	}

	private static JsonObject retrieveJsonObjectFromTemplateFile(ObjectInputStream input) {
		try {
			input.readObject();
			//this is incomplete.
		}
		catch(Exception e) {
			VWUtils.log(e);
		}
		
		return null; //change this upon implementation.
	}

	public static VacuumWorldMonitoringContainer createMonitoringContainer(JsonObject initialStateRepresentation) {
		VacuumWorldMonitoringContainer container = new VacuumWorldMonitoringContainer();
		List<VacuumWorldMonitoringAgent> monitoringAgents = new ArrayList<>();
		
		for(int i=0; i < ConfigData.getMonitoringAgentsNumber(); i++) {
			VacuumWorldMonitoringAgent agent = createMonitoringAgent(initialStateRepresentation);
			monitoringAgents.add(agent);
		}
		
		container.addMonitoringAgents(monitoringAgents);
		
		return container;
	}

	private static VacuumWorldMonitoringAgent createMonitoringAgent(JsonObject initialStateRepresentation) {
		String bodyId = "Monitor-" + UUID.randomUUID().toString();
		VacuumWorldMonitoringAgentMind mind = new VacuumWorldMonitoringAgentMind(bodyId, initialStateRepresentation);
		VacuumWorldMonitoringAgentBrain brain = new VacuumWorldMonitoringAgentBrain();
		List<Sensor<VacuumWorldSensorRole>> sensors = createSensors(3, bodyId, VacuumWorldMonitoringAgentSensor.class);
		List<Actuator<VacuumWorldActuatorRole>> actuators = createActuators(3, bodyId, VacuumWorldMonitoringAgentActuator.class);
		Double[] dimensions = new Double[] {(double) 1, (double) 1};
		VacuumWorldMonitoringAgentAppearance appearance = new VacuumWorldMonitoringAgentAppearance(bodyId, dimensions);
		
		VacuumWorldMonitoringAgent agent = new VacuumWorldMonitoringAgent(appearance, sensors, actuators, mind, brain);
		agent.setId(bodyId);
		
		return agent;
	}
}