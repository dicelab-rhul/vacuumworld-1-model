package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;

public enum ActorFacingDirection {
	NORTH, SOUTH, WEST, EAST;
	
	private static final Map<Pair<ActorFacingDirection>, List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>>> bestStrategies = getBestStrategy();
	
	public static ActorFacingDirection fromString(String value) {
		switch(value) {
		case "north":
			return NORTH;
		case "south":
			return SOUTH;
		case "west":
			return WEST;
		case "east":
			return EAST;
		default:
			throw new IllegalArgumentException("Bad facing position representation: " + value);
		}
	}
	
	private static Map<Pair<ActorFacingDirection>, List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>>> getBestStrategy() {
		Map<Pair<ActorFacingDirection>, List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>>> strategies = new HashMap<>();
		
		strategies.put(new Pair<>(ActorFacingDirection.NORTH, ActorFacingDirection.NORTH), Arrays.asList(MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.NORTH, ActorFacingDirection.SOUTH), Arrays.asList(TurnRightAction.class, TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.NORTH, ActorFacingDirection.WEST), Arrays.asList(TurnLeftAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.NORTH, ActorFacingDirection.EAST), Arrays.asList(TurnRightAction.class, MoveAction.class));
		
		strategies.put(new Pair<>(ActorFacingDirection.SOUTH, ActorFacingDirection.NORTH), Arrays.asList(TurnRightAction.class, TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.SOUTH, ActorFacingDirection.SOUTH), Arrays.asList(MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.SOUTH, ActorFacingDirection.WEST), Arrays.asList(TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.SOUTH, ActorFacingDirection.EAST), Arrays.asList(TurnLeftAction.class, MoveAction.class));
		
		strategies.put(new Pair<>(ActorFacingDirection.WEST, ActorFacingDirection.NORTH), Arrays.asList(TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.WEST, ActorFacingDirection.SOUTH), Arrays.asList(TurnLeftAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.WEST, ActorFacingDirection.WEST), Arrays.asList(MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.WEST, ActorFacingDirection.EAST), Arrays.asList(TurnRightAction.class, TurnRightAction.class, MoveAction.class));
		
		strategies.put(new Pair<>(ActorFacingDirection.EAST, ActorFacingDirection.NORTH), Arrays.asList(TurnLeftAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.EAST, ActorFacingDirection.SOUTH), Arrays.asList(TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.EAST, ActorFacingDirection.WEST), Arrays.asList(TurnRightAction.class, TurnRightAction.class, MoveAction.class));
		strategies.put(new Pair<>(ActorFacingDirection.EAST, ActorFacingDirection.EAST), Arrays.asList(MoveAction.class));
		
		return strategies;
	}

	public static ActorFacingDirection fromCompactRepresentation(String value) {
		switch(value) {
		case "N":
			return NORTH;
		case "S":
			return SOUTH;
		case "W":
			return WEST;
		case "E":
			return EAST;
		default:
			throw new IllegalArgumentException("Bad facing position representation: " + value);
		}
	}
	
	public List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> getBestStrategyForMoving(ActorFacingDirection targetposition) {
		return ActorFacingDirection.bestStrategies.get(new Pair<ActorFacingDirection>(ActorFacingDirection.this, targetposition));
	}
	
	public ActorFacingDirection getLeftDirection() {
		switch(this) {
		case NORTH:
			return WEST;
		case SOUTH:
			return EAST;
		case WEST:
			return SOUTH;
		case EAST:
			return NORTH;
		default:
			throw new IllegalArgumentException("Bad facing position: " + this);
		}
	}
	
	public ActorFacingDirection getRightDirection() {
		switch(this) {
		case NORTH:
			return EAST;
		case SOUTH:
			return WEST;
		case WEST:
			return NORTH;
		case EAST:
			return SOUTH;
		default:
			throw new IllegalArgumentException("Bad facing position: " + this);
		}
	}
	
	public boolean isOpposite(ActorFacingDirection candidate) {
		switch(this) {
		case NORTH:
			return candidate.equals(SOUTH);
		case SOUTH:
			return candidate.equals(NORTH);
		case WEST:
			return candidate.equals(EAST);
		case EAST:
			return candidate.equals(WEST);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public String compactRepresentation() {
		switch(this) {
		case NORTH:
			return "N";
		case SOUTH:
			return "S";
		case WEST:
			return "W";
		case EAST:
			return "E";
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static ActorFacingDirection random() {
		Random random = new Random();
		
		if(random.nextBoolean()) {
			return northOrSouth(random);
		}
		else {
			return westOrEast(random);
		}
	}

	private static ActorFacingDirection westOrEast(Random random) {
		if(random.nextBoolean()) {
			return ActorFacingDirection.WEST;
		}
		else {
			return ActorFacingDirection.EAST;
		}
	}

	private static ActorFacingDirection northOrSouth(Random random) {
		if(random.nextBoolean()) {
			return ActorFacingDirection.NORTH;
		}
		else {
			return ActorFacingDirection.SOUTH;
		}
	}
}