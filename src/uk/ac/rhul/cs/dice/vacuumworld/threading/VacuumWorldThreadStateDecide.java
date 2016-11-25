package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;

public class VacuumWorldThreadStateDecide extends AbstractThreadStateDecide {
    @Override
    public void run(AbstractActorRunnable runnable) {
	if (runnable instanceof VacuumWorldActorRunnable) {
	    EnvironmentalAction nextAction = runnable.getActorMind().decide();
	    Mind mind = ((VacuumWorldActorRunnable) runnable).getActorMind();
	    mind.setNextActionForExecution(nextAction);
	}
	else {
	    super.run(runnable);
	}
    }
}