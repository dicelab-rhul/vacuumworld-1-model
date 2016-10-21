package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.UniverseAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Universe;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;

public class VacuumWorldUniverse extends Universe<VacuumWorldPerception> {
	private VacuumWorldMonitoringBridge monitoringBridge;
	private VacuumWorldMonitoringContainer monitoringContainer;
	private VacuumWorldMonitoringPhysics monitoringPhysics;

	public VacuumWorldUniverse(EnvironmentalSpace state, VacuumWorldPhysics physics, VacuumWorldMonitoringContainer monitoringContainer, VacuumWorldMonitoringPhysics monitoringPhysics, UniverseAppearance appearance) {
		super(state, physics, appearance);
		
		VacuumWorldSpace space = (VacuumWorldSpace) state;
		
		this.monitoringContainer = monitoringContainer;
		this.monitoringPhysics = monitoringPhysics;
		this.monitoringBridge = new VacuumWorldMonitoringBridge(this.monitoringContainer.getClass(), this.monitoringPhysics.getClass(), space.getClass(), physics.getClass());
	
		makeObservers(space, physics);
	}

	private void makeObservers(VacuumWorldSpace monitoredSpace, VacuumWorldPhysics monitoredPhysics) {
		monitoredSpace.addObserver(monitoredPhysics);
		monitoredPhysics.addObserver(monitoredSpace);
		
		this.monitoringBridge.addObserver(this.monitoringPhysics);
		this.monitoringPhysics.addObserver(this.monitoringBridge);
		this.monitoringBridge.addObserver(monitoredPhysics);
		monitoredPhysics.addObserver(this.monitoringBridge);
		
		this.monitoringContainer.addObserver(this.monitoringPhysics);
		this.monitoringPhysics.addObserver(this.monitoringContainer);
	}

	public VacuumWorldMonitoringBridge getMonitoringBridge() {
		return this.monitoringBridge;
	}

	public VacuumWorldMonitoringContainer getMonitoringContainer() {
		return this.monitoringContainer;
	}

	public VacuumWorldMonitoringPhysics getMonitoringPhysics() {
		return this.monitoringPhysics;
	}
	
	@Override
	public VacuumWorldSpace getState() {
		return (VacuumWorldSpace) super.getState();
	}
	
	@Override
	public VacuumWorldPhysics getPhysics() {
		return (VacuumWorldPhysics) super.getPhysics();
	}
}