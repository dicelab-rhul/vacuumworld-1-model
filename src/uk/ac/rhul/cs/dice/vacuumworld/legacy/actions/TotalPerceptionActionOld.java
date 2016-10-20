package uk.ac.rhul.cs.dice.vacuumworld.legacy.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.physics.VacuumWorldMonitoringPhysics;

/**
 * An {@link EnvironmentalAction} extending {@link AbstractAction} that is used
 * to request a complete perception of a {@link Space}. In VacuumWorld this
 * should only be used by the {@link ObserverAgent} and should only be available
 * in {@link VacuumWorldLegacyMonitoringContainer}.
 * 
 * @author Ben Wilkins
 *
 */
public class TotalPerceptionActionOld extends AbstractAction {

	@Override
	public boolean isPossible(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).isPossible(TotalPerceptionActionOld.this, context);
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).isNecessary(TotalPerceptionActionOld.this, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).perform(TotalPerceptionActionOld.this, context);
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return ((VacuumWorldMonitoringPhysics) physics).succeeded(TotalPerceptionActionOld.this, context);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}