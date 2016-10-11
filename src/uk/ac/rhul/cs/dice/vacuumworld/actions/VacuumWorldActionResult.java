package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldActionResult extends DefaultActionResult {
	private VacuumWorldPerception perception;

	public VacuumWorldActionResult(ActionResult result, VacuumWorldPerception perception, String actorId, List<String> recipientsIds) {
		super(result, actorId, recipientsIds);
		this.perception = perception;
	}

	public VacuumWorldActionResult(ActionResult result, Exception failureReason, VacuumWorldPerception perception, String actorId, List<String> recipientsIds) {
		super(result, actorId, failureReason, recipientsIds);
		this.perception = perception;
	}

	public VacuumWorldActionResult(DefaultActionResult replace, String actorId) {
		super(replace.getActionResult(), actorId, replace.getFailureReason(), null);
		this.perception = null;
	}

	public VacuumWorldPerception getPerception() {
		return this.perception;
	}

	public void setPerception(VacuumWorldPerception perception) {
		this.perception = perception;
	}
}