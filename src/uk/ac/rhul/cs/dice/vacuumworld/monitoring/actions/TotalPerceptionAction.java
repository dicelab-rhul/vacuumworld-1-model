package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;

public class TotalPerceptionAction extends SensingAction {

	@Override
	public boolean isPossible(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).isPossible(this, context);
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).isNecessary(this, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).perform(this, context);
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).succeeded(this, context);
	}
}