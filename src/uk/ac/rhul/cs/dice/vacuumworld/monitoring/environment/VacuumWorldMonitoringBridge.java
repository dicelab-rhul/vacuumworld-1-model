package uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;

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
		if(this.monitoredContainerPhysicsClass.isAssignableFrom(o.getClass()) && arg instanceof VacuumWorldMonitoringActionResult) {
			notifySuperContainerPhysics((VacuumWorldMonitoringActionResult) arg);
		}
		else if(this.monitoringContainerPhysicsClass.isAssignableFrom(o.getClass()) && arg instanceof VacuumWorldEvent) {
			VacuumWorldSpace context = (VacuumWorldSpace) getObservers().stream().filter((CustomObserver observer) -> this.monitoredContainerClass.isAssignableFrom(observer.getClass())).findAny().orElse(null);
			
			notifySubContainerPhysics((VacuumWorldEvent) arg, context);
		}
	}

	private void notifySubContainerPhysics(VacuumWorldEvent event, VacuumWorldSpace context) {
		notifyObservers(new VWPair<VacuumWorldEvent, VacuumWorldSpace>(event, context), this.monitoredContainerPhysicsClass);
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