package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class MoveAction extends PhysicalAction {
	private VacuumWorldCoordinates oldLocationCoordinates;
	
	public VacuumWorldCoordinates getOldLocationCoordinates() {
		return this.oldLocationCoordinates;
	}
	
	public void setOldLocationCoordinates(VacuumWorldCoordinates coordinates) {
		this.oldLocationCoordinates = coordinates;
	}
	
	@Override
	public boolean isPossible(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isPossible(this, context);
	}
	
	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).isNecessary(this, context);
	}
	
	@Override
	public Result perform(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).perform(this, context);
	}
	
	@Override
	public boolean succeeded(Physics physics, Space context) {
		return ((VacuumWorldPhysics) physics).succeeded(this, context);
	}
}