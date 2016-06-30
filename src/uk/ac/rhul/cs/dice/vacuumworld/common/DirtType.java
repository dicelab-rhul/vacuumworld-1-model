package uk.ac.rhul.cs.dice.vacuumworld.common;

public enum DirtType {
	GREEN, ORANGE, NEUTRAL;
	
	public static DirtType fromString(String type) {
		if(type == null) {
			return null;
		}
		
		switch(type) {
		case "green":
			return DirtType.GREEN;
		case "orange":
			return DirtType.ORANGE;
		default:
			return DirtType.NEUTRAL;
		}
	}
	
	public String compactRepresentation() {
		switch(this) {
		case GREEN:
			return "g";
		case ORANGE:
			return "o";
		default:
			return "n";
		}
	}
}