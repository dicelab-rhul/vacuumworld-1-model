package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverBrain;

public class VWObserverBrain extends ObserverBrain {

  public VWObserverBrain(Class<?> mindObserver, Class<?> agentObserver) {
    super(mindObserver, agentObserver);
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
        + o.getClass().getSimpleName() + " " + arg);

    if (o instanceof VWObserverMind && arg instanceof Action) {
      notifyObservers(arg, VWObserverAgent.class);
    } else if (o instanceof VWObserverAgent && arg instanceof MonitoringResult) {
      notifyObservers(arg, VWObserverMind.class);
    }
  }
}
