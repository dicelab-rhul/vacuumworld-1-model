package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashSet;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class DropDirtActionReport extends AbstractActionReport {
    private DirtType droppedDirtType;

    public DropDirtActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates, DirtType droppedDirtType) {
	super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates, null);

	this.droppedDirtType = droppedDirtType;
    }

    public DirtType getDroppedDirtType() {
	return this.droppedDirtType;
    }

    public void setDroppedDirtType(DirtType droppedDirtType) {
	this.droppedDirtType = droppedDirtType;
    }

    @Override
    public AbstractActionReport duplicate() {
	Set<VacuumWorldCoordinates> perceptionKeysCopy = new HashSet<>();
	getPerceptionKeys().forEach(coordinates -> perceptionKeysCopy.add(coordinates.duplicate()));

	DropDirtActionReport toReturn = new DropDirtActionReport(getAction(), getActionResult(), getActorOldDirection(), getActorNewDirection(), getActorOldCoordinates().duplicate(), getActorNewCoordinates().duplicate(), this.droppedDirtType);
	toReturn.setPerceptionKeys(perceptionKeysCopy);

	return toReturn;
    }
}