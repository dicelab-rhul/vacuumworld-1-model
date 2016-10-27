package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashSet;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class CleanActionReport extends AbstractActionReport {
	private DirtType cleanedDirt;
	
	public CleanActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates, DirtType cleanedDirt) {
		super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates, null);
		
		this.cleanedDirt = cleanedDirt;
	}
	
	public DirtType getCleanedDirt() {
		return this.cleanedDirt;
	}

	@Override
	public AbstractActionReport duplicate() {
		Set<VacuumWorldCoordinates> perceptionKeysCopy = new HashSet<>();
		getPerceptionKeys().forEach(coordinates -> perceptionKeysCopy.add(coordinates.duplicate()));
		
		CleanActionReport toReturn = new CleanActionReport(getAction(), getActionResult(), getActorOldDirection(), getActorNewDirection(), getActorOldCoordinates().duplicate(), getActorNewCoordinates().duplicate(), this.cleanedDirt);
		toReturn.setPerceptionKeys(perceptionKeysCopy);
		
		return toReturn;
	}
}