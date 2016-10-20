package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;

public interface VacuumWorldMonitoringPhysicsInterface {
	public abstract boolean isPossible(TotalPerceptionAction action, Space context);
	public abstract boolean isNecessary(TotalPerceptionAction action, Space context);
	public abstract Result perform(TotalPerceptionAction action, Space context);
	public abstract boolean succeeded(TotalPerceptionAction action, Space context);
}