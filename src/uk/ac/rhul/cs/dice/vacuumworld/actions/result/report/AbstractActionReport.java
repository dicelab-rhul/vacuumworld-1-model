package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Duplicable;

public abstract class AbstractActionReport implements ActionReportInterface<EnvironmentalAction, ActorFacingDirection, VacuumWorldCoordinates>, Duplicable<AbstractActionReport> {
	private Class<? extends EnvironmentalAction> action;
	private ActionResult actionResult;
	private ActorFacingDirection actorOldDirection;
	private ActorFacingDirection actorNewDirection;
	private VacuumWorldCoordinates actorOldCoordinates;
	private VacuumWorldCoordinates actorNewCoordinates;
	private Set<VacuumWorldCoordinates> perceptionKeys;
	
	public AbstractActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates, Set<VacuumWorldCoordinates> perceptionKeys) {
		this.action = action;
		this.actionResult = actionResult;
		this.actorOldDirection = actorOldDirection;
		this.actorNewDirection = actorNewDirection;
		this.actorOldCoordinates = actorOldCoordinates;
		this.actorNewCoordinates = actorNewCoordinates;
		this.perceptionKeys = perceptionKeys;
	}

	@Override
	public Class<? extends EnvironmentalAction> getAction() {
		return this.action;
	}

	@Override
	public void setAction(Class<? extends EnvironmentalAction> action) {
		this.action = action;
	}

	@Override
	public ActionResult getActionResult() {
		return this.actionResult;
	}

	@Override
	public void setActionResult(ActionResult actionResult) {
		this.actionResult = actionResult;
	}

	@Override
	public ActorFacingDirection getActorOldDirection() {
		return this.actorOldDirection;
	}

	@Override
	public void setActorOldDirection(ActorFacingDirection actorOldDirection) {
		this.actorOldDirection = actorOldDirection;
	}

	@Override
	public ActorFacingDirection getActorNewDirection() {
		return this.actorNewDirection;
	}

	@Override
	public void setActorNewDirection(ActorFacingDirection actorNewDirection) {
		this.actorNewDirection = actorNewDirection;
	}

	@Override
	public VacuumWorldCoordinates getActorOldCoordinates() {
		return this.actorOldCoordinates;
	}

	@Override
	public void setActorOldCoordinates(VacuumWorldCoordinates actorOldCoordinates) {
		this.actorOldCoordinates = actorOldCoordinates;
	}

	@Override
	public VacuumWorldCoordinates getActorNewCoordinates() {
		return this.actorNewCoordinates;
	}

	@Override
	public void setActorNewCoordinates(VacuumWorldCoordinates actorNewCoordinates) {
		this.actorNewCoordinates = actorNewCoordinates;
	}
	
	public Set<VacuumWorldCoordinates> getPerceptionKeys() {
		return this.perceptionKeys;
	}
	
	public void setPerceptionKeys(Set<VacuumWorldCoordinates> perceptionKeys) {
		this.perceptionKeys = perceptionKeys;
	}
}