package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

@FunctionalInterface
public interface ThreadState<P extends Perception> {
	public void run(AbstractActorRunnable<P> runnable);
}