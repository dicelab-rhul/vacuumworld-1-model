package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldAgentThreadExperimentManager extends VacuumWorldAgentThreadManager {

	private List<long[]> pdeTimes = new ArrayList<>();
	private int numberOfCycles;
	private String testCase;

	public VacuumWorldAgentThreadExperimentManager(int numberOfCycles) {
		this.numberOfCycles = numberOfCycles;
	}

	public void clear() {
		this.activeThreads.clear();
		this.cleaningRunnables.clear();
		this.monitorRunnables.clear();
		this.pdeTimes.clear();
		this.testCase = null;
		this.simulationStarted = false;
	}

	@Override
	protected void cycle(double delayInSeconds) {
		Utils.logWithClass(this.getClass().getSimpleName(), "Start: " + this.testCase + " experiment...");
		Utils.doWait(Math.max((int) (delayInSeconds * 1000), 200));
		
		cycleHelper();
		log();
	}

	private void log() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.testCase + " ");
		
		for (int j = 0; j < this.pdeTimes.size(); j++) {
			long[] v = this.pdeTimes.get(j);
			builder.append((v[0] + v[1] + v[2]) + " ");
		}
		
		Logger log = Utils.fileLogger("logs/test/results.txt", true);
		log.info(builder.toString());
		
		closeHandlers(log);
	}

	private void closeHandlers(Logger log) {
		Handler[] h = log.getHandlers();
		
		for (int k = 0; k < h.length; k++) {
			h[k].close();
		}
		Utils.logWithClass(this.getClass().getSimpleName(), "Experiment done.");
	}

	private void cycleHelper() {
		boolean doPerceive = false;
		
		for(int i=0; i < this.numberOfCycles; i++) {
			doCycleStep(this.monitorRunnables);
			super.doCycleStep(this.cleaningRunnables, doPerceive);
			doPerceive = true;
		}
	}

	@Override
	protected void doCycleStep(Set<AgentRunnable> agentsRunnables, boolean... flags) {
		long p = timePerceive(agentsRunnables);
		long d = timeDecide(agentsRunnables);
		long e = timeExecute(agentsRunnables);
		
		this.pdeTimes.add(new long[] { p, d, e });
	}

	private long timeDecide(Set<AgentRunnable> agentsRunnables) {
		Long l = System.nanoTime();
		doDecide(agentsRunnables);
		
		return System.nanoTime() - l;
	}

	private long timePerceive(Set<AgentRunnable> agentsRunnables) {
		Long l = System.nanoTime();
		doPerceive(agentsRunnables, true);
	
		return System.nanoTime() - l;
	}

	private long timeExecute(Set<AgentRunnable> agentsRunnables) {
		Long l = System.nanoTime();
		doExecute(agentsRunnables);
		
		return System.nanoTime() - l;
	}

	public String getTestCase() {
		return testCase;
	}

	public void setTestCase(String testCase) {
		this.testCase = testCase;
	}
}