package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public abstract class TurningAction extends PhysicalAction<VacuumWorldPerception> {
	private ActorFacingDirection actorOldFacingDirection;

	public ActorFacingDirection getActorOldFacingDirection() {
		return this.actorOldFacingDirection;
	}

	public void setActorOldFacingDirection(ActorFacingDirection actorOldFacingDirection) {
		this.actorOldFacingDirection = ActorFacingDirection.fromString(actorOldFacingDirection.toString().toLowerCase());
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