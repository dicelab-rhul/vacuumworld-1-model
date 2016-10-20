package uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;

public class VacuumWorldMonitoringBridge extends CustomObservable implements MonitoringBridgeInterface<VacuumWorldMonitoringContainer, VacuumWorldMonitoringPhysics, VacuumWorldSpace, VacuumWorldPhysics> {	
	private Class<? extends VacuumWorldMonitoringContainer> monitoringContainerClass;
	private Class<? extends VacuumWorldSpace> monitoredContainerClass;
	private Class<? extends VacuumWorldMonitoringPhysics> monitoringContainerPhysicsClass;
	private Class<? extends VacuumWorldPhysics> monitoredContainerPhysicsClass;
	
	public VacuumWorldMonitoringBridge(Class<? extends VacuumWorldMonitoringContainer> monitoringContainerClass, Class<? extends VacuumWorldMonitoringPhysics> monitoringContainerPhysicsClass, Class<? extends VacuumWorldSpace> monitoredContainerClass, Class<? extends VacuumWorldPhysics> monitoredContainerPhysicsClass) {
		this.monitoringContainerClass = monitoringContainerClass;
		this.monitoringContainerPhysicsClass = monitoringContainerPhysicsClass;
		this.monitoredContainerClass = monitoredContainerClass;
		this.monitoredContainerPhysicsClass = monitoredContainerPhysicsClass;
	}
	
	@Override
	public void update(CustomObservable o, Object arg) {
		if(this.monitoredContainerPhysicsClass.isAssignableFrom(o.getClass()) && DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			VacuumWorldMonitoringActionResult result = constructMonitoringResult((DefaultActionResult) arg);
			notifySuperContainerPhysics(result);
		}
		else if(this.monitoringContainerPhysicsClass.isAssignableFrom(o.getClass())) {
			VacuumWorldSpace context = (VacuumWorldSpace) getObservers().stream().filter((CustomObserver observer) -> this.monitoredContainerClass.isAssignableFrom(observer.getClass())).findAny().orElse(null);
			
			notifySubContainerPhysics(arg, context);
		}
	}
	
	private VacuumWorldMonitoringActionResult constructMonitoringResult(DefaultActionResult arg) {
		if(arg instanceof VacuumWorldMonitoringActionResult) {
			return (VacuumWorldMonitoringActionResult) arg;
		}
		else if(arg instanceof VacuumWorldActionResult) {
			VacuumWorldPerception p = ((VacuumWorldActionResult) arg).getPerception();
			VacuumWorldMonitoringPerception perception = p == null ? null : new VacuumWorldMonitoringPerception(p.getPerceivedMap(), p.getActorCoordinates());
			Exception e = ((VacuumWorldActionResult) arg).getFailureReason();
			
			return constructMonitoringResult((VacuumWorldActionResult) arg, perception, e);
		}
		else {
			return new VacuumWorldMonitoringActionResult(arg.getActionResult(), arg.getFailureReason(), arg.getRecipientsIds());
		}
	}
	
	private VacuumWorldMonitoringActionResult constructMonitoringResult(VacuumWorldActionResult r, VacuumWorldMonitoringPerception perception, Exception e) {
		if(e == null) {
			return new VacuumWorldMonitoringActionResult(r.getActionResult(), r.getActorId(), r.getRecipientsIds(), perception);
		}
		else {
			return new VacuumWorldMonitoringActionResult(r.getActionResult(), r.getActorId(), e, r.getRecipientsIds());
		}
	}

	private void notifySubContainerPhysics(Object arg, VacuumWorldSpace context) {
		notifyObservers(new Object[]{ arg, context }, this.monitoredContainerPhysicsClass);
	}
	
	private void notifySuperContainerPhysics(Object arg) {
		notifyObservers(arg, this.monitoringContainerPhysicsClass);
	}

	@Override
	public Class<? extends VacuumWorldMonitoringContainer> getMonitoringSpaceClass() {
		return this.monitoringContainerClass;
	}

	@Override
	public Class<? extends VacuumWorldSpace> getMonitoredSpaceClass() {
		return this.monitoredContainerClass;
	}

	@Override
	public Class<? extends VacuumWorldMonitoringPhysics> getMonitoringSpacePhysicsClass() {
		return this.monitoringContainerPhysicsClass;
	}

	@Override
	public Class<? extends VacuumWorldPhysics> getMonitoredSpacePhysicsClass() {
		return this.monitoredContainerPhysicsClass;
	}
}