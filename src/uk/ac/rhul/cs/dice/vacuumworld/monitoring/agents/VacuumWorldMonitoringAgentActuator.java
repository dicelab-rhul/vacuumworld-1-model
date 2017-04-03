package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.ActuatorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitoringAgentActuator extends AbstractActuator {

    public VacuumWorldMonitoringAgentActuator(String bodyId, ActuatorPurpose purpose) {
	super(bodyId, purpose);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldMonitoringAgent && arg instanceof VacuumWorldMonitoringEvent) {
	    notifyObservers((VacuumWorldMonitoringEvent) arg, VacuumWorldMonitoringContainer.class);
	}
    }
}