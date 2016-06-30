package uk.ac.rhul.cs.dice.vacuumworld.agents;

public enum VacuumWorldAgentType {
	GREEN, ORANGE, NEUTRAL;
	
	public static VacuumWorldAgentType fromString(String type) {
		if(type == null) {
			return null;
		}
		
		switch(type) {
		case "green":
			return VacuumWorldAgentType.GREEN;
		case "orange":
			return VacuumWorldAgentType.ORANGE;
		default:
			return VacuumWorldAgentType.NEUTRAL;
		}
	}
	
	public String compactRepresentation() {
		switch(this) {
		case GREEN:
			return "G";
		case ORANGE:
			return "O";
		default:
			return "N";
		}
	}
}