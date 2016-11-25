package uk.ac.rhul.cs.dice.vacuumworld.utils;

import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;

public enum TurningDirection {
    LEFT, RIGHT;

    public static TurningDirection fromString(String candidate) {
	switch (candidate.toLowerCase()) {
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
	switch (this) {
	case LEFT:
	    return "LEFT";
	case RIGHT:
	    return "RIGHT";
	default:
	    throw new IllegalArgumentException();
	}
    }

    public static TurningDirection fromFacingDirections(Pair<ActorFacingDirection> pair) {
	if (pair.getSecond().equals(pair.getFirst().getLeftDirection())) {
	    return TurningDirection.LEFT;
	}
	else if (pair.getSecond().equals(pair.getFirst().getRightDirection())) {
	    return TurningDirection.RIGHT;
	}
	else {
	    throw new IllegalArgumentException();
	}
    }
}