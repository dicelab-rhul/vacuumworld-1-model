package uk.ac.rhul.cs.dice.vacuumworld.threading;

public abstract class AbstractThreadStatePerceive implements ThreadState {

	@Override
	public void run(AbstractActorRunnable runnable) {
		runnable.getActorMind().perceive(null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}