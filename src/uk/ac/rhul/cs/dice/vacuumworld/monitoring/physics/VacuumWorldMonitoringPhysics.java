package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseReadStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.MongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringPhysics extends AbstractPhysics implements VacuumWorldMonitoringPhysicsInterface {

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringContainer && arg instanceof VWPair<?, ?>) {
			manageMonitoringContainerRequest((VWPair<?, ?>) arg);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VacuumWorldMonitoringActionResult) {
			notifyObservers((VacuumWorldMonitoringActionResult) arg, VacuumWorldMonitoringContainer.class);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VacuumWorldActionResult) {
			VacuumWorldActionResult temp = (VacuumWorldActionResult) arg;
			VacuumWorldMonitoringActionResult result = new VacuumWorldMonitoringActionResult(temp.getActionResult(), temp.getActorId(), temp.getFailureReason(), temp.getRecipientsIds());
			notifyObservers(result, VacuumWorldMonitoringContainer.class);
		}
	}

	private void manageMonitoringContainerRequest(VWPair<?, ?> arg) {
		if(arg.checkClasses(VacuumWorldMonitoringEvent.class, VacuumWorldMonitoringContainer.class)) {
			Result result = ((VacuumWorldMonitoringEvent) arg.getFirst()).attempt(this, (VacuumWorldMonitoringContainer) arg.getSecond());
			forwardEventIfNeededOrSendResultBack((VacuumWorldMonitoringEvent) arg.getFirst(), result);
		}
	}

	private void forwardEventIfNeededOrSendResultBack(VacuumWorldMonitoringEvent event, Result result) {
		if(ActionResult.ACTION_FAILED.equals(result.getActionResult()) || ActionResult.ACTION_IMPOSSIBLE.equals(result.getActionResult())) {
			notifyObservers((VacuumWorldMonitoringActionResult) result, VacuumWorldMonitoringContainer.class);
		}
		else {
			VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + event.getActor().getId() + ": forwarded a total perception request to the bridge...");
			
			TotalPerceptionAction action = event.getAction() instanceof TotalPerceptionAction ? (TotalPerceptionAction) event.getAction() : new TotalPerceptionAction();
			action.setActor(event.getAction().getActor());
			Event totalPerceptionEvent = new VacuumWorldEvent(action, System.currentTimeMillis(), action.getActor());
			totalPerceptionEvent.setSensorToCallBackId(event.getSensorToCallBackId());
			notifyObservers(totalPerceptionEvent, VacuumWorldMonitoringBridge.class);
		}
	}

	@Override
	public synchronized boolean isPossible(TotalPerceptionAction action, VacuumWorldMonitoringContainer context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TotalPerceptionAction action, VacuumWorldMonitoringContainer context) {
		return false;
	}

	@Override
	public synchronized Result perform(TotalPerceptionAction action, VacuumWorldMonitoringContainer context) {
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, action.getActor().getId(), new ArrayList<>(), null);
	}

	@Override
	public synchronized boolean succeeded(TotalPerceptionAction action, VacuumWorldMonitoringContainer context) {
		return true;
	}
	
	@Override
	public boolean isPossible(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context) {
		return VWUtils.isCollectionNotNullAndNotEmpty(action.getStates());
	}

	@Override
	public boolean isNecessary(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context) {
		return false;
	}

	@Override
	public Result perform(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context) {
		MongoConnector connector = new MongoConnector();
		
		if(connector.updateSystemStates(action.getStates())) {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		}
		else {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), null, new ArrayList<>());
		}
	}

	@Override
	public boolean succeeded(DatabaseUpdateStatesAction action, VacuumWorldMonitoringContainer context) {
		return true;
	}
	
	@Override
	public boolean isPossible(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context) {
		return true;
	}

	@Override
	public boolean isNecessary(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context) {
		return false;
	}

	@Override
	public Result perform(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context) {
		MongoConnector connector = new MongoConnector();
		List<JsonObject> states = connector.getStates();
		
		if(states.isEmpty()) {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		}
		else {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), null, new ArrayList<>());
		}
	}

	@Override
	public boolean succeeded(DatabaseReadStatesAction action, VacuumWorldMonitoringContainer context) {
		return true;
	}
	
	@Override
	public boolean isPossible(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context) {
		return event.getAction().isPossible(this, context);
	}

	@Override
	public boolean isNecessary(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context) {
		return event.getAction().isNecessary(this, context);
	}

	@Override
	public synchronized Result attempt(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context) {
		if(event.isPossible(this, context)) {
			Result result = event.perform(this, context);
			result.setRecipientsIds(Arrays.asList(event.getSensorToCallBackId()));
			
			if(!ActionResult.ACTION_DONE.equals(result.getActionResult())) {
				return result;
			}
			
			if(!event.succeeded(this, context)) {
				return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), null, Arrays.asList(event.getSensorToCallBackId()));
			}
			else {
				return result;
			}
		}
		else {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_IMPOSSIBLE, null, Arrays.asList(event.getSensorToCallBackId()));
		}
	}

	@Override
	public synchronized Result perform(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context) {
		return event.getAction().perform(this, context);
	}

	@Override
	public synchronized boolean succeeded(VacuumWorldMonitoringEvent event, VacuumWorldMonitoringContainer context) {
		return event.getAction().succeeded(this, context);
	}
}