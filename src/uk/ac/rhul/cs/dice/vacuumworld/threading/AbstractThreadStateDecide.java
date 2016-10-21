package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

public abstract class AbstractThreadStateDecide<P extends Perception> implements ThreadState<P> {

	@Override
	public void run(AbstractActorRunnable<P> runnable) {
		EnvironmentalAction<P> action = runnable.getActorMind().decide();
		runnable.getActorMind().setNextActionForExecution(action);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}