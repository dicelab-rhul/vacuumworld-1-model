package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

public abstract class AbstractThreadStatePerceive<P extends Perception> implements ThreadState<P> {

	@Override
	public void run(AbstractActorRunnable<P> runnable) {
		runnable.getActorMind().perceive(null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}