package uk.ac.rhul.cs.dice.vacuumworld.threading;

public class DefaultThreadStateDecide implements ThreadState {

	@Override
	public void run(ActorRunnable runnable) {
		runnable.getActorMind().decide();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}