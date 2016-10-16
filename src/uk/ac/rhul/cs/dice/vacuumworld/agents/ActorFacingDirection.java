package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.Random;

public enum ActorFacingDirection {
	NORTH, SOUTH, WEST, EAST;
	
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