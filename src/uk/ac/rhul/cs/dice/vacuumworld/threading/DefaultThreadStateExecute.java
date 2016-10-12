package uk.ac.rhul.cs.dice.vacuumworld.threading;

public class DefaultThreadStateExecute implements ThreadState {

	@Override
	public void run(AgentRunnable runnable) {
		runnable.getAgentMind().execute(null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}