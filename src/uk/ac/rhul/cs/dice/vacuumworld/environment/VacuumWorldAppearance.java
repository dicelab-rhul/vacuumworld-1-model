package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.UniverseAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.TextualInterfaceBuilder;

public class VacuumWorldAppearance extends UniverseAppearance {
	private List<String> stringRepresentation;
	
	public VacuumWorldAppearance(String name, Map<SpaceCoordinates, Double[]> bounds, VacuumWorldSpace state) {
		super(name, bounds);
		updateRepresentation(state);
	}

	public void updateRepresentation(VacuumWorldSpace state) {
		this.stringRepresentation = TextualInterfaceBuilder.getStringRepresentation(state);
	}
	
	@Override
	public String represent() {
		StringBuilder representation = new StringBuilder();
		
		for(String s : this.stringRepresentation) {
			representation.append(s + "\n");
		}
		
		return representation.toString() + "\n";
	}
}