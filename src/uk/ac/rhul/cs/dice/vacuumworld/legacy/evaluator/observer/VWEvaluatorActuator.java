package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;

public class VWEvaluatorActuator extends EvaluatorActuator<VacuumWorldActuatorRole> {

	public VWEvaluatorActuator(String bodyId, VacuumWorldActuatorRole role) {
		super(bodyId, role);
	}
	
	@Override
	public void updateCon(CustomObservable o, Object arg) {
		// TODO
	}
}