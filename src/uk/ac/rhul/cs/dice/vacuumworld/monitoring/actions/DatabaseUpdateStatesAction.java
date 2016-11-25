package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.List;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;

public class DatabaseUpdateStatesAction extends DatabaseWriteAction {
    private List<JsonObject> states;

    public DatabaseUpdateStatesAction(List<JsonObject> states) {
	super(VacuumWorldDatabaseInteractions.UPDATE_STATES);

	this.states = states;
    }

    public List<JsonObject> getStates() {
	return this.states;
    }

    @Override
    public boolean isPossible(Physics physics, Space context) {
	if (physics instanceof VacuumWorldMonitoringPhysics) {
	    return ((VacuumWorldMonitoringPhysics) physics).isPossible(this, (VacuumWorldMonitoringContainer) context);
	}
	else {
	    return physics.isPossible(this, context);
	}
    }

    @Override
    public boolean isNecessary(Physics physics, Space context) {
	if (physics instanceof VacuumWorldMonitoringPhysics) {
	    return ((VacuumWorldMonitoringPhysics) physics).isNecessary(this, (VacuumWorldMonitoringContainer) context);
	}
	else {
	    return physics.isNecessary(this, context);
	}
    }

    @Override
    public Result perform(Physics physics, Space context) {
	if (physics instanceof VacuumWorldMonitoringPhysics) {
	    return ((VacuumWorldMonitoringPhysics) physics).perform(this, (VacuumWorldMonitoringContainer) context);
	}
	else {
	    return physics.perform(this, context);
	}
    }

    @Override
    public boolean succeeded(Physics physics, Space context) {
	if (physics instanceof VacuumWorldMonitoringPhysics) {
	    return ((VacuumWorldMonitoringPhysics) physics).succeeded(this, (VacuumWorldMonitoringContainer) context);
	}
	else {
	    return physics.succeeded(this, context);
	}
    }
}