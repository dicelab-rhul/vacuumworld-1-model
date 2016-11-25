package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;

public class VacuumWorldActorRunnable extends AbstractActorRunnable {

    public VacuumWorldActorRunnable(Mind mind) {
	super(mind);
    }

    @Override
    public void run() {
	super.getState().run(this);
    }
}