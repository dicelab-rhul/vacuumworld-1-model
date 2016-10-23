package uk.ac.rhul.cs.dice.vacuumworld.utils;

public enum TurningDirection {
	LEFT, RIGHT;
	
	public static TurningDirection fromString(String candidate) {
		switch(candidate.toLowerCase()) {
		case "left":
			return TurningDirection.LEFT;
		case "right":
			return TurningDirection.RIGHT;
		default:
			throw new IllegalArgumentException("Bad turing direction: " + candidate + ".");
		}
	}
	
	@Override
	public String toString() {
		switch(this) {
		case LEFT:
			return "LEFT";
		case RIGHT:
			return "RIGHT";
		default:
			throw new IllegalArgumentException();
		}
	}
}