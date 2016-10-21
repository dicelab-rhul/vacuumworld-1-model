package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class UserSensor extends AbstractSensor<VacuumWorldSensorRole> {

	public UserSensor(String bodyId, VacuumWorldSensorRole role) {
		super(bodyId, role);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldSpace && arg instanceof DefaultActionResult) {
			notifyObservers(arg, User.class);
		}
	}
}