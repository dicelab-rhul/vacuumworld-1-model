package uk.ac.rhul.cs.dice.vacuumworld.utils.parser;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.PhysicalBody;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class TextualInterfaceBuilder {
    private TextualInterfaceBuilder() {}

    public static List<String> getStringRepresentation(VacuumWorldSpace state) {
	if (state == null) {
	    return new ArrayList<>();
	}
	else {
	    return getStringRepresentationHelper(state);
	}
    }

    private static List<String> getStringRepresentationHelper(VacuumWorldSpace state) {
	List<String> representation = new ArrayList<>();

	int width = state.getDimensions()[0];
	int height = state.getDimensions()[1];

	for (int i = 0; i < height; i++) {
	    StringBuilder builder = new StringBuilder();

	    for (int j = 0; j < width; j++) {
		builder.append(representLocation(state.getLocation(new VacuumWorldCoordinates(j, i))));
	    }

	    representation.add(builder.toString());
	}

	return representation;
    }

    private static String representLocation(VacuumWorldLocation location) {
	if (isOverlapping(location)) {
	    return getOverlappingSymbol(location);
	}
	else if (location.isAnAgentPresent()) {
	    return location.getAgent().getExternalAppearance().represent();
	}
	else if (location.isAUserPresent()) {
	    return location.getUser().getExternalAppearance().represent();
	}
	else if (location.isDirtPresent()) {
	    return location.getDirt().getExternalAppearance().represent();
	}
	else {
	    return "#";
	}
    }

    private static String getOverlappingSymbol(VacuumWorldLocation location) {
	if (location.isAnAgentPresent()) {
	    return getOverlappingSymbol(location.getAgent(), location.getDirt());
	}
	else if (location.isAUserPresent()) {
	    return getOverlappingSymbol(location.getUser(), location.getDirt());
	}
	else {
	    return null;
	}
    }

    private static boolean isOverlapping(VacuumWorldLocation location) {
	return location.isDirtPresent() && (location.isAnAgentPresent() || location.isAUserPresent());
    }

    private static String getOverlappingSymbol(PhysicalBody actor, Dirt dirt) {
	String actorString = actor.getExternalAppearance().represent().toLowerCase();
	String dirtString = dirt.getExternalAppearance().represent();

	return actorString.equals(dirtString) ? "@" : "!";
    }
}