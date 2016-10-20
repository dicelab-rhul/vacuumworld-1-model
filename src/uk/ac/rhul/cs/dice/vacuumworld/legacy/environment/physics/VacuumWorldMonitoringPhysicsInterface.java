package uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.physics;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionActionOld;

public interface VacuumWorldMonitoringPhysicsInterface {
	public boolean isPossible(TotalPerceptionActionOld action, Space context);
	public boolean isNecessary(TotalPerceptionActionOld action, Space context);
	public Result perform(TotalPerceptionActionOld action, Space context);
	public boolean succeeded(TotalPerceptionActionOld action, Space context);
}