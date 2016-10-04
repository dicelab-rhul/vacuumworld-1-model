package uk.ac.rhul.cs.dice.vacuumworld.common;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public enum DirtType {
	GREEN, ORANGE, NEUTRAL;

	public static DirtType fromString(String type) {
		switch (type) {
		case "green":
			return DirtType.GREEN;
		case "orange":
			return DirtType.ORANGE;
		case "neutral":
			return DirtType.NEUTRAL;
		default:
			throw new IllegalArgumentException("Bad dirt type: " + type);
		}
	}

	public static boolean agentAndDirtCompatible(DirtType dirtType, VacuumWorldAgentType agentType) {
		if(dirtType.toString().equals(agentType.toString())) {
			return true;
		}
		
		if (agentType.equals(VacuumWorldAgentType.WHITE)) {
			return true;
		}
		
		if (dirtType.equals(DirtType.NEUTRAL)) {
			return true;
		}
		
		return false;
	}

	public String compactRepresentation() {
		switch (this) {
		case GREEN:
			return "g";
		case ORANGE:
			return "o";
		default:
			return "n";
		}
	}
}