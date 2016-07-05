package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverMind;
import uk.ac.rhul.cs.dice.monitor.common.PerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

/**
 * A Vacuum World implementation of {@link ObserverMind}. Contains Vacuum World
 * Specific functionality including {@link Thread} related functionality. This
 * {@link Mind} is set up to handle {@link MonitoringResult MonitoringResults}
 * from {@link VWObserverBrain}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverMind extends ObserverMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private final Action perceiveAction = new TotalPerceptionAction();
  private boolean canProceed = false;

  /**
   * Constructor.
   * 
   * @param refiner
   *          for refining {@link Perceptions}. Note that this should be
   *          {@link DefaultPerceptionRefiner} as (unless for a good reason) the
   *          perception should not be refined further. Only parse a value other
   *          than {@link DefaultPerceptionRefiner} if you understand the
   *          implications; having create a new {@link MongoBridge} etc.
   */
  public VWObserverMind(PerceptionRefiner refiner) {
    super(refiner);
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    if (o instanceof VWObserverBrain) {
      perceive(arg);
    }
  }

  /**
   * Starts the perceive process, which for an {@link ObserverAgent} involves
   * sending the perception to a database.
   */
  @Override
  public void perceive(Object perceptionWrapper) {
   //System.out.println(this.getClass().getSimpleName() + Thread.currentThread().getId() + " perceive");
    if (perceptionWrapper instanceof MonitoringResult) {
      Perception perception = ((MonitoringResult) perceptionWrapper)
          .getPerception();
      this.storePerception(perception);
    }
  }

  @Override
  public void execute(Action action) {
    notifyObservers(action, VWObserverBrain.class);
  }

  @Override
  protected void manageWriteResult(WriteResult result) {
    if (result.getActionResult().equals(ActionResult.ACTION_DONE)) {
      this.setState(ThreadState.AFTER_PERCEIVE);
      return;
    } else {
      Logger.getGlobal().log(Level.SEVERE, "WRITE RESULT FAILED ",
          result.getFailureReason());
    }
  }

  // ******* THREAD RELATED METHODS *******//

  @Override
  public ThreadState getState() {
    return this.state;
  }

  @Override
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
    canProceed = false;
    this.state = ThreadState.JUST_STARTED;
    this.state = ThreadState.AFTER_DECIDE;

    waitForServerBeforeExecution();
    execute(perceiveAction);
  }

  @Override
  public void waitForServerBeforeExecution() {
    while (!this.canProceed)
      ;
  }

  @Override
  public void resume() {
    this.canProceed = true;
  }

  public void setState(ThreadState state) {
    this.state = state;
  }

  // ******* NOT USED METHODS *******//

  /**
   * Not used, start is called via
   * {@link VacuumWorldMindInterface#start(int, boolean, Set)}.
   */
  @Override
  public void start() {
  }

  /**
   * Not used. An {@link VWObserverMind} makes no decisions. Its only actions
   * are to perceive.
   */
  @Override
  public Action decide(Object... parameters) {
    return null;
  }

  /**
   * Not used. Cycling is done via
   * {@link VacuumWorldMindInterface#start(int, boolean, Set)}.
   */
  @Override
  protected void stepCycle(Object actionResultWrapper) {
  }

@Override
public boolean canProceed() {
	return this.canProceed;
}
}
