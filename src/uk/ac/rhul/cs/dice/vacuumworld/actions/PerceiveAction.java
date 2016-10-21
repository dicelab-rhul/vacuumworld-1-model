package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class PerceiveAction extends SensingAction<VacuumWorldPerception> {
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
	public String toString() {
		return "{" + this.getClass().getSimpleName() + ":" + this.getPerceptionRange() + ":" + this.canAgentSeeBehind() + "}";
	}
	
	@Override
	public boolean isPossible(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).isPossible(this, context);
	}
	
	@Override
	public boolean isNecessary(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).isNecessary(this, context);
	}
	
	@Override
	public Result<VacuumWorldPerception> perform(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).perform(this, context);
	}
	
	@Override
	public boolean succeeded(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).succeeded(this, context);
	}
}