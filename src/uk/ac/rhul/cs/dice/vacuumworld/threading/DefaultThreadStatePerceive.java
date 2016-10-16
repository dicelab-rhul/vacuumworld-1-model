package uk.ac.rhul.cs.dice.vacuumworld.threading;

public class DefaultThreadStatePerceive implements ThreadState {

	@Override
	public void run(ActorRunnable runnable) {
		runnable.getActorMind().perceive(null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}