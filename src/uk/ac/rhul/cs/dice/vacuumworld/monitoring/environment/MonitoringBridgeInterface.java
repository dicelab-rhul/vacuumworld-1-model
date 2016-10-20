package uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;

public interface MonitoringBridgeInterface<M, P1, S, P2> extends CustomObserver {
	public abstract Class<? extends M> getMonitoringSpaceClass();
	public abstract Class<? extends S> getMonitoredSpaceClass();
	public abstract Class<? extends P1> getMonitoringSpacePhysicsClass();
	public abstract Class<? extends P2> getMonitoredSpacePhysicsClass();
}