package uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.physics;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionAction;

public interface VacuumWorldMonitoringPhysicsInterface {
	public boolean isPossible(TotalPerceptionAction action, Space context);
	public boolean isNecessary(TotalPerceptionAction action, Space context);
	public Result perform(TotalPerceptionAction action, Space context);
	public boolean succeeded(TotalPerceptionAction action, Space context);
}