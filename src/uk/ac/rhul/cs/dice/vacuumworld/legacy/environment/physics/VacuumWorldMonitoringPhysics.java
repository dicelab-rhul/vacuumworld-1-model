package uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionActionOld;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;

public class VacuumWorldMonitoringPhysics extends AbstractPhysics implements VacuumWorldMonitoringPhysicsInterface, CustomObserver {
	private Physics monitoredContainerPhysics;
	private ConcurrentMap<Long, AbstractAgent<?, ?>> activeAgents;
	private ConcurrentMap<Long, List<String>> sensorsToNotify;

	public VacuumWorldMonitoringPhysics() {
		this.activeAgents = new ConcurrentHashMap<>();
		this.sensorsToNotify = new ConcurrentHashMap<>();
	}

	public Physics getMonitoredContainerPhysics() {
		return this.monitoredContainerPhysics;

	}

	public void setMonitoredContainerPhysics(Physics monitoredContainerPysics) {
		this.monitoredContainerPhysics = monitoredContainerPysics;
	}

	@Override
	public synchronized Result attempt(Event event, Space context) {
		this.activeAgents.put(Thread.currentThread().getId(), (AbstractAgent<?, ?>) event.getActor());
		this.sensorsToNotify.putIfAbsent(Thread.currentThread().getId(), new ArrayList<>());
		this.sensorsToNotify.get(Thread.currentThread().getId()).add(((MonitoringEvent) event).getSensorToCallBackId());

		return event.getAction().attempt(this, context);
	}

	@Override
	public synchronized boolean isPossible(TotalPerceptionActionOld action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TotalPerceptionActionOld action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TotalPerceptionActionOld action, Space context) {
		return new MonitoringResult(ActionResult.ACTION_DONE, null, null, ((VacuumWorldLegacyMonitoringContainer) context).getVacuumWorldSpaceRepresentation());
	}

	@Override
	public synchronized boolean succeeded(TotalPerceptionActionOld action, Space context) {
		return true;
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldLegacyMonitoringContainer && arg instanceof Object[]) {
			manageEnvironmentRequest((Object[]) arg);
		}
	}

	private synchronized void manageEnvironmentRequest(Object[] arg) {
		if (arg.length != 2) {
			return;
		}

		if (arg[0] instanceof MonitoringEvent && arg[1] instanceof VacuumWorldLegacyMonitoringContainer) {
			attemptEvent((MonitoringEvent) arg[0], (VacuumWorldLegacyMonitoringContainer) arg[1]);
		}
	}

	private synchronized void attemptEvent(MonitoringEvent event, VacuumWorldLegacyMonitoringContainer context) {
		Result result = event.attempt(this, context);
		
		if (result.getRecipientsIds() == null) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
		
		if(result.getRecipientsIds().isEmpty()) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}

		this.activeAgents.remove(Thread.currentThread().getId());
		this.sensorsToNotify.remove(Thread.currentThread().getId());

		notifyObservers(result, VacuumWorldLegacyMonitoringContainer.class);
	}
}