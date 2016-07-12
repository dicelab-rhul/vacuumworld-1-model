package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldMonitoringPhysics;

/**
 * An {@link EnvironmentalAction} extending {@link AbstractAction} that is used to request a
 * complete perception of a {@link Space}. In VacuumWorld this should only be
 * used by the {@link ObserverAgent} and should only be available in
 * {@link VacuumWorldMonitoringContainer}.
 * 
 * @author Ben Wilkins
 *
 */
public class TotalPerceptionAction extends AbstractAction {

  @Override
  public boolean isPossible(Physics physics, Space context) {
    return ((VacuumWorldMonitoringPhysics) physics).isPossible(
        TotalPerceptionAction.this, context);
  }

  @Override
  public boolean isNecessary(Physics physics, Space context) {
    return ((VacuumWorldMonitoringPhysics) physics).isNecessary(
        TotalPerceptionAction.this, context);
  }

  @Override
  public Result perform(Physics physics, Space context) {
    return ((VacuumWorldMonitoringPhysics) physics).perform(
        TotalPerceptionAction.this, context);
  }

  @Override
  public boolean succeeded(Physics physics, Space context) {
    return ((VacuumWorldMonitoringPhysics) physics).succeeded(
        TotalPerceptionAction.this, context);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
