package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Brain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;

/**
 * The Vacuum World implementation of {@link ObserverBrain}. This {@link Brain}
 * is set up to handle {@link TotalPerceptionAction TotalPerceptionActions} from
 * the {@link VWObserverMind} and {@link MonitoringResult MonitoringResults}
 * from the {@link VWObserverAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverBrain extends ObserverBrain {

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    if (o instanceof VWObserverMind && arg instanceof TotalPerceptionAction) {
      notifyObservers(arg, VWObserverAgent.class);
    } else if (o instanceof VWObserverAgent && arg instanceof MonitoringResult) {
      notifyObservers(arg, VWObserverMind.class);
    }
  }
}
