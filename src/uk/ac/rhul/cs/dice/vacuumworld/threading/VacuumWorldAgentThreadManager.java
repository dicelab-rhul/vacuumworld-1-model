package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringThreadStateDecide;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringThreadStateExecute;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringThreadStatePerceive;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;

public class VacuumWorldAgentThreadManager extends Observable {
	private VacuumWorldClientListener listener;
	private VacuumWorldSpace state;
	private final ThreadState threadStateDecide = new VacuumWorldThreadStateDecide();
	private final ThreadState threadStateExecute = new VacuumWorldThreadStateExecute();
	private final ThreadState threadStatePerceive = new VacuumWorldThreadStatePerceive();
	private final ThreadState monitoringThreadStateDecide = new VacuumWorldMonitoringThreadStateDecide();
	private final ThreadState monitoringThreadStateExecute = new VacuumWorldMonitoringThreadStateExecute();
	private final ThreadState monitoringThreadStatePerceive = new VacuumWorldMonitoringThreadStatePerceive();
	private boolean simulationStarted = false;
	private ExecutorService executor;
	private ExecutorService monitoringExecutor;
	private Set<VacuumWorldActorRunnable> cleaningRunnables;
	private Set<VacuumWorldMonitoringActorRunnable> monitorRunnables;
	private VacuumWorldActorRunnable userRunnable;
	
	private volatile StopSignal sharedStopSignal;
	private volatile boolean threadManagerTerminated;

	public VacuumWorldAgentThreadManager(StopSignal sharedStopSignal) {
		this.cleaningRunnables = new HashSet<>();
		this.monitorRunnables = new HashSet<>();
		this.sharedStopSignal = sharedStopSignal;
		this.threadManagerTerminated = false;
	}

	public VacuumWorldClientListener getClientListener() {
		return this.listener;
	}
	
	public boolean isTerminated() {
		return this.threadManagerTerminated;
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
	
	private void cycle(double delayInSeconds) throws InterruptedException {
		boolean doPerceive = false;
		
		while (this.simulationStarted && !this.sharedStopSignal.mustStop()) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), "START CYCLE");
			
			doCycleStep(doPerceive);

			VWUtils.logWithClass(this.getClass().getSimpleName(), "NEXT CYCLE!!\n\n");
			doPerceive = true;
			this.notifyObservers();
			
