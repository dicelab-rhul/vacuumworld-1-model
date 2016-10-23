package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractActuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class UserActuator extends AbstractActuator<VacuumWorldActuatorRole> {

	public UserActuator(String bodyId, VacuumWorldActuatorRole role) {
		super(bodyId, role);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof User) {
			executeUserBodyRequest(arg);
		}
	}

	private void executeUserBodyRequest(Object arg) {
		if(arg instanceof VacuumWorldEvent) {
			executeUserBodyRequest((VacuumWorldEvent) arg);
		}
	}
	
	private void executeUserBodyRequest(VacuumWorldEvent event) {
		notifyObservers(event, VacuumWorldSpace.class);
	}
	
	public boolean canHandle(Class<?> action) {
		if(AbstractAction.class.isAssignableFrom(action)) {
			return true;
		}
		
		return false;
	}
}