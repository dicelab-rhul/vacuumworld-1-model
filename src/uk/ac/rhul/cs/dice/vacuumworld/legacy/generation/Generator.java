package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class Generator {
	private ObjectMapper mapper;
	private int counter = 0;
	private Set<VacuumWorldCoordinates> allCoordinates;
	private Set<LocationStructure> allLocations;
	private Set<LocationStructure> dirtFreeLocations;
	private Set<LocationStructure> agentFreeLocations;

	private int dirts = 0;
	private int agents = 0;
	private int totalDirts = 0;
	private int totalAgents = 0;

	private static final Random RANDOM = new Random();
	private static final String AGENT = "agent";
	private static final String[] DIRECTIONS = new String[] { ActorFacingDirection.NORTH.toString().toLowerCase(), ActorFacingDirection.SOUTH.toString().toLowerCase(), ActorFacingDirection.EAST.toString().toLowerCase(), ActorFacingDirection.WEST.toString().toLowerCase() };
	private static final String[] COLORS = new String[] { VacuumWorldAgentType.ORANGE.toString().toLowerCase(), VacuumWorldAgentType.GREEN.toString().toLowerCase(), VacuumWorldAgentType.WHITE.toString().toLowerCase() };

	public void generate(File file, int width, int height, int numAgents, int numDirts) {
		if (validateInput(width, height, numAgents, numDirts)) {
			init(width, height, numAgents, numDirts, file);
		}
		else {
			Utils.logWithClass(this.getClass().getSimpleName(), "Failed to generate output.");
		}
	}

	private void init(int width, int height, int numAgents, int numDirts, File file) {
		try {
			initAttributes(numAgents, numDirts);
			tryAgentsOrDirts(width, height, numAgents, numDirts);
			Structure struct = new Structure(width, height, new ArrayList<LocationStructure>(this.allLocations));
			this.mapper.writeValue(file, struct);
		}
		catch (IOException e) {
			Utils.log(e);
		}
	}

	private void tryAgentsOrDirts(int width, int height, int numAgents, int numDirts) {
		while (this.agents + this.dirts < numAgents + numDirts) {
			if (RANDOM.nextInt(2) == 0) {
				tryDirt(width, height);
			}
			else {
				tryAgent(width, height);
			}
		}
	}

	private void initAttributes(int numAgents, int numDirts) {
		this.dirts = 0;
		this.agents = 0;
		this.totalDirts = numDirts;
		this.totalAgents = numAgents;
		this.mapper = new ObjectMapper();
		this.allCoordinates = new HashSet<>();
		this.allLocations = new HashSet<>();
		this.dirtFreeLocations = new HashSet<>();
		this.agentFreeLocations = new HashSet<>();
	}

	private void tryDirt(int width, int height) {
		if (this.dirts < this.totalDirts) {
			LocationStructure struct = tryDirtHelper(width, height);
			checkStruct(struct, true);
		}
		else {
			tryAgent(width, height);
		}
	}

	private void checkStruct(LocationStructure struct, boolean agentOrDirt) {
		if (struct != null) {
			this.allCoordinates.add(new VacuumWorldCoordinates(struct.getX(), struct.getY()));
			this.allLocations.add(struct);
			
			addStructToSet(struct, agentOrDirt);
			
		}
	}

	private void addStructToSet(LocationStructure struct, boolean agentOrDirt) {
		if(agentOrDirt) {
			this.agentFreeLocations.add(struct);
		}
		else {
			this.dirtFreeLocations.add(struct);
		}
	}

	private LocationStructure tryDirtHelper(int width, int height) {
		if (width * height > this.allLocations.size()) {
			return generateLocationWithDirt(width, height);
		}
		else {
			Utils.logWithClass(this.getClass().getSimpleName(), "All locations exist: " + this.allLocations.size());
			addDirtToLocation(generateRandomColor());
			this.dirts++;
			
			return null;
		}
	}

	private LocationStructure generateLocationWithDirt(int width, int height) {
		if (RANDOM.nextInt(5) < 4) {
			LocationStructure struct = generateLocationWithDirt(generateRandomCoordinate(width, height));
			this.dirts++;
			
			return struct;
		}
		else {
			return generateLocationWithDirtHelper(width, height);
		}
	}

	private LocationStructure generateLocationWithDirtHelper(int width, int height) {
		LocationStructure struct = null;
		
		if (!this.dirtFreeLocations.isEmpty()) {
			addDirtToLocation(generateRandomColor());
		}
		else {
			struct = generateLocationWithDirt(generateRandomCoordinate(width, height));
		}
		
		this.dirts++;
		
		return struct;
	}

	private void addDirtToLocation(String dirt) {
		if (!this.dirtFreeLocations.isEmpty()) {
			LocationStructure struct = this.dirtFreeLocations.iterator().next();
			struct.setDirt(dirt);
			this.dirtFreeLocations.remove(struct);
		}
		else {
			Utils.logWithClass(this.getClass().getSimpleName(), "No free locations to add dirt.");
		}
	}

	private LocationStructure generateLocationWithDirt(VacuumWorldCoordinates coord) {
		return new LocationStructure(coord.getX(), coord.getY(), null, COLORS[RANDOM.nextInt(COLORS.length)]);
	}

	private void tryAgent(int width, int height) {		
		if (this.agents < this.totalAgents) {
			LocationStructure struct = tryAgentHelper(width, height);
			checkStruct(struct, false);
		}
		else {
			tryDirt(width, height);
		}
	}

	private LocationStructure tryAgentHelper(int width, int height) {
		if (width * height > this.allLocations.size()) {
			return generateLocatonWithAgent(width, height);
		}
		else {
			addAgentToLocation(generateRandomAgent());
			this.agents++;
			
			return null;
		}
	}

	private LocationStructure generateLocatonWithAgent(int width, int height) {
		if (RANDOM.nextInt(5) < 4) {
			LocationStructure struct = generateLocationWithAgent(generateRandomCoordinate(width, height));
			this.agents++;
			
			return struct;
		}
		else {
			return generateLocationWithAgentHelper(width, height);
		}
	}

	private LocationStructure generateLocationWithAgentHelper(int width, int height) {
		LocationStructure struct = null;
		
		if (!this.agentFreeLocations.isEmpty()) {
			addAgentToLocation(generateRandomAgent());
		}
		else {
			struct = generateLocationWithAgent(generateRandomCoordinate(width, height));
		}
		
		this.agents++;
		
		return struct;
	}

	private void addAgentToLocation(AgentStructure agent) {
		if (!this.agentFreeLocations.isEmpty()) {
			LocationStructure struct = this.dirtFreeLocations.iterator().next();
			struct.setAgent(agent);
			this.agentFreeLocations.remove(struct);
		}
		else {
			Utils.logWithClass(this.getClass().getSimpleName(), "No free locations to add an agent.");
		}
	}

	private LocationStructure generateLocationWithAgent(VacuumWorldCoordinates coord) {
		return new LocationStructure(coord.getX(), coord.getY(), generateRandomAgent(), null);
	}

	private VacuumWorldCoordinates generateRandomCoordinate(int width, int height) {
		VacuumWorldCoordinates newCoord;
		
		while (this.allCoordinates.contains(newCoord = new VacuumWorldCoordinates(RANDOM.nextInt(width), RANDOM.nextInt(height)))) {
			continue;
		}
		
		this.allCoordinates.add(newCoord);
		return newCoord;
	}

	private AgentStructure generateRandomAgent() {
		return new AgentStructure(generateId(), generateName(), generateRandomColor(), generateRandomDirection(), 1, 1, 1, 1);
	}

	private String generateId() {
		return String.valueOf(RANDOM.nextLong());
	}

	private String generateName() {
		return AGENT + ++this.counter;
	}

	private String generateRandomColor() {
		return COLORS[RANDOM.nextInt(COLORS.length)];
	}

	private String generateRandomDirection() {
		return DIRECTIONS[RANDOM.nextInt(DIRECTIONS.length)];
	}

	private boolean validateInput(int width, int height, int numAgents, int numDirts) {
		int totalSpace = width * height;
		float perAgent = (float) numAgents / totalSpace;
		float perDirt = (float) numDirts / totalSpace;

		return validateAgents(perAgent, width, height, numAgents) && validateDirts(perDirt, width, height, numDirts);
	}

	private boolean validateDirts(float perDirt, int width, int height, int numDirts) {
		if (perDirt > 0.8) {
			densityWarning(perDirt, "DIRT");
			
			if (perDirt > 1) {
				densityFailure("DIRT", width, height, numDirts);
				return false;
			}
		}
		
		return true;
	}

	private boolean validateAgents(float perAgent, int width, int height, int numAgents) {
		if (perAgent > 0.8) {
			densityWarning(perAgent, "AGENT");
			
			if (perAgent > 1) {
				densityFailure("AGENT", width, height, numAgents);
				return false;
			}
		}
		
		return true;
	}

	private void densityWarning(float per, String type) {
		Utils.logWithClass(this.getClass().getSimpleName(), "MAP " + type + " POPULATION DENSE AT: " + per * 100 + "% : CONSIDER ENLARGING MAP");
	}

	private void densityFailure(String type, int width, int height, int num) {
		Utils.logWithClass(this.getClass().getSimpleName(), "CANNOT ADD " + num + " " + type + " TO A GRID SIZE OF (" + width + "," + height + ")");
	}
}