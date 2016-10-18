package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor.VacuumWorldMonitorMind;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;

public class VacuumWorldAgentThreadManager extends Observable {
	private VacuumWorldClientListener listener;
	private VacuumWorldSpace state;
	protected final ThreadState threadStateDecide = new VacuumWorldThreadStateDecide();
	protected final ThreadState threadStateExecute = new DefaultThreadStateExecute();
	protected final ThreadState threadStatePerceive = new DefaultThreadStatePerceive();
	protected boolean simulationStarted = false;
	protected ExecutorService executor;
	protected Set<ActorRunnable> cleaningRunnables;
	protected Set<ActorRunnable> monitorRunnables;
	
	private volatile StopSignal sharedStopSignal;

	public VacuumWorldAgentThreadManager(StopSignal sharedStopSignal) {
		this.cleaningRunnables = new HashSet<>();
		this.monitorRunnables = new HashSet<>();
		this.sharedStopSignal = sharedStopSignal;
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
	
	public void start(double delayInSeconds) throws InterruptedException {
		this.simulationStarted = true;
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Thread manager correctly started.");
		
		cycle(delayInSeconds);
	}

	//monitoring is disabled
	protected void cycle(double delayInSeconds) throws InterruptedException {
		boolean doPerceive = false;
		
		while (this.simulationStarted && !this.sharedStopSignal.mustStop()) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), "START CYCLE");
			
			doCycleStep(this.cleaningRunnables, doPerceive);

			VWUtils.logWithClass(this.getClass().getSimpleName(), "NEXT CYCLE!!\n\n");
			doPerceive = true;
			this.notifyObservers();
			
			VWUtils.doWait((int) Math.max(Math.floor(1000 * delayInSeconds), 200));
		}
		
		this.executor.shutdownNow();
		this.executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Agents threads termination complete.");
	}

	protected void doCycleStep(Set<ActorRunnable> agentsRunnables, boolean... flags) {
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

	protected void doDecide(Set<ActorRunnable> runnables) {
		doPhase(this.threadStateDecide, runnables);
	}

	protected void doExecute(Set<ActorRunnable> runnables) {
		doPhase(this.threadStateExecute, runnables);
	}

	protected void doPerceive(Set<ActorRunnable> runnables, boolean doPerceive) {
		if (doPerceive) {
			doPhase(this.threadStatePerceive, runnables);
		}
	}

	protected void doPhase(ThreadState state, Set<ActorRunnable> runnables) {
		try {
			this.executor = Executors.newFixedThreadPool(this.cleaningRunnables.size());
			
			setNextPhase(state, runnables);
			startThreads(runnables);
			waitForAllThreads();
			
			this.executor.shutdown();
			this.executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void waitForAllThreads() {
		while(((ThreadPoolExecutor) this.executor).getActiveCount() != 0) {
			continue;
		}
	}

	private void startThreads(Set<ActorRunnable> runnables) {
		for(ActorRunnable runnable : runnables) {
			this.executor.execute(runnable);
		}
	}

	private void setNextPhase(ThreadState state, Set<ActorRunnable> runnables) {
		for (ActorRunnable runnable : runnables) {
			runnable.setState(state);
		}
	}

	public void addActor(ActorRunnable runnable) {
		if (this.simulationStarted) {
			throw new IllegalThreadStateException("Cannot add a new agent at runtime.");
		}

		if (runnable.getActorMind() instanceof DatabaseAgentMind || runnable.getActorMind() instanceof VacuumWorldMonitorMind) {
			this.monitorRunnables.add(runnable);
		}
		else {
			this.cleaningRunnables.add(runnable);
		}
	}
}