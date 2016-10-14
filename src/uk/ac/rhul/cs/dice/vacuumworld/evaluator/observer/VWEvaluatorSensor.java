package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;

public class VWEvaluatorSensor extends EvaluatorSensor<VacuumWorldSensorRole> {

	public VWEvaluatorSensor(String bodyId, VacuumWorldSensorRole role) {
		super(bodyId, role);
	}
	
	@Override
	public void updateCon(CustomObservable o, Object arg) {
		//TODO
	}
}