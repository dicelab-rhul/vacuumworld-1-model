package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

public class VacuumWorldParser {

  private static Class<VacuumWorldSmartRandomSocialMind> smartRandomSocialMind = VacuumWorldSmartRandomSocialMind.class;
  private static Class<VacuumWorldRandomMind> randomMind = VacuumWorldRandomMind.class;

  private static Class<? extends VacuumWorldDefaultMind> mindClassToUse = smartRandomSocialMind;

  private VacuumWorldParser() {
  }

  public static VacuumWorldMonitoringContainer parseInitialState(String filename)
      throws FileNotFoundException {
    FileInputStream input = new FileInputStream(new File(filename));
    return parseInitialState(input);
  }

  public static VacuumWorldMonitoringContainer parseInitialState(
      InputStream input) {
    JsonReader reader = Json.createReader(input);
    JsonObject json = reader.readObject();
    reader.close();

    VacuumWorldSpace space = createInitialState(json);
    VacuumWorldMonitoringPhysics monitoringPhysics = new VacuumWorldMonitoringPhysics();

    return new VacuumWorldMonitoringContainer(monitoringPhysics, space);
  }

  private static VacuumWorldSpace createInitialState(JsonObject json) {
    int width = json.getInt("width");
    int height = json.getInt("height");

    List<VacuumWorldLocation> notableLocations = getNotableLocations(json,
        width, height);

    return createInitialState(notableLocations, width, height);
  }

  private static VacuumWorldSpace createInitialState(
      List<VacuumWorldLocation> notableLocations, int width, int height) {
    Map<LocationKey, Location> spaceMap = new HashMap<>();

    for (VacuumWorldLocation location : notableLocations) {
      spaceMap.put(location.getCoordinates(), location);
    }

    return fillState(spaceMap, width, height);
  }