			VWUtils.doWait((int) Math.max(Math.floor(1000 * delayInSeconds), 200));
		}
	}

	public void shutdownExecutors() throws InterruptedException {
		shutdownCleaningAgentsThreadsExecutorIfNeeded();
		shutdownMonitoringingAgentsThreadsExecutorIfNeeded();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Agents threads termination complete.");
		this.threadManagerTerminated = true;
	}

	private void shutdownCleaningAgentsThreadsExecutorIfNeeded() {
		if(this.executor == null) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Cleaning agents threads executor does not exist. No termination needed.");
			
			return;
		}
		
		if(this.executor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Cleaning agents threads executor was already terminated.");
			
			return;
		}
		
		shutdownCleaningAgentsThreadsExecutor();
	}

	private void shutdownMonitoringingAgentsThreadsExecutorIfNeeded() {
		if(this.monitoringExecutor == null) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Monitoring agents threads executor does not exist. No termination needed.");
			
			return;
		}
		
		if(this.monitoringExecutor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Monitoring agents threads executor was already terminated.");
			
			return;
		}
		
		shutdownMonitoringAgentsThreadsExecutor();
	}

	private void shutdownCleaningAgentsThreadsExecutor() {
		try {
			this.executor.shutdownNow();
			this.executor.awaitTermination(5, TimeUnit.SECONDS);
			checkCleaningAgentsThreadsExecutorTermination();
		}
		catch(InterruptedException e) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Cleaning agents threads executor was not correctly terminated. A forcefully JVM termination will be needed.");
			Thread.currentThread().interrupt();
		}
	}
	
	private void checkCleaningAgentsThreadsExecutorTermination() {
		if(this.executor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Cleaning agents threads executor correctly terminated.");
		}
		else {
			VWUtils.logWithClass(getClass().getSimpleName(), "Cleaning agents threads executor was not correctly terminated. A forcefully JVM termination will be needed.");
		}
	}

	private void shutdownMonitoringAgentsThreadsExecutor() {
		try {
			this.monitoringExecutor.shutdownNow();
			this.monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS);
			checkMonitoringAgentsThreadsExecutorTermination();
		}
		catch(InterruptedException e) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Monitoring agents threads executor was not correctly terminated. A forcefully JVM termination will be needed.");
			Thread.currentThread().interrupt();
		}
	}

	private void checkMonitoringAgentsThreadsExecutorTermination() {
		if(this.executor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Monitoring agents threads executor correctly terminated.");
		}
		else {
			VWUtils.logWithClass(getClass().getSimpleName(), "Monitoring agents threads executor was not correctly terminated. A forcefully JVM termination will be needed.");
		}
	}

	protected void doCycleStep(boolean... flags) {
		boolean doPerceive = true;

		if (flags.length > 0) {
			doPerceive = flags[0];
		}

		doPerceive(this.cleaningRunnables, this.userRunnable, doPerceive);
		doMonitoringPerceive(this.monitorRunnables, doPerceive);
		doDecide(this.cleaningRunnables, this.userRunnable);
		doMonitoringDecide(this.monitorRunnables);
		doExecute(this.cleaningRunnables, this.userRunnable);
		doMonitoringExecute(this.monitorRunnables);
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	private void doDecide(Set<VacuumWorldActorRunnable> runnables, VacuumWorldActorRunnable user) {
		doPhase(this.threadStateDecide, runnables, user);
	}

	private void doExecute(Set<VacuumWorldActorRunnable>runnables, VacuumWorldActorRunnable user) {
		doPhase(this.threadStateExecute, runnables, user);
	}

	private void doPerceive(Set<VacuumWorldActorRunnable> runnables, VacuumWorldActorRunnable user, boolean doPerceive) {
		if (doPerceive) {
			doPhase(this.threadStatePerceive, runnables, user);
		}
	}
	
	private void doMonitoringDecide(Set<VacuumWorldMonitoringActorRunnable> runnables) {
		doMonitoringPhase(this.monitoringThreadStateDecide, runnables);
	}

	private void doMonitoringExecute(Set<VacuumWorldMonitoringActorRunnable>runnables) {
		doMonitoringPhase(this.monitoringThreadStateExecute, runnables);
	}

	private void doMonitoringPerceive(Set<VacuumWorldMonitoringActorRunnable> runnables, boolean doPerceive) {
		if (doPerceive) {
			doMonitoringPhase(this.monitoringThreadStatePerceive, runnables);
		}
	}

	private void doPhase(ThreadState state, Set<VacuumWorldActorRunnable> runnables, VacuumWorldActorRunnable user) {
		try {
			this.executor = Executors.newFixedThreadPool(this.cleaningRunnables.size() + (user == null ? 0 : 1));
			
			setNextPhase(state, runnables, user);
			startThreads(runnables, user);
			waitForAllThreads();
			
			this.executor.shutdown();
			this.executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void doMonitoringPhase(ThreadState state, Set<VacuumWorldMonitoringActorRunnable> runnables) {
		if(!VWUtils.isCollectionNotNullAndNotEmpty(this.monitorRunnables)) {
			return;
		}
		
		
		try {
			this.monitoringExecutor = Executors.newFixedThreadPool(this.monitorRunnables.size());
			
			setNextPhaseForMonitoringAgents(state, runnables);
			startMonitoringThreads(runnables);
			waitForAllMonitoringThreads();
			
			this.monitoringExecutor.shutdown();
			this.monitoringExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
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
	
	private void waitForAllMonitoringThreads() {
		while(((ThreadPoolExecutor) this.monitoringExecutor).getActiveCount() != 0) {
			continue;
		}
	}

	private void startThreads(Set<VacuumWorldActorRunnable> runnables, VacuumWorldActorRunnable user) {
		if(user != null) {
			this.executor.execute(user);
		}
		
		for(VacuumWorldActorRunnable runnable : runnables) {
			this.executor.execute(runnable);
		}
	}
	
	private void startMonitoringThreads(Set<VacuumWorldMonitoringActorRunnable> runnables) {
		for(VacuumWorldMonitoringActorRunnable runnable : runnables) {
			this.monitoringExecutor.execute(runnable);
		}
	}

	private void setNextPhase(ThreadState state, Set<VacuumWorldActorRunnable> runnables, VacuumWorldActorRunnable user) {
		if(user != null) {
			user.setState(state);
		}
		
		for (VacuumWorldActorRunnable runnable : runnables) {
			runnable.setState(state);
		}
	}
	
	private void setNextPhaseForMonitoringAgents(ThreadState state, Set<VacuumWorldMonitoringActorRunnable> runnables) {
		for (VacuumWorldMonitoringActorRunnable runnable : runnables) {
			runnable.setState(state);
		}
	}

	public void addActor(VacuumWorldActorRunnable runnable) {
		if (this.simulationStarted) {
			throw new IllegalThreadStateException("Cannot add a new agent at runtime.");
		}

		if (runnable.getActorMind() instanceof VacuumWorldDefaultMind) {
			this.cleaningRunnables.add(runnable);
		}
		else if(runnable.getActorMind() instanceof UserMind) {
			this.userRunnable = runnable;
		}
	}
	
	public void addMonitoringAgent(VacuumWorldMonitoringActorRunnable runnable) {
		if (this.simulationStarted) {
			throw new IllegalThreadStateException("Cannot add a new agent at runtime.");
		}

		if (runnable.getActorMind() instanceof VacuumWorldMonitoringAgentMind) {
			this.monitorRunnables.add(runnable);
		}
	}
}