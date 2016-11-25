package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class DropDirtAction extends PhysicalAction {
    private DirtType dirtType;
    private int dropCycle;

    public DropDirtAction(DirtType dirtType, Integer dropCycle) {
	this.dirtType = dirtType;
	this.dropCycle = dropCycle;
    }

    public DirtType getDirtToDropType() {
	return this.dirtType;
    }

    public int getDropCycle() {
	return this.dropCycle;
    }

    @Override
    public boolean isPossible(Physics physics, Space context) {
	if (physics instanceof VacuumWorldPhysics) {
	    return ((VacuumWorldPhysics) physics).isPossible(this, (VacuumWorldSpace) context);
	}
	else {
	    return physics.isPossible(this, context);
	}
    }

    @Override
    public boolean isNecessary(Physics physics, Space context) {
	if (physics instanceof VacuumWorldPhysics) {
	    return ((VacuumWorldPhysics) physics).isNecessary(this, (VacuumWorldSpace) context);
	}
	else {
	    return physics.isNecessary(this, context);
	}
    }

    @Override
    public Result perform(Physics physics, Space context) {
	if (physics instanceof VacuumWorldPhysics) {
	    return ((VacuumWorldPhysics) physics).perform(this, (VacuumWorldSpace) context);
	}
	else {
	    return physics.perform(this, context);
	}
    }

    @Override
    public boolean succeeded(Physics physics, Space context) {
	if (physics instanceof VacuumWorldPhysics) {
	    return ((VacuumWorldPhysics) physics).succeeded(this, (VacuumWorldSpace) context);
	}
	else {
	    return physics.succeeded(this, context);
	}
    }
}