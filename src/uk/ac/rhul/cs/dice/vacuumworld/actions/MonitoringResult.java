package uk.ac.rhul.cs.dice.vacuumworld.actions;

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

  /**
   * Constructor.
   * 
   * @param result
   *          of the {@link TotalPerceptionAction} that was performed
   * @param failureReason
   *          the exception that was thrown if this
   *          {@link TotalPerceptionAction} {@link ActionResult#ACTION_FAILED
   *          failed} or was {@link ActionResult#ACTION_IMPOSSIBLE impossible}
   * @param recipientId
   *          the id of {@link ObserverAgent} that performed the action -
   *          allowing this result to be sent back correctly
   * @param representation
   *          the perception that was made
   */
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
