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
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;

/**
 * A Vacuum World implementation of {@link ObserverMind}. Contains Vacuum World
 * Specific functionality including {@link Thread} related functionality. This
 * {@link Mind} is set up to handle {@link MonitoringResult MonitoringResults}
 * from {@link VWObserverBrain}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverMind extends ObserverMind {

  private Set<Action> actions;
  private Action nextAction;

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
  public Action decide(Object... parameters) {
    nextAction = (Action) actions.toArray()[0];
    return nextAction;
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
    if (perceptionWrapper instanceof MonitoringResult) {
      Perception perception = ((MonitoringResult) perceptionWrapper)
          .getPerception();
      this.storePerception(perception);
    }
  }

  @Override
  public void execute(Action action) {
    notifyObservers(nextAction, VWObserverBrain.class);
  }

  @Override
  protected void manageWriteResult(WriteResult result) {
    if (result.getActionResult().equals(ActionResult.ACTION_DONE)) {
      return;
    } else {
      Logger.getGlobal().log(Level.SEVERE, "WRITE RESULT FAILED ",
          result.getFailureReason());
    }
  }
  
  public void setAvailableActions(Set<Action> actions) {
    this.actions = actions;
  }
}
