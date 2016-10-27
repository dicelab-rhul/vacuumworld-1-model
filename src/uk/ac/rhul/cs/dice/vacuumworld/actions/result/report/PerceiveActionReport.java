package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class PerceiveActionReport extends AbstractActionReport {
	private Set<VacuumWorldCoordinates> perceptionKeys;
	
	public PerceiveActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates) {
		super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates);
	}
	
	public void setPerceptionKeys(Set<VacuumWorldCoordinates> perceptionKeys) {
		this.perceptionKeys = perceptionKeys;
	}
	
	public void setPerceptionKeys(Collection<VacuumWorldCoordinates> perceptionKeys) {
		this.perceptionKeys = new HashSet<>(perceptionKeys);
	}
	
	public Set<VacuumWorldCoordinates> getPerceptionKeys() {
		return this.perceptionKeys;
	}
}