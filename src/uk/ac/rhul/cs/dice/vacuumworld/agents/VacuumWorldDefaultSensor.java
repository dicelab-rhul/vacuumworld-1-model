package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class VacuumWorldDefaultSensor extends AbstractSensor<VacuumWorldSensorRole> {

    public VacuumWorldDefaultSensor(String bodyId, VacuumWorldSensorRole role) {
	super(bodyId, role);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldSpace && (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass()) || VacuumWorldSpeechActionResult.class.isAssignableFrom(arg.getClass()))) {
	    notifyObservers(arg, VacuumWorldCleaningAgent.class);
	}
    }
}