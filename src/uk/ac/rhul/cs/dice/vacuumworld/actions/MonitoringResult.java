package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;

/**
 * A {@link Result} extending {@link DefaultActionResult} that is specific to
 * monitoring Vacuum World. It contains a {@link VacuumWorldSpaceRepresentation}
 * that is the {@link RefinedPerception} of {@link VacuumWorldSpace}. An
 * instance of this class will always be the result of a
 * {@link TotalPerceptionAction} performed by an {@link ObserverAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class MonitoringResult extends DefaultActionResult {
  private VacuumWorldSpaceRepresentation representation;

  public MonitoringResult(ActionResult result, Exception failureReason,
      List<String> recipientsIds, VacuumWorldSpaceRepresentation representation) {
    super(result, failureReason, recipientsIds);
    this.representation = representation;
  }

  public Perception getPerception() {
    return this.representation;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
