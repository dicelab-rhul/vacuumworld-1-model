package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;

public interface VacuumWorldPhysicsInterface {
	public boolean isPossible(TurnLeftAction action, Space context);
	public boolean isNecessary(TurnLeftAction action, Space context);
	public Result perform(TurnLeftAction action, Space context);
	public boolean succeeded(TurnLeftAction action, Space context);
	
	public boolean isPossible(TurnRightAction action, Space context);
	public boolean isNecessary(TurnRightAction action, Space context);
	public Result perform(TurnRightAction action, Space context);
	public boolean succeeded(TurnRightAction action, Space context);
	
	public boolean isPossible(MoveAction action, Space context);
	public boolean isNecessary(MoveAction action, Space context);
	public Result perform(MoveAction action, Space context);
	public boolean succeeded(MoveAction action, Space context);
	
	public boolean isPossible(CleanAction action, Space context);
	public boolean isNecessary(CleanAction action, Space context);
	public Result perform(CleanAction action, Space context);
	public boolean succeeded(CleanAction action, Space context);
	
	public boolean isPossible(PerceiveAction action, Space context);
	public boolean isNecessary(PerceiveAction action, Space context);
	public Result perform(PerceiveAction action, Space context);
	public boolean succeeded(PerceiveAction action, Space context);
}