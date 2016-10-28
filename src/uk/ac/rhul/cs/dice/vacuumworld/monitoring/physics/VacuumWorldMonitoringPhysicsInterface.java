package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateActionsAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;

public interface VacuumWorldMonitoringPhysicsInterface extends Physics {
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
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default Result perform(EnvironmentalAction action, Space context) {
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
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
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default Result perform(Event event, Space context) {
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), new UnsupportedOperationException(), new ArrayList<>());
	}
	
	@Override
	public default boolean succeeded(Event event, Space context) {
		return false;
	}
	
	public abstract boolean isPossible(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseUpdateActionsAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseUpdateActionsAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseUpdateActionsAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract Result attempt(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract Result perform(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
}