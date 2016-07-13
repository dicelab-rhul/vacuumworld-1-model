package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorMind;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldAgentThreadManager extends Observable {
	private final ThreadStateDecide threadStateDecide = new ThreadStateDecide();
	private final ThreadStateExecute threadStateExecute = new ThreadStateExecute();
	private final ThreadStatePerceive threadStatePerceive = new ThreadStatePerceive();

	private boolean simulationStarted = false;

	private Set<Thread> activeThreads;

	private Set<AgentRunnable> cleaningRunnables;
	private Set<AgentRunnable> monitorRunnables;

	public VacuumWorldAgentThreadManager() {
		this.activeThreads = new HashSet<>();
		this.cleaningRunnables = new HashSet<>();
		this.monitorRunnables = new HashSet<>();
	}

	public void start() {
		this.simulationStarted = true;
		Utils.log("START");
		doCycleStep(this.monitorRunnables); // let monitors get the initial state
		cycle();
	}

	private void cycle() {

		while (this.simulationStarted) {
			Utils.log("START CYCLE");
			doCycleStep(this.cleaningRunnables);
			doCycleStep(this.monitorRunnables);
			// next cycle!
			Utils.log("NEXT CYCLE!! \n \n \n \n");
			this.notifyObservers();
			
			Utils.doWait(1000);
		}
	}
	
	private void doCycleStep(Set<AgentRunnable> agentsRunnables) {
		doPerceive(agentsRunnables);
		doDecide(agentsRunnables);
		doExecute(agentsRunnables);
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	private void doDecide(Set<AgentRunnable> runnables) {
		doPhase(this.threadStateDecide, runnables);
	}

	private void doExecute(Set<AgentRunnable> runnables) {
		doPhase(this.threadStateExecute, runnables);
	}

	private void doPerceive(Set<AgentRunnable> runnables) {
		doPhase(this.threadStatePerceive, runnables);
	}

	private void doPhase(ThreadState state, Set<AgentRunnable> runnables) {
		buildThreads(runnables);
		setNextPhase(state, runnables);
		startAllThreads();
		waitForAllThreads();
	}

	private void startAllThreads() {
		for (Thread t : this.activeThreads) {
			t.start();
		}
	}

	private void setNextPhase(ThreadState state, Set<AgentRunnable> runnables) {
		for(AgentRunnable runnable : runnables) {
			runnable.setState(state);
		}
	}

	private void buildThreads(Set<AgentRunnable> runnables) {
		this.activeThreads.clear();
		
		for (AgentRunnable a : runnables) {
			this.activeThreads.add(new Thread(a));
		}
	}

	private void waitForAllThreads() {
		while (checkAlive()) {
			continue;
		}
	}

	private boolean checkAlive() {
		for (Thread t : this.activeThreads) {
			if (t.isAlive()) {
				return true;
			}
		}

		return false;
	}

	public void addAgent(AgentRunnable runnable) {
		if (this.simulationStarted) {
			throw new IllegalThreadStateException("Cannot add a new agent at runtime.");
		}
		
		if (runnable.getAgent() instanceof DatabaseAgentMind || runnable.getAgent() instanceof VacuumWorldMonitorMind) {
			Utils.log("Adding monitoring runnable");
			this.monitorRunnables.add(runnable);
		}
		else {
			Utils.log("Adding cleaning runnable");
			this.cleaningRunnables.add(runnable);
		}
	}
}