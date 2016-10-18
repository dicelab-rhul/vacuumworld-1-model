package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;

public abstract class DefaultThreadStateDecide implements ThreadState {

	@Override
	public void run(ActorRunnable runnable) {
		EnvironmentalAction action = runnable.getActorMind().decide();
		((VacuumWorldDefaultMind) runnable.getActorMind()).setNextActionForExecution(action);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}