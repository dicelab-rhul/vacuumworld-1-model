package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldActorRunnable extends AbstractActorRunnable<VacuumWorldPerception> {

	public VacuumWorldActorRunnable(Mind<VacuumWorldPerception> mind) {
		super(mind);
	}
	
	@Override
	public void run() {
		super.getState().run(this);
	}
}