package uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics;

import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;

public class VacuumWorldMonitoringPhysics extends AbstractPhysics implements VacuumWorldMonitoringPhysicsInterface {

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringContainer && arg instanceof VWPair<?, ?>) {
			manageMonitoringContainerRequest((VWPair<?, ?>) arg);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VacuumWorldMonitoringActionResult) {
			notifyObservers((VacuumWorldMonitoringActionResult) arg, VacuumWorldMonitoringContainer.class);
		}
	}

	private void manageMonitoringContainerRequest(VWPair<?, ?> arg) {
		if(arg.checkClasses(VacuumWorldMonitoringEvent.class, VacuumWorldMonitoringContainer.class)) {
			((VacuumWorldMonitoringEvent) arg.getFirst()).attempt(this, (VacuumWorldMonitoringContainer) arg.getSecond());
		}
	}

	@Override
	public synchronized boolean isPossible(TotalPerceptionAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TotalPerceptionAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TotalPerceptionAction action, Space context) {
		Event totalPerceptionEvent = new VacuumWorldEvent(action, System.currentTimeMillis(), action.getActor());
		notifyObservers(totalPerceptionEvent, VacuumWorldMonitoringBridge.class);
		
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, action.getActor().getId(), new ArrayList<>(), null);
	}

	@Override
	public synchronized boolean succeeded(TotalPerceptionAction action, Space context) {
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
			
			if(!event.succeeded(this, context)) {
				return editResultIfnecessary(event, result);
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
	
	private Result editResultIfnecessary(VacuumWorldMonitoringEvent event, Result result) {
		if(ActionResult.ACTION_FAILED.equals(result.getActionResult())) {
			return result;
		}
		
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), result.getFailureReason(), Arrays.asList(event.getSensorToCallBackId()));
	}
}