package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;

public abstract class TurningAction extends PhysicalAction {
	private AgentFacingDirection agentOldFacingDirection;

	public AgentFacingDirection getAgentOldFacingDirection() {
		return this.agentOldFacingDirection;
	}

	public void setAgentOldFacingDirection(AgentFacingDirection agentOldFacingDirection) {
		this.agentOldFacingDirection = AgentFacingDirection.fromString(agentOldFacingDirection.toString().toLowerCase());
	}
}