package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldMonitoringPhysics;

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
