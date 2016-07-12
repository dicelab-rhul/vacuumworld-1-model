package uk.ac.rhul.cs.dice.vacuumworld.threading;

public class ThreadStatePerceive implements ThreadState {

  @Override
  public void run(AgentRunnable runnable) {
    runnable.getAgent().perceive(null);
  }
}
