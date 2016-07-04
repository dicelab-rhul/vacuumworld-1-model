package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorSensor;

public class VWEvaluatorSensor extends EvaluatorSensor {

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    // Only looks at the database via is superclass
  }
}