  private static VacuumWorldSpace fillState(
      Map<LocationKey, Location> spaceMap, int width, int height) {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        putNewLocationIfNecessary(spaceMap, i, j, width, height);
      }
    }
    return packState(spaceMap, width, height);
  }

  private static void putNewLocationIfNecessary(
      Map<LocationKey, Location> spaceMap, int i, int j, int width, int height) {
    VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);

    if (!spaceMap.containsKey(coordinates)) {
      VacuumWorldLocationType type = VacuumWorldLocationType.NORMAL;
      VacuumWorldLocation location = new VacuumWorldLocation(coordinates, type,
          width - 1, height - 1);
      spaceMap.put(coordinates, location);
    }
  }

  private static VacuumWorldSpace packState(
      Map<LocationKey, Location> spaceMap, int width, int height) {
    int[] dimensions = new int[] { width, height };

    return new VacuumWorldSpace(dimensions, spaceMap);
  }

  private static List<VacuumWorldLocation> getNotableLocations(JsonObject json,
      int width, int height) {
    List<VacuumWorldLocation> locations = new ArrayList<>();
    JsonArray notableLocations = json.getJsonArray("notable_locations");

    for (JsonValue value : notableLocations) {
      if (value instanceof JsonObject) {
        locations.add(parseLocation((JsonObject) value, width, height));
      }
    }

    return locations;
  }

  private static VacuumWorldLocation parseLocation(JsonObject value, int width,
      int height) {
    int x = value.getInt("x");
    int y = value.getInt("y");
    VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(x, y);

    JsonObject agentObject = value.isNull("agent") ? null : value
        .getJsonObject("agent");
    VacuumWorldCleaningAgent agent = parseAgent(agentObject);

    JsonString dirtString = value.isNull("dirt") ? null : value
        .getJsonString("dirt");
    Dirt dirt = parseDirt(dirtString);

    return createLocation(coordinates, agent, dirt, width, height);
  }

  private static VacuumWorldLocation createLocation(
      VacuumWorldCoordinates coordinates, VacuumWorldCleaningAgent agent,
      Dirt dirt, int width, int height) {
    VacuumWorldLocation location = new VacuumWorldLocation(coordinates,
        VacuumWorldLocationType.NORMAL, width - 1, height - 1);

    if (agent != null) {
      agent.setCurrentLocation(coordinates);
    }

    location.addAgent(agent);
    location.setDirt(dirt);

    return location;
  }

  private static Dirt parseDirt(JsonString dirtString) {
    if (dirtString == null) {
      return null;
    } else {
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

  private static VacuumWorldCleaningAgent parseAgent(JsonObject agentObject) {
    if (agentObject == null) {
      return null;
    } else {
      return parseNotNullAgent(agentObject);
    }
  }

  private static VacuumWorldCleaningAgent parseNotNullAgent(
      JsonObject agentObject) {
    String id = agentObject.getString("id");
    String name = agentObject.getString("name");
    String color = agentObject.getString("color");

    int sensorsNumber = agentObject.getInt("sensors");
    int actuatorsNumber = agentObject.getInt("actuators");
    int width = agentObject.getInt("width");
    int height = agentObject.getInt("height");

    AgentFacingDirection agentFacingDirection = AgentFacingDirection
        .fromString(agentObject.getString("facing_direction"));
    Double[] dimensions = new Double[] { (double) width, (double) height };

    return createAgent(id, name, color, sensorsNumber, actuatorsNumber,
        dimensions, agentFacingDirection);
  }

  private static VacuumWorldCleaningAgent createAgent(String id, String name,
      String color, int sensorsNumber, int actuatorsNumber,
      Double[] dimensions, AgentFacingDirection agentFacingDirection) {
    List<Sensor> sensors = createSensors(sensorsNumber);
    List<Actuator> actuators = createActuators(actuatorsNumber);

    VacuumWorldAgentType type = VacuumWorldAgentType.fromString(color);
    AbstractAgentAppearance appearance = new VacuumWorldAgentAppearance(name,
        dimensions, type);

    VacuumWorldDefaultMind mind = null;
    try {
      mind = mindClassToUse.getConstructor(AgentAwarenessRepresentation.class)
          .newInstance(createAwareness(actuators, sensors, id, type));
    } catch (Exception e) {
      e.printStackTrace();
    }

    VacuumWorldDefaultBrain brain = new VacuumWorldDefaultBrain(mind.getClass());

    VacuumWorldCleaningAgent agent = new VacuumWorldCleaningAgent(appearance,
        sensors, actuators, mind, brain, agentFacingDirection);
    agent.setId(id);

    return agent;
  }

  private static AgentAwarenessRepresentation createAwareness(
      List<Actuator> as, List<Sensor> ss, String id, VacuumWorldAgentType type) {
    ArrayList<String> aids = new ArrayList<>();
    as.forEach(new Consumer<Actuator>() {
      @Override
      public void accept(Actuator t) {
        aids.add(((VacuumWorldDefaultActuator) t).getActuatorId());
      }
    });
    ArrayList<String> earids = new ArrayList<>();
    ss.forEach(new Consumer<Sensor>() {
      @Override
      public void accept(Sensor t) {
        earids.add(((VacuumWorldDefaultSensor) t).getSensorId());
      }
    });
    ArrayList<String> eyeids = new ArrayList<>();
    ss.forEach(new Consumer<Sensor>() {
      @Override
      public void accept(Sensor t) {
        eyeids.add(((VacuumWorldDefaultSensor) t).getSensorId());
      }
    });
    return new AgentAwarenessRepresentation(id, aids, earids, eyeids, type);
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

  public static List<String> getStringRepresentation(VacuumWorldSpace state) {
    List<String> representation = new ArrayList<>();

    int width = state.getDimensions()[0];
    int height = state.getDimensions()[1];

    for (int j = 0; j < height; j++) {
      String newLine = "";

      for (int i = 0; i < width; i++) {
        newLine += representLocation(state
            .getLocation(new VacuumWorldCoordinates(i, j)));
      }

      representation.add(newLine);
    }

    return representation;
  }

  private static String representLocation(Location location) {
    VacuumWorldLocation loc = (VacuumWorldLocation) location;

    if (loc.isAnAgentPresent() && loc.isDirtPresent()) {
      return getOverlappingSymbol(loc.getAgent(), loc.getDirt());
    } else if (loc.isAnAgentPresent()) {
      return loc.getAgent().getExternalAppearance().represent();
    } else if (loc.isDirtPresent()) {
      return loc.getDirt().getExternalAppearance().represent();
    } else {
      return "#";
    }
  }

  private static String getOverlappingSymbol(VacuumWorldCleaningAgent agent,
      Dirt dirt) {
    String agentString = agent.getExternalAppearance().represent()
        .toLowerCase();
    String dirtString = dirt.getExternalAppearance().represent();

    return agentString.equals(dirtString) ? "@" : "!";
  }
}