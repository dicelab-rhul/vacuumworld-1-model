package uk.ac.rhul.cs.dice.vacuumworld.threading;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorMind;
import util.Utils;

public class VacuumWorldAgentThreadManager extends Observable {
  private final ThreadStateDecide threadStateDecide = new ThreadStateDecide();
  private final ThreadStateExecute threadStateExecute = new ThreadStateExecute();

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
    while (this.simulationStarted) {
      doDecide(this.cleaningRunnables);
      Utils.log("Cleaning decide phase complete");
      doExecute(this.cleaningRunnables);
      Utils.log("Cleaning execute phase complete");

      doDecide(this.monitorRunnables);
      Utils.log("Monitoring deciding phase complete");
      doExecute(this.monitorRunnables);
      Utils.log("Monitoring execute phase complete");

      // next cycle!
      Utils.log("NEXT CYCLE!! \n \n \n \n");
      this.notifyObservers();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void notifyObservers() {
    setChanged();
    super.notifyObservers();
  }

  public void doDecide(Set<AgentRunnable> runnables) {
    doPhase(this.threadStateDecide, runnables);
  }

  private void doExecute(Set<AgentRunnable> runnables) {
    doPhase(this.threadStateExecute, runnables);
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
    Iterator<AgentRunnable> iter = runnables.iterator();
    while (iter.hasNext()) {
      iter.next().setState(state);
    }
  }

  private void buildThreads(Set<AgentRunnable> runnables) {
    this.activeThreads.clear();
    for (AgentRunnable a : runnables) {
      this.activeThreads.add(new Thread(a));
    }
  }

  private void waitForAllThreads() {
    Utils.log("Manager waiting...");

    while (checkAlive()) {
      continue;
    }

    Utils.log("... threads finished!");
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
      throw new IllegalThreadStateException(
          "Cannot add a new agent at runtime.");
    }
    if (runnable.getAgent() instanceof DatabaseAgentMind
        || runnable.getAgent() instanceof VacuumWorldMonitorMind) {
      System.out.println("Adding monitoring runnable");
      monitorRunnables.add(runnable);
    } else {
      System.out.println("Adding cleaning runnable");
      cleaningRunnables.add(runnable);
    }
  }
}