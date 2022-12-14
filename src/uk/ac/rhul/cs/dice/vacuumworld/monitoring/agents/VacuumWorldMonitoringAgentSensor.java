package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.SensorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitoringAgentSensor extends VacuumWorldDefaultSensor {

    public VacuumWorldMonitoringAgentSensor(String bodyId, SensorPurpose purpose) {
	super(bodyId, purpose);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldMonitoringContainer && arg instanceof VacuumWorldMonitoringActionResult) {
	    notifyObservers(arg, VacuumWorldMonitoringAgent.class);
	}
    }
}