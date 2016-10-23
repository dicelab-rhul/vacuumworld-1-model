package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class VacuumWorldEvent extends VacuumWorldAbstractEvent {
	
	public VacuumWorldEvent(EnvironmentalAction action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}

	@Override
	public String represent() {
		return "at time " + getTimestamp() + " agent " + ((VacuumWorldCleaningAgent) getActor()).getExternalAppearance().getName() + " attempted a " + getAction().getClass().getSimpleName();
	}

	@Override
	public boolean isPossible(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics) physics).isPossible(this, (VacuumWorldSpace) context); 
		}
		else {
			return physics.isPossible(this, context);
		}
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics) physics).isNecessary(this, (VacuumWorldSpace) context); 
		}
		else {
			return physics.isNecessary(this, context);
		}
	}

	@Override
	public Result attempt(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics) physics).attempt(this, (VacuumWorldSpace) context); 
		}
		else {
			return physics.attempt(this, context);
		}
	}

	@Override
	public Result perform(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics) physics).perform(this, (VacuumWorldSpace) context); 
		}
		else {
			return physics.perform(this, context);
		}
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		if(physics instanceof VacuumWorldPhysics) {
			return ((VacuumWorldPhysics) physics).succeeded(this, (VacuumWorldSpace) context); 
		}
		else {
			return physics.succeeded(this, context);
		}
	}
}