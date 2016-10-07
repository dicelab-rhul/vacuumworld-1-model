package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorMind;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldAgentThreadManager extends Observable {
	private VacuumWorldClientListener listener;
	private VacuumWorldSpace state;
	protected final ThreadStateDecide threadStateDecide = new ThreadStateDecide();
	protected final ThreadStateExecute threadStateExecute = new ThreadStateExecute();
	protected final ThreadStatePerceive threadStatePerceive = new ThreadStatePerceive();
	protected boolean simulationStarted = false;
	protected Set<Thread> activeThreads;
	protected Set<AgentRunnable> cleaningRunnables;
	protected Set<AgentRunnable> monitorRunnables;

	public VacuumWorldAgentThreadManager() {
		this.activeThreads = new HashSet<>();
		this.cleaningRunnables = new HashSet<>();
		this.monitorRunnables = new HashSet<>();
	}

	public VacuumWorldClientListener getClientListener() {
		return this.listener;
	}
	
	public VacuumWorldSpace getState() {
		return this.state;
	}
	
	public void setClientListener(VacuumWorldClientListener listener, VacuumWorldSpace state) {
		this.listener = listener;
		this.state = state;
	}
	
	public void start(double delayInSeconds) {
		this.simulationStarted = true;
		
		cycle(delayInSeconds);
	}

	protected void cycle(double delayInSeconds) {
		boolean doPerceive = false;
		
		while (this.simulationStarted) {
			Utils.logWithClass(this.getClass().getSimpleName(), "\n\nSTART CYCLE");
			doCycleStep(this.monitorRunnables);
			doCycleStep(this.cleaningRunnables, doPerceive);

			Utils.logWithClass(this.getClass().getSimpleName(), "NEXT CYCLE!!\n\n");
			doPerceive = true;
			this.notifyObservers();
			
			Utils.doWait((int) Math.max(Math.floor(1000 * delayInSeconds), 200));
		}
	}

	protected void doCycleStep(Set<AgentRunnable> agentsRunnables, boolean... flags) {
		boolean doPerceive = true;

		if (flags.length > 0) {
			doPerceive = flags[0];
		}

		doPerceive(agentsRunnables, doPerceive);
		doDecide(agentsRunnables);
		doExecute(agentsRunnables);
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	protected void doDecide(Set<AgentRunnable> runnables) {
		doPhase(this.threadStateDecide, runnables);
	}

	protected void doExecute(Set<AgentRunnable> runnables) {
		doPhase(this.threadStateExecute, runnables);
	}

	protected void doPerceive(Set<AgentRunnable> runnables, boolean doPerceive) {
		if (doPerceive) {
			doPhase(this.threadStatePerceive, runnables);
		}
	}

	protected void doPhase(ThreadState state, Set<AgentRunnable> runnables) {
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
		for (AgentRunnable runnable : runnables) {
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
			this.monitorRunnables.add(runnable);
		}
		else {
			this.cleaningRunnables.add(runnable);
		}
	}
}