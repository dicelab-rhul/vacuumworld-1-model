package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.Appearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.SimpleEnvironmentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.UniverseAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.TextualInterfaceBuilder;

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

	for (String s : this.stringRepresentation) {
	    representation.append(s + "\n");
	}

	return representation.toString() + "\n";
    }

    @Override
    public VacuumWorldAppearance duplicate() {
	String name = getName();
	List<SimpleEnvironmentAppearance> subEnvironmentAppearances = getSubAppearances().stream().map(Appearance::duplicate).map(appearance -> (SimpleEnvironmentAppearance) appearance).collect(Collectors.toList());

	Map<SpaceCoordinates, Double[]> bounds = new EnumMap<>(SpaceCoordinates.class);
	getBounds().entrySet().forEach(entry -> bounds.put(entry.getKey(), entry.getValue()));

	VacuumWorldAppearance toReturn = new VacuumWorldAppearance(name, bounds, null);
	subEnvironmentAppearances.stream().forEach(toReturn::addSubEnvironmentAppearance);

	List<String> stringRepresentationCopy = this.stringRepresentation.stream().collect(Collectors.toList());
	toReturn.setStringRepresentation(stringRepresentationCopy);

	return toReturn;
    }
}