package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public abstract class AbstractThreadStateDecide implements ThreadState {

    @Override
    public void run(AbstractActorRunnable runnable) {
	EnvironmentalAction action = runnable.getActorMind().decide();
	runnable.getActorMind().setNextActionForExecution(action);
    }

    @Override
    public String toString() {
	return this.getClass().getSimpleName();
    }
}