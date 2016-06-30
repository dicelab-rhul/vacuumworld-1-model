package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverMind;
import uk.ac.rhul.cs.dice.monitor.common.PerceptionRefiner;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

public class VWObserverMind extends ObserverMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private final Action perceiveAction = new TotalPerceptionAction();
  private boolean canProceed = false;

  public VWObserverMind(PerceptionRefiner refiner, Class<?> brainObserver) {
    super(refiner, brainObserver);
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    if(o instanceof VWObserverBrain && arg instanceof MonitoringResult) {
        perceive(arg);
    }
  }
  
  @Override
  public void perceive(Object perceptionWrapper) {
    System.out.println("PERCEIVING!");
    if(perceptionWrapper instanceof MonitoringResult) {
      //get the perception and refine it
      Perception perception = ((MonitoringResult) perceptionWrapper).getPerception();
      this.storePerception(perception);
    }
  }

  @Override
  public void execute(Action action) {
    notifyObservers(action, VWObserverBrain.class);
  }
 
  @Override
  protected void manageWriteResult(WriteResult result) {
    if(result.getActionResult().equals(ActionResult.ACTION_DONE)){
      this.setState(ThreadState.AFTER_PERCEIVE);
      return;
    } else {
      System.out.println("WRITE ACTION FAILED");
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
    this.state = ThreadState.JUST_STARTED;
    this.state = ThreadState.AFTER_DECIDE;

    waitForServerBeforeExecution();
    execute(perceiveAction);
  }

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
    // Unused
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
}
