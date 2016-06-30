package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.common.PerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;

public class DefaultPerceptionRefiner implements PerceptionRefiner {

  @Override
  public RefinedPerception refinePerception(Perception perception) {
    return (RefinedPerception) perception;
  }
}
