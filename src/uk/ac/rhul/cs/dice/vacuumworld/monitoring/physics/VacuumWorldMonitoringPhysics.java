package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;

public class VacuumWorldMonitoringPhysics extends AbstractPhysics implements VacuumWorldMonitoringPhysicsInterface {

	@Override
	public Result attempt(Event event, Space context) {
		EnvironmentalAction action = ((VacuumWorldMonitoringEvent) event).getAction();
		return action.attempt(this, context);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringContainer && arg instanceof VacuumWorldMonitoringEvent) {
			((VacuumWorldMonitoringEvent) arg).attempt(this, (VacuumWorldMonitoringContainer) o);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VacuumWorldMonitoringActionResult) {
			notifyObservers((VacuumWorldMonitoringActionResult) arg, VacuumWorldMonitoringContainer.class);
		}
	}

	@Override
	public boolean isPossible(TotalPerceptionAction action, Space context) {
		return true;
	}

	@Override
	public boolean isNecessary(TotalPerceptionAction action, Space context) {
		return false;
	}

	@Override
	public Result perform(TotalPerceptionAction action, Space context) {
		notifyObservers(action, VacuumWorldMonitoringBridge.class);
		
		return null;
	}

	@Override
	public boolean succeeded(TotalPerceptionAction action, Space context) {
		return true;
	}
}