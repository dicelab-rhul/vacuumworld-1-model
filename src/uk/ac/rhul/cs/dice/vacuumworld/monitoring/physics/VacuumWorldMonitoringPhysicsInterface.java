package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadAgentHistoryAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadAgentsHistoriesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadDirtHistoryAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadDirtsHistoriesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadUserHistoryAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateAgentsHistoriesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateDirtsHistoriesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateUserHistoryAction;
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
	public abstract boolean isNecessary(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(TotalPerceptionAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseReadAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseReadDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadAgentHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseReadAgentHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadAgentHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadAgentHistoryAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadDirtHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseReadDirtHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadDirtHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadDirtHistoryAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseReadUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseReadUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseReadUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseReadUserHistoryAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseUpdateAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseUpdateAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseUpdateAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseUpdateAgentsHistoriesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseUpdateDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseUpdateDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseUpdateDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseUpdateDirtsHistoriesAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(DatabaseUpdateUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(DatabaseUpdateUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract Result perform(DatabaseUpdateUserHistoryAction action, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(DatabaseUpdateUserHistoryAction action, VacuumWorldMonitoringContainer context);
	
	public abstract boolean isPossible(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract boolean isNecessary(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract Result attempt(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract Result perform(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
	public abstract boolean succeeded(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context);
}