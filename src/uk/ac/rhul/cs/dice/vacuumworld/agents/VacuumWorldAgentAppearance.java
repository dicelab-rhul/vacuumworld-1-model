package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;

public class VacuumWorldAgentAppearance extends AbstractAgentAppearance {
	private VacuumWorldAgentType type;
	
	public VacuumWorldAgentAppearance(String name, Double[] dimensions, VacuumWorldAgentType type) {
		super(name, dimensions);
		this.type = type;
	}

	public VacuumWorldAgentType getType() {
		return this.type;
	}

	@Override
	public String represent() {
		return this.type.compactRepresentation();
	}
}