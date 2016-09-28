package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.JsonForControllerBuilder;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorMind;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.view.ModelUpdate;
import uk.ac.rhul.cs.dice.vacuumworld.view.ViewRequestsEnum;

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

	public void setClientListener(VacuumWorldClientListener listener, VacuumWorldSpace state) {
		this.listener = listener;
		this.state = state;
	}
	
	public void start() {
		this.simulationStarted = true;
		// Utils.log("START");
		cycle();
	}

	protected void cycle() {
		boolean doPerceive = false;
		
		while (this.simulationStarted) {
			Utils.log("START CYCLE");
			doCycleStep(this.monitorRunnables);
			doCycleStep(this.cleaningRunnables, doPerceive);

			Utils.log("NEXT CYCLE!! \n \n \n \n");
			doPerceive = true;
			this.notifyObservers();

			listenForClientCommand();
			
			Utils.doWait(1000);
		}
	}

	private void listenForClientCommand() {
		if(this.listener == null) {
			System.out.println("listener is null.");
		}
		
		ViewRequestsEnum code = this.listener.getRequestCode();
		
		if(code != null) {
			listenForClientCommand(code);
		}
	}

	private void listenForClientCommand(ViewRequestsEnum code) {
		switch(code) {
		case GET_STATE:
			sendModelUpdate();
			break;
		case STOP:
			stopServer();
			break;
		default:
			return;
		}
	}

	private void stopServer() {
		System.exit(0);
	}

	private void sendModelUpdate() {
		ModelUpdate update = JsonForControllerBuilder.createModelUpdate(this.state);
		this.listener.setUpdateToSend(update);
		this.listener.unlock();
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