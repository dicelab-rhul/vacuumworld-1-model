package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class PerceiveAction extends SensingAction {
	private int range;
	private boolean behind;
	
	public PerceiveAction() {
		this.range = 2;
		this.behind = false;
	}
	
	public PerceiveAction(int range, boolean behind) {
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
	public boolean isPossible(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isPossible(PerceiveAction.this, context);
	}
	
	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isNecessary(PerceiveAction.this, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).perform(PerceiveAction.this, context);
	}
	
	@Override
	public boolean succeeded(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).succeeded(PerceiveAction.this, context);
	}
	
	@Override
	public String toString() {
	  return "{" + this.getClass().getSimpleName() + ":" + this.getPerceptionRange() + ":" + this.canAgentSeeBehind() + "}";
	}
	
	
	
}