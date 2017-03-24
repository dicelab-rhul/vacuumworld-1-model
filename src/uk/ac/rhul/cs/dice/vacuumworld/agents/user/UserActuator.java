package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorPurpose;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class UserActuator extends AbstractActuator<VacuumWorldActuatorPurpose> {

    public UserActuator(String bodyId, VacuumWorldActuatorPurpose role) {
	super(bodyId, role);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof User) {
	    executeUserBodyRequest(arg);
	}
    }

    private void executeUserBodyRequest(Object arg) {
	if (arg instanceof VacuumWorldEvent) {
	    executeUserBodyRequest((VacuumWorldEvent) arg);
	}
    }

    private void executeUserBodyRequest(VacuumWorldEvent event) {
	notifyObservers(event, VacuumWorldSpace.class);
    }
}