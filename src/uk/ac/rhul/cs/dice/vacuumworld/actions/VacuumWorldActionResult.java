package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldActionResult extends DefaultActionResult<VacuumWorldPerception> {
	public VacuumWorldActionResult(ActionResult result, VacuumWorldPerception perception, String actorId, List<String> recipientsIds) {
		super(result, actorId, recipientsIds, perception);
	}

	public VacuumWorldActionResult(ActionResult result, Exception failureReason, String actorId, List<String> recipientsIds) {
		super(result, actorId, failureReason, recipientsIds);
	}

	public VacuumWorldActionResult(DefaultActionResult<VacuumWorldPerception> replace, String actorId) {
		super(replace.getActionResult(), actorId, replace.getFailureReason(), null);
	}
}