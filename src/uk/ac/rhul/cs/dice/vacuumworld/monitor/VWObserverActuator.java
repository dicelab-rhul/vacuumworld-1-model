package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

/**
 * The Vacuum World implementation of {@link ObserverActuator}. This
 * {@link Actuator} is set up to handle {@link MonitoringEvent MonitoringEvents}
 * from {@link VWObserverAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverActuator extends ObserverActuator {

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    if (o instanceof VWObserverAgent && arg instanceof MonitoringEvent) {
      //System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
          //+ o.getClass().getSimpleName() + " " + arg);
      notifyObservers(arg, VacuumWorldMonitoringContainer.class);
    }
  }
}
