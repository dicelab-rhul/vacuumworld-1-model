package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class DropDirtAction extends PhysicalAction {
	private DirtType dirtType;
	
	public DropDirtAction() {
		Random rng = new Random();
		
		if(rng.nextBoolean()) {
			this.dirtType = DirtType.GREEN;
		}
		else {
			this.dirtType = DirtType.ORANGE;
		}
	}
	
	public DropDirtAction(DirtType dirtType) {
		this.dirtType = dirtType;
	}
	
	public DirtType getDirtToDropType() {
		return this.dirtType;
	}
	
	@Override
	public boolean isPossible(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isPossible(this, context);
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isNecessary(this, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).perform(this, context);
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).succeeded(this, context);
	}
}