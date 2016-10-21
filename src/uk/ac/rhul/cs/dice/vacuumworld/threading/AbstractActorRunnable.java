package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

public abstract class AbstractActorRunnable<P extends Perception> implements Runnable {
	private Mind<P> agentMind;
	private ThreadState<P> state;

	public AbstractActorRunnable(Mind<P> agentMind) {
		this.agentMind = agentMind;
	}

	public ThreadState<P> getState() {
		return this.state;
	}

	public void setState(ThreadState<P> state) {
		this.state = state;
	}

	public void setAgentMind(Mind<P> agentMind) {
		this.agentMind = agentMind;
	}

	public Mind<P> getActorMind() {
		return this.agentMind;
	}
}