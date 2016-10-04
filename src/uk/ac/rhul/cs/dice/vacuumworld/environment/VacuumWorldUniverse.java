package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.UniverseAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Body;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Universe;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class VacuumWorldUniverse extends Universe {

  public VacuumWorldUniverse(EnvironmentalSpace state, Set<Class<? extends AbstractAction>> admissibleActions, Set<Body> bodies, Physics physics, UniverseAppearance appearance) {
		super(state, admissibleActions, bodies, physics, appearance);
		
		VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.getState();
		VacuumWorldMonitoringPhysics monitoringPhysics = container.getPhysics();
		
		VacuumWorldSpace space = container.getSubContainerSpace();
		VacuumWorldPhysics vacuumWorldPhysics = (VacuumWorldPhysics) monitoringPhysics.getMonitoredContainerPhysics();
		
		makeObservers(container, monitoringPhysics, space, vacuumWorldPhysics);
	}

	private void makeObservers(VacuumWorldMonitoringContainer container, VacuumWorldMonitoringPhysics monitoringPhysics, VacuumWorldSpace space, VacuumWorldPhysics vacuumWorldPhysics) {
		space.addObserver(vacuumWorldPhysics);
		vacuumWorldPhysics.addObserver(space);
		
		container.addObserver(monitoringPhysics);
		monitoringPhysics.addObserver(container);
		
		vacuumWorldPhysics.addObserver(container);
	}
}