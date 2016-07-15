package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldAgentThreadExperimentManager extends
    VacuumWorldAgentThreadManager {

  private ArrayList<long[]> pdeTimes = new ArrayList<>();
  private int numberOfCycles;
  private String testCase;
  
  public VacuumWorldAgentThreadExperimentManager(int numberOfCycles) {
    this.numberOfCycles = numberOfCycles;
  }
  
  public void clear() {
    this.activeThreads.clear();
    this.cleaningRunnables.clear();
    this.monitorRunnables.clear();
    pdeTimes.clear();
    testCase = null;
    this.simulationStarted = false;
  }

  @Override
  protected void cycle() {
    System.out.println("Start: " + testCase + " experiment...");
    boolean doPerceive = false;
    int i = 0;
    while (i < numberOfCycles) {
      doCycleStep(monitorRunnables);
      super.doCycleStep(this.cleaningRunnables, doPerceive);
      doPerceive = true;
      i++;
    }
    
    StringBuilder builder = new StringBuilder();
    builder.append(testCase + "\n");
    for(int j = 0; j < pdeTimes.size(); j++) {
      long[] v = pdeTimes.get(j);
      builder.append("P:" + v[0] + " D:" + v[1] + " E:" + v[2] + "\n");
      System.out.println("P:" + v[0] + " D:" + v[1] + " E:" + v[2]);
    }
    Logger log = Utils.fileLogger("logs/test/results.txt", true);
    log.info(builder.toString());
    //close file handlers
    Handler h[] = log.getHandlers();
    for(int k = 0; k < h.length; k++) {
      h[k].close();
    }
    System.out.println("Complete");
  }

  @Override
  protected void doCycleStep(Set<AgentRunnable> agentsRunnables,
      boolean... flags) {
    long p = timePerceive(agentsRunnables);
    long d = timeDecide(agentsRunnables);
    long e = timeExecute(agentsRunnables);
    pdeTimes.add(new long[] { p, d, e });
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
