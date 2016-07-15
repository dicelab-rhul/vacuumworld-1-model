package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitorSensor extends AbstractSensor {

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitoringContainer && arg instanceof MonitoringResult) {
			notifyObservers(arg, VacuumWorldMonitorAgent.class);
		}
	}
}