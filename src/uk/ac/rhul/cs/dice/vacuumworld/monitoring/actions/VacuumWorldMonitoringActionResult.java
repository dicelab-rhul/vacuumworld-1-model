package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.AbstractActionReport;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractActionResult;

public class VacuumWorldMonitoringActionResult extends AbstractActionResult {
    private Map<String, AbstractActionReport> cycleReports;

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

    public Map<String, AbstractActionReport> getCycleReports() {
	return this.cycleReports;
    }

    public void setCycleReports(Map<String, AbstractActionReport> cycleReports) {
	this.cycleReports = cycleReports;
    }

    @Override
    public VacuumWorldMonitoringPerception getPerception() {
	return (VacuumWorldMonitoringPerception) super.getPerception();
    }
}