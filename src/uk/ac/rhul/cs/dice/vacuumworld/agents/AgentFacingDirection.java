package uk.ac.rhul.cs.dice.vacuumworld.agents;

public enum AgentFacingDirection {
	NORTH, SOUTH, WEST, EAST;
	
	public static AgentFacingDirection fromString(String value) {
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
	
	public AgentFacingDirection getLeftDirection() {
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
	
	public AgentFacingDirection getRightDirection() {
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
}