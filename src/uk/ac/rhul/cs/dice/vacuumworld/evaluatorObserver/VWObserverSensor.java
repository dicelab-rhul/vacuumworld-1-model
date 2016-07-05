package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverSensor;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

/**
 * The Vacuum World implementation of {@link ObserverSensor}. This
 * {@link Sensor} is set up to handle {@link MonitoringResult MonitoringResults}
 * from {@link VacuumWorldContainer}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverSensor extends ObserverSensor {

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitoringContainer
        && arg instanceof MonitoringResult) {
      notifyObservers(arg, VWObserverAgent.class);
    }
  }
}
