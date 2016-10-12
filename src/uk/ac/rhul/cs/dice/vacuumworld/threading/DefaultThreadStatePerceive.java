package uk.ac.rhul.cs.dice.vacuumworld.threading;

public class DefaultThreadStatePerceive implements ThreadState {

	@Override
	public void run(AgentRunnable runnable) {
		runnable.getAgentMind().perceive(null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}