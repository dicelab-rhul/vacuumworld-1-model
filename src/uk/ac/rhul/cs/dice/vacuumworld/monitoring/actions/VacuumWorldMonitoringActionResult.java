package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;

public class VacuumWorldMonitoringActionResult extends DefaultActionResult<VacuumWorldMonitoringPerception> {	
	public VacuumWorldMonitoringActionResult(ActionResult result, List<String> recipientsIds, VacuumWorldMonitoringPerception perception) {
		super(result, recipientsIds, perception);
	}
	
	public VacuumWorldMonitoringActionResult(ActionResult result, Exception failureReason, List<String> recipientsIds) {
		super(result, failureReason, recipientsIds);
	}
	
	public VacuumWorldMonitoringActionResult(ActionResult result, String actorId, List<String> recipientsIds, VacuumWorldMonitoringPerception perception) {
		super(result, actorId, recipientsIds, perception);
	}
	
	public VacuumWorldMonitoringActionResult(ActionResult result, String actorId, Exception failureReason, List<String> recipientsIds) {
		super(result, actorId, failureReason, recipientsIds);
	}
}