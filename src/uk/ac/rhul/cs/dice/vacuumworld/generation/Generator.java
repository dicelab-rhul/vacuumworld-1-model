package uk.ac.rhul.cs.dice.vacuumworld.generation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Generator {

  private ObjectMapper mapper;
  private int counter = 0;
  private HashSet<Coordinate> allCoordinates;
  private HashSet<LocationStructure> allLocations;
  private HashSet<LocationStructure> dirtFreeLocations;
  private HashSet<LocationStructure> agentFreeLocations;

  private int dirts = 0, agents = 0, totalDirts = 0, totalAgents = 0;

  private final Random RANDOM = new Random();
  private final String AGENT = "agent";
  private final String[] DIRECTIONS = new String[] { "north", "south", "east",
      "west" };
  private final String[] COLORS = new String[] { "orange", "green", "white" };

  public Generator() {
  }

  public void generate(File file, int width, int height, int numAgents,
      int numDirts) {
    if (validateInput(width, height, numAgents, numDirts)) {
      try {
        dirts = 0;
        agents = 0;
        totalDirts = numDirts;
        totalAgents = numAgents;
        mapper = new ObjectMapper();
        allCoordinates = new HashSet<>();
        allLocations = new HashSet<>();
        dirtFreeLocations = new HashSet<>();
        agentFreeLocations = new HashSet<>();

        while (agents + dirts < numAgents + numDirts) {
          if (RANDOM.nextInt(2) == 0) {
            tryDirt(width, height);
          } else {
            tryAgent(width, height);
          }
        }
        Structure struct = new Structure(width, height,
            new ArrayList<LocationStructure>(allLocations));
        mapper.writeValue(file, struct);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Logger.getGlobal().log(Level.SEVERE, "FAILED TO GENERATE OUTPUT");
    }
  }

  private void tryDirt(int width, int height) {
    LocationStructure struct = null;
    //System.out.println("TRYING DIRT {");
    if (dirts < totalDirts) {
      if (width * height > allLocations.size()) {
        if (RANDOM.nextInt(5) < 4) {
          struct = generateLocationWithDirt(generateRandomCoordinate(width,
              height));
          dirts++;
        } else {
          if (!dirtFreeLocations.isEmpty()) {
            addDirtToLocation(generateRandomColor());
          } else {
            struct = generateLocationWithDirt(generateRandomCoordinate(width,
                height));
          }
          dirts++;
        }
      } else {
        System.out.println("ALL LOCATIONS EXIST " + allLocations.size());
        addDirtToLocation(generateRandomColor());
        dirts++;
      }
      if (struct != null) {
        allCoordinates.add(new Coordinate(struct.getX(), struct.getY()));
        allLocations.add(struct);
        agentFreeLocations.add(struct);
      }
    } else {
      tryAgent(width, height);
    }
    //System.out.println("}");
  }

  private void addDirtToLocation(String dirt) {
    if (!dirtFreeLocations.isEmpty()) {
      LocationStructure struct = dirtFreeLocations.iterator().next();

      //System.out.println("adding dirt to location: " + struct.getX() + ","
          //+ struct.getY());

      struct.setDirt(dirt);
      dirtFreeLocations.remove(struct);
    } else {
      Logger.getGlobal().log(Level.SEVERE, "NO FREE LOCATIONS TO ADD DIRT");
    }
  }

  private LocationStructure generateLocationWithDirt(Coordinate coord) {
    //System.out.println("creating dirt at location: " + coord);
    return new LocationStructure(coord.getX(), coord.getY(), null,
        COLORS[RANDOM.nextInt(COLORS.length)]);
  }

  private void tryAgent(int width, int height) {
    //System.out.println("TRYING AGENT {");

    LocationStructure struct = null;
    if (agents < totalAgents) {
      if (width * height > allLocations.size()) {
        if (RANDOM.nextInt(5) < 4) {
          struct = generateLocationWithAgent(generateRandomCoordinate(width,
              height));
          agents++;
        } else {
          if (!agentFreeLocations.isEmpty()) {
            addAgentToLocation(generateRandomAgent());
          } else {
            struct = generateLocationWithAgent(generateRandomCoordinate(width,
                height));
          }
          agents++;
        }
      } else {
        //System.out.println("ALL LOCATIONS EXIST " + allLocations.size());
        addAgentToLocation(generateRandomAgent());
        agents++;
      }
      if (struct != null) {
        allCoordinates.add(new Coordinate(struct.getX(), struct.getY()));
        allLocations.add(struct);
        dirtFreeLocations.add(struct);
      }
    } else {
      tryDirt(width, height);
    }
    //System.out.println("}");
  }

  private void addAgentToLocation(AgentStructure agent) {
    if (!agentFreeLocations.isEmpty()) {
      LocationStructure struct = agentFreeLocations.iterator().next();

      //System.out.println("adding agent to location: " + struct.getX() + ","
          //+ struct.getY());

      struct.setAgent(agent);
      agentFreeLocations.remove(struct);
    } else {
      Logger.getGlobal().log(Level.SEVERE, "NO FREE LOCATIONS TO ADD AGENT");
    }
  }

  private LocationStructure generateLocationWithAgent(Coordinate coord) {
    //System.out.println("creating agent at location: " + coord);
    return new LocationStructure(coord.getX(), coord.getY(),
        generateRandomAgent(), null);
  }

  private Coordinate generateRandomCoordinate(int width, int height) {
    Coordinate newCoord;
    while (allCoordinates.contains((newCoord = new Coordinate(RANDOM
        .nextInt(width), RANDOM.nextInt(height))))) {
    }
    allCoordinates.add(newCoord);
    return newCoord;
  }

  private AgentStructure generateRandomAgent() {
    return new AgentStructure(generateId(), generateName(),
        generateRandomColor(), 1, 1, 1, 1, generateRandomDirection());
  }

  private String generateId() {
    return String.valueOf(RANDOM.nextLong());
  }

  private String generateName() {
    counter++;
    return AGENT + counter;
  }

  private String generateRandomColor() {
    return COLORS[RANDOM.nextInt(COLORS.length)];
  }

  private String generateRandomDirection() {
    return DIRECTIONS[RANDOM.nextInt(DIRECTIONS.length)];
  }

  private boolean validateInput(int width, int height, int numAgents,
      int numDirts) {
    int totalSpace = width * height;
    float perAgent = (float) numAgents / totalSpace;
    float perDirt = (float) numDirts / totalSpace;

    if (perAgent > 0.8) {
      densityWarning(perAgent, "AGENT");
      if (perAgent > 1) {
        densityFailure("AGENT", width, height, numAgents);
        return false;
      }
    }

    if (perDirt > 0.8) {
      densityWarning(perDirt, "DIRT");
      if (perDirt > 1) {
        densityFailure("DIRT", width, height, numDirts);
        return false;
      }
    }
    return true;
  }

  private void densityWarning(float per, String type) {
    Logger.getGlobal().warning(
        "MAP " + type + " POPULATION DENSE AT: " + per * 100
            + "% : CONSIDER ENLARGING MAP");
  }

  private void densityFailure(String type, int width, int height, int num) {
    Logger.getGlobal().severe(
        "CANNOT ADD " + num + " " + type + " TO A GRID SIZE OF (" + width + ","
            + height + ")");
  }

}
