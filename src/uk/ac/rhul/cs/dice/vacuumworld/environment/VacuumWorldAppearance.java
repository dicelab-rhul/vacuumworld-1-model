package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.Appearance;
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
	
	private void setStringRepresentation(List<String> stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}
	
	@Override
	public String represent() {
		StringBuilder representation = new StringBuilder();
		
		for(String s : this.stringRepresentation) {
			representation.append(s + "\n");
		}
		
		return representation.toString() + "\n";
	}

	@Override
	public VacuumWorldAppearance duplicate() {
		String name = getName();
		List<Appearance> subEnvironmentAppearances = new ArrayList<>();
		
		for(Appearance appearance : getSubAppearances()) {
			subEnvironmentAppearances.add(appearance.duplicate());
		}
		
		Map<SpaceCoordinates, Double[]> bounds = new EnumMap<>(SpaceCoordinates.class);
		
		for(Entry<SpaceCoordinates, Double[]> entry : getBounds().entrySet()) {
			bounds.put(entry.getKey(), entry.getValue());
		}
		
		VacuumWorldAppearance toReturn = new VacuumWorldAppearance(name, bounds, null);
		
		List<String> stringRepresentationCopy = new ArrayList<>();
		
		for(String string : this.stringRepresentation) {
			stringRepresentationCopy.add(string);
		}
		
		toReturn.setStringRepresentation(stringRepresentationCopy);
		
		return toReturn;
	}
}