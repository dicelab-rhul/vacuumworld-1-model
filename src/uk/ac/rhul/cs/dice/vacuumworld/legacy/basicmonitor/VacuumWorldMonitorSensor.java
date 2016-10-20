package uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;

public class VacuumWorldMonitorSensor extends AbstractSensor<VacuumWorldSensorRole> {

	public VacuumWorldMonitorSensor(String bodyId, VacuumWorldSensorRole role) {
		super(bodyId, role);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldLegacyMonitoringContainer && arg instanceof MonitoringResult) {
			notifyObservers(arg, VacuumWorldMonitorAgent.class);
		}
	}
}