package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitorActuator extends AbstractActuator {
  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorAgent && arg instanceof MonitoringEvent) {
      System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
          + o.getClass().getSimpleName() + " " + arg);
      notifyObservers(arg, VacuumWorldMonitoringContainer.class);
    }
  }
}
