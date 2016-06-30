package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;

/**
 * Used to build a bridge between different kinds of agents that exist in Vacuum
 * World. All the methods in this interface should be implemented by agents of
 * Vacuum World as they will be need when initialising the universe and for
 * thread management. </br> known direct sub class:
 * {@link VacuumWorldThreadedMind}.
 * 
 * @author cloudstrife9999 a.k.a. Emanuele Uliana
 * @author Ben Wilkins
 * @author Kostas Stathis
 *
 */
public interface VacuumWorldMindInterface {

  /**
   * Getter for the {@link ThreadState} of the agent.
   * 
   * @return the {@link ThreadState}
   */
  public ThreadState getState();

  /**
   * Allows an agent to proceed.
   */
  public void resume();

  /**
   * Starts or restarts the agents thread cycle. The parameters are only need
   * for the first execution.
   * 
   * @param perceptionRange
   * @param canSeeBehind
   * @param availableActions
   */
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions);

  /**
   * Wait for the server.
   */
  public void waitForServerBeforeExecution();
}
