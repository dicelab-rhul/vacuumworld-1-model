package uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AbstractActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AbstractThreadStateDecide;

public class VacuumWorldMonitoringThreadStateDecide extends AbstractThreadStateDecide {
    @Override
    public void run(AbstractActorRunnable runnable) {
	if (runnable instanceof VacuumWorldMonitoringActorRunnable) {
	    EnvironmentalAction nextAction = runnable.getActorMind().decide();
	    Mind mind = runnable.getActorMind();
	    mind.setNextActionForExecution(nextAction);
	}
	else {
	    super.run(runnable);
	}
    }
}