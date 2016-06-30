package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

public class AgentRunnable implements Runnable {
	private AbstractAgent agent;
	private Set<Action> availableActions;
	private Random rng;
	private final long id;
	
	public AgentRunnable(AbstractAgent agent, Set<Action> availableActions) {
		this.agent = agent;
		this.availableActions = new HashSet<>(availableActions);
		this.rng = new Random();
		this.id = this.rng.nextLong();
	}
	
	public ThreadState getThreadState() {
		return ((VacuumWorldMindInterface) this.agent.getMind()).getState();
	}
	
	public AbstractAgent getAgent() {
		return this.agent;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public void resumeAgent() {
		((VacuumWorldMindInterface) this.agent.getMind()).resume();
	}
	
	@Override
	public void run() {
	  VacuumWorldAgentInterface a = (VacuumWorldAgentInterface)this.agent;
		((VacuumWorldMindInterface) this.agent.getMind()).start(a.getPerceptionRange(), a.canSeeBehind(), this.availableActions);
	}
}