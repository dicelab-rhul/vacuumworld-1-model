package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;

public abstract class ActorRunnable implements Runnable {
	private Mind agentMind;
	private ThreadState state;

	public ActorRunnable(Mind agentMind) {
		this.agentMind = agentMind;
	}

	public ThreadState getState() {
		return this.state;
	}

	public void setState(ThreadState state) {
		this.state = state;
	}

	public void setAgentMind(Mind agentMind) {
		this.agentMind = agentMind;
	}

	public Mind getActorMind() {
		return this.agentMind;
	}
}