package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;

public abstract class ActorRunnable implements Runnable {
	private Mind agent;
	private ThreadState state;

	public ActorRunnable(Mind agent) {
		this.agent = agent;
	}

	@Override
	public void run() {
		this.state.run(this);
	}

	public ThreadState getState() {
		return this.state;
	}

	public void setState(ThreadState state) {
		this.state = state;
	}

	public void setAgentMind(Mind agent) {
		this.agent = agent;
	}

	public Mind getActorMind() {
		return this.agent;
	}
}