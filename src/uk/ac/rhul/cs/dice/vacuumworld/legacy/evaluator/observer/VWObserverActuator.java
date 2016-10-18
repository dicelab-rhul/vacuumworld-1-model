package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldMonitoringContainer;

/**
 * The Vacuum World implementation of {@link ObserverActuator}. This
 * {@link Actuator} is set up to handle {@link MonitoringEvent MonitoringEvents}
 * from {@link VWObserverAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverActuator extends ObserverActuator<VacuumWorldActuatorRole> {

	public VWObserverActuator(String bodyId, VacuumWorldActuatorRole role) {
		super(bodyId, role);
	}
	
	@Override
	public void updateCon(CustomObservable o, Object arg) {
		if (o instanceof VWObserverAgent && arg instanceof MonitoringEvent) {
			notifyObservers(arg, VacuumWorldMonitoringContainer.class);
		}
	}
}