package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Universe;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;

public class VacuumWorldUniverse extends Universe {
    private VacuumWorldMonitoringBridge monitoringBridge;
    private VacuumWorldMonitoringContainer monitoringContainer;
    private VacuumWorldMonitoringPhysics monitoringPhysics;

    public VacuumWorldUniverse(VacuumWorldSpace state, VacuumWorldPhysics physics, VacuumWorldMonitoringContainer monitoringContainer, VacuumWorldMonitoringPhysics monitoringPhysics, VacuumWorldAppearance appearance) {
	super(state, physics, appearance);

	this.monitoringContainer = monitoringContainer;
	this.monitoringPhysics = monitoringPhysics;
	this.monitoringBridge = new VacuumWorldMonitoringBridge(this.monitoringContainer.getClass(), this.monitoringPhysics.getClass(), state.getClass(), physics.getClass());

	makeObservers(state, physics);
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

    @Override
    public VacuumWorldAppearance getAppearance() {
	return (VacuumWorldAppearance) super.getAppearance();
    }
}