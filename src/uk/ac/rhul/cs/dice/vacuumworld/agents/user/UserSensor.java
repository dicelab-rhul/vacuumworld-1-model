package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.SensorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class UserSensor extends AbstractSensor {

    public UserSensor(String bodyId, SensorPurpose purpose) {
	super(bodyId, purpose);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldSpace && (arg instanceof VacuumWorldActionResult || arg instanceof VacuumWorldSpeechActionResult)) {
	    notifyObservers(arg, User.class);
	}
    }
}