package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitorActuator extends AbstractActuator {
  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorAgent && arg instanceof MonitoringEvent) {
      notifyObservers(arg, VacuumWorldMonitoringContainer.class);
    }
  }
}