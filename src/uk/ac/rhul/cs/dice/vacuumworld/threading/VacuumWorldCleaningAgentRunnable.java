package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;

public class VacuumWorldCleaningAgentRunnable extends AgentRunnable {

	public VacuumWorldCleaningAgentRunnable(VacuumWorldDefaultMind mind) {
		super(mind);
	}
	
	@Override
	public VacuumWorldDefaultMind getAgentMind() {
		return (VacuumWorldDefaultMind) super.getAgentMind();
	}
}