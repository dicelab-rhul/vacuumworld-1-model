package uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;

public class VacuumWorldMonitorActuator extends AbstractActuator<VacuumWorldActuatorRole> {
	
	public VacuumWorldMonitorActuator(String bodyId, VacuumWorldActuatorRole role) {
		super(bodyId, role);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitorAgent && arg instanceof MonitoringEvent) {
			notifyObservers(arg, VacuumWorldLegacyMonitoringContainer.class);
		}
	}
}