package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashSet;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class PerceiveActionReport extends AbstractActionReport {
    public PerceiveActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates) {
	super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates, null);
    }

    @Override
    public AbstractActionReport duplicate() {
	Set<VacuumWorldCoordinates> perceptionKeysCopy = new HashSet<>();
	getPerceptionKeys().forEach(coordinates -> perceptionKeysCopy.add(coordinates.duplicate()));

	PerceiveActionReport toReturn = new PerceiveActionReport(getAction(), getActionResult(), getActorOldDirection(), getActorNewDirection(), getActorOldCoordinates().duplicate(), getActorNewCoordinates().duplicate());
	toReturn.setPerceptionKeys(perceptionKeysCopy);

	return toReturn;
    }
}