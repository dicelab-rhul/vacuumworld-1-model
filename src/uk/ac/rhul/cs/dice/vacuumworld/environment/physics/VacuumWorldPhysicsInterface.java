package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.DropDirtAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;

public interface VacuumWorldPhysicsInterface extends Physics {
	
	@Override
	public default boolean isPossible(EnvironmentalAction action, Space context) {
		return false;
	}
	
	@Override
	public default boolean isNecessary(EnvironmentalAction action, Space context) {
		return false;
	}
	
	@Override
	public default Result attempt(EnvironmentalAction action, Space context) {
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default Result perform(EnvironmentalAction action, Space context) {
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default boolean succeeded(EnvironmentalAction action, Space context) {
		return false;
	}
	
	@Override
	public default boolean isPossible(Event event, Space context) {
		return false;
	}
	
	@Override
	public default boolean isNecessary(Event event, Space context) {
		return false;
	}
	
	@Override
	public default Result attempt(Event event, Space context) {
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default Result perform(Event event, Space context) {
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default boolean succeeded(Event event, Space context) {
		return false;
	}
	
	public abstract boolean isPossible(TurnLeftAction action, VacuumWorldSpace context);
	public abstract Result perform(TurnLeftAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(TurnLeftAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(TurnRightAction action, VacuumWorldSpace context);
	public abstract Result perform(TurnRightAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(TurnRightAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(MoveAction action, VacuumWorldSpace context);
	public abstract Result perform(MoveAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(MoveAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(CleanAction action, VacuumWorldSpace context);
	public abstract Result perform(CleanAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(CleanAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(PerceiveAction action, VacuumWorldSpace context);
	public abstract Result perform(PerceiveAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(PerceiveAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(SpeechAction action, VacuumWorldSpace context);
	public abstract Result perform(SpeechAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(SpeechAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(DropDirtAction action, VacuumWorldSpace context);
	public abstract Result perform(DropDirtAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(DropDirtAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(TotalPerceptionAction action, VacuumWorldSpace context);
	public abstract Result perform(TotalPerceptionAction action, VacuumWorldSpace context);
	public abstract boolean succeeded(TotalPerceptionAction action, VacuumWorldSpace context);
	
	public abstract boolean isPossible(VacuumWorldEvent event, VacuumWorldSpace context);
	public abstract Result attempt(VacuumWorldEvent event, VacuumWorldSpace context);
	public abstract Result perform(VacuumWorldEvent event, VacuumWorldSpace context);
	public abstract boolean succeeded(VacuumWorldEvent event, VacuumWorldSpace context);
}