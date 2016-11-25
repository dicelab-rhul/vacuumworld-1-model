package uk.ac.rhul.cs.dice.vacuumworld.agents;

public enum VacuumWorldAgentType {
    GREEN, ORANGE, WHITE;

    public static VacuumWorldAgentType fromString(String type) {
	switch (type) {
	case "green":
	    return VacuumWorldAgentType.GREEN;
	case "orange":
	    return VacuumWorldAgentType.ORANGE;
	case "white":
	    return VacuumWorldAgentType.WHITE;
	default:
	    throw new IllegalArgumentException("Bad agent color: " + type + ".");
	}
    }

    public String compactRepresentation() {
	switch (this) {
	case GREEN:
	    return "G";
	case ORANGE:
	    return "O";
	case WHITE:
	    return "W";
	default:
	    throw new IllegalArgumentException();
	}
    }
}