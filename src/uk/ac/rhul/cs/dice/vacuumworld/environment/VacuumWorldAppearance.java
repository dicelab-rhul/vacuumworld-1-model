package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.UniverseAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldParser;

public class VacuumWorldAppearance extends UniverseAppearance {
	private List<String> stringRepresentation;
	
	public VacuumWorldAppearance(String name, Map<SpaceCoordinates, Double[]> bounds, VacuumWorldSpace state) {
		super(name, bounds);
		updateRepresentation(state);
	}

	public void updateRepresentation(VacuumWorldSpace state) {
		this.stringRepresentation = VacuumWorldParser.getStringRepresentation(state);
	}
	
	@Override
	public String represent() {
		String representation = "";
		
		for(String s : this.stringRepresentation) {
			representation += s + "\n";
		}
		
		return representation + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stringRepresentation == null) ? 0 : stringRepresentation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VacuumWorldAppearance other = (VacuumWorldAppearance) obj;
		if (stringRepresentation == null) {
			if (other.stringRepresentation != null)
				return false;
		} else if (!stringRepresentation.equals(other.stringRepresentation))
			return false;
		return true;
	}
}