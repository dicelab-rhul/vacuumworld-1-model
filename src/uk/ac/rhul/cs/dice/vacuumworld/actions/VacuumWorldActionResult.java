package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldActionResult extends DefaultActionResult {
	private VacuumWorldPerception perception;
	
	public VacuumWorldActionResult(ActionResult result, VacuumWorldPerception perception, String recipientId) {
		super(result, recipientId);
		this.perception = perception;
	}

	public VacuumWorldActionResult(ActionResult result, Exception failureReason, VacuumWorldPerception perception, String recipientId) {
		super(result, failureReason, recipientId);
		this.perception = perception;
	}
	
	public VacuumWorldPerception getPerception() {
		return this.perception;
	}
}
