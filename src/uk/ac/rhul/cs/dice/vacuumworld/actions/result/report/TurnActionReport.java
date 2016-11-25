package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashSet;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.TurningDirection;

public class TurnActionReport extends AbstractActionReport {
    private TurningDirection turningDirection;

    public TurnActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates, TurningDirection turningDirection) {
	super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates, null);

	this.turningDirection = turningDirection;
    }

    public TurningDirection getTurningDirection() {
	return this.turningDirection;
    }

    @Override
    public AbstractActionReport duplicate() {
	Set<VacuumWorldCoordinates> perceptionKeysCopy = new HashSet<>();
	getPerceptionKeys().forEach(coordinates -> perceptionKeysCopy.add(coordinates.duplicate()));

	TurnActionReport toReturn = new TurnActionReport(getAction(), getActionResult(), getActorOldDirection(), getActorNewDirection(), getActorOldCoordinates().duplicate(), getActorNewCoordinates().duplicate(), this.turningDirection);
	toReturn.setPerceptionKeys(perceptionKeysCopy);

	return toReturn;
    }
}