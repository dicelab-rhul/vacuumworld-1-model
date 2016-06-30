package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

public class MonitoringResult extends DefaultActionResult {

  private VacuumWorldSpaceRepresentation representation;

  public MonitoringResult(ActionResult result, Exception failureReason,
      String recipientId, VacuumWorldSpaceRepresentation representation) {
    super(result, failureReason, recipientId);
    this.representation = representation;
  }

  public Perception getPerception() {
    return representation;
  }
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
