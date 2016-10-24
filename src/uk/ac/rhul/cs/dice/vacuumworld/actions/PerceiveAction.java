package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class PerceiveAction extends SensingAction {
	private int range;
	private boolean behind;
	
	public PerceiveAction() {
		this.range = 2;
		this.behind = false;
	}
	
	public PerceiveAction(Integer range, Boolean behind) {
		this.range = range;
		this.behind = behind;
	}
	
	public int getPerceptionRange() {
		return this.range;
	}

	public boolean canAgentSeeBehind() {
		return this.behind;
	}
	
	@Override
	public String toString() {
		return "{" + this.getClass().getSimpleName() + ":" + this.getPerceptionRange() + ":" + this.canAgentSeeBehind() + "}";
	}
	
	@Override
	public boolean isPossible(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics)physics).isPossible(this, (VacuumWorldSpace) context);
		}
		else {
			return physics.isPossible(this, context);
		}
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics)physics).isNecessary(this, (VacuumWorldSpace) context);
		}
		else {
			return physics.isNecessary(this, context);
		}
	}

	@Override
	public Result perform(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics)physics).perform(this, (VacuumWorldSpace) context);
		}
		else {
			return physics.perform(this, context);
		}
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics)physics).succeeded(this, (VacuumWorldSpace) context);
		}
		else {
			return physics.succeeded(this, context);
		}
	}
}