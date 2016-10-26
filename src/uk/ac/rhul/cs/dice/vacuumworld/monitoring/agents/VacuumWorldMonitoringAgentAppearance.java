package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;

public class VacuumWorldMonitoringAgentAppearance extends AbstractAgentAppearance {

	public VacuumWorldMonitoringAgentAppearance(String name, Double[] dimensions) {
		super(name, dimensions);
	}

	@Override
	public String represent() {
		return "M";
	}

	@Override
	public VacuumWorldMonitoringAgentAppearance duplicate() {
		return new VacuumWorldMonitoringAgentAppearance(getName(), getDimensions());
	}
}