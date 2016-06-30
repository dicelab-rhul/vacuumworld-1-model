package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverSensor;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

public class VWObserverSensor extends ObserverSensor {

  public VWObserverSensor(Class<?> observerAgent) {
    super(observerAgent);
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
        + o.getClass().getSimpleName() + " " + arg);
    if (o instanceof VacuumWorldMonitoringContainer
        && arg instanceof MonitoringResult) {
      notifyObservers(arg, VWObserverAgent.class);
    }
  }
}
