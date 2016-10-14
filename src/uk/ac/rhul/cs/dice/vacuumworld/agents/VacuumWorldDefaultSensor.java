package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class VacuumWorldDefaultSensor extends AbstractSensor<VacuumWorldSensorRole> {
	
	public VacuumWorldDefaultSensor(String bodyId, VacuumWorldSensorRole role) {
		super(bodyId, role);
	}
	
	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldSpace && arg instanceof DefaultActionResult) {
			manageEnviromnentRequest((DefaultActionResult) arg);
		}
	}

	private void manageEnviromnentRequest(DefaultActionResult result) {
		notifyObservers(result, VacuumWorldCleaningAgent.class);
	}
}