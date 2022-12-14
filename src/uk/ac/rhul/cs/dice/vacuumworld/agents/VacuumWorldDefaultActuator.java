package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.ActuatorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class VacuumWorldDefaultActuator extends AbstractActuator {

    public VacuumWorldDefaultActuator(String bodyId, ActuatorPurpose purpose) {
	super(bodyId, purpose);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldCleaningAgent) {
	    executeBodyRequest(arg);
	}
    }

    private void executeBodyRequest(Object arg) {
	if (arg instanceof VacuumWorldEvent) {
	    executeBodyRequest((VacuumWorldEvent) arg);
	}
    }

    private void executeBodyRequest(VacuumWorldEvent event) {
	notifyObservers(event, VacuumWorldSpace.class);
    }
}