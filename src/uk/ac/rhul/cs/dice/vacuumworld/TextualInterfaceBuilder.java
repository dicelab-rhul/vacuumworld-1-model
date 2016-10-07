package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class TextualInterfaceBuilder {
	private TextualInterfaceBuilder(){}
	
	public static List<String> getStringRepresentation(VacuumWorldSpace state) {
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

	private static String representLocation(Location location) {
		VacuumWorldLocation loc = (VacuumWorldLocation) location;

		if (loc.isAnAgentPresent() && loc.isDirtPresent()) {
			return getOverlappingSymbol(loc.getAgent(), loc.getDirt());
		}
		else if (loc.isAnAgentPresent()) {
			return loc.getAgent().getExternalAppearance().represent();
		}
		else if (loc.isDirtPresent()) {
			return loc.getDirt().getExternalAppearance().represent();
		}
		else {
			return "#";
		}
	}

	private static String getOverlappingSymbol(VacuumWorldCleaningAgent agent, Dirt dirt) {
		String agentString = agent.getExternalAppearance().represent().toLowerCase();
		String dirtString = dirt.getExternalAppearance().represent();

		return agentString.equals(dirtString) ? "@" : "!";
	}
}