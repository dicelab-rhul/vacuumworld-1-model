package uk.ac.rhul.cs.dice.vacuumworld.actions.result;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldActionResult extends AbstractActionResult {
    
    public VacuumWorldActionResult(ActionResult result, List<String> recipientsIds, VacuumWorldPerception perception) {
	super(result, recipientsIds, perception);
    }

    public VacuumWorldActionResult(ActionResult result, Exception failureReason, List<String> recipientsIds) {
	super(result, failureReason, recipientsIds);
    }

    public VacuumWorldActionResult(ActionResult result, String actorId, List<String> recipientsIds, VacuumWorldPerception perception) {
	super(result, actorId, recipientsIds, perception);
    }

    public VacuumWorldActionResult(ActionResult result, String actorId, Exception failureReason, List<String> recipientsIds) {
	super(result, actorId, failureReason, recipientsIds);
    }

    @Override
    public VacuumWorldPerception getPerception() {
	return (VacuumWorldPerception) super.getPerception();
    }
}