package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadAction;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

public class VWEvaluatorMind extends EvaluatorMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private boolean canProceed = false;

  private EvaluationStrategy<?> strategy;
  private int changes = 0;

  /**
   * Constructor.
   * 
   * @param strategy
   *          that will be used for evaluation.
   */
  public VWEvaluatorMind(EvaluationStrategy<?> strategy) {
    this.strategy = strategy;
    canProceed = false;
  }

  @Override
  public void execute(Action action) {
    if (shouldRead()) {
      Logger.getGlobal().log(Level.INFO, "Evaluator reading...");
      read();
      
    } else {
      Logger.getGlobal().log(Level.INFO, "Evaluator did not read");
      this.state = ThreadState.AFTER_PERCEIVE;
    }
  }

  @Override
  protected void manageReadResult(ReadResult result) {
    perceive(result);
    Evaluation e = this.evaluate();
    System.out.println("AN EVALUATION WAS MADE!");
    this.state = ThreadState.AFTER_PERCEIVE;
    return;
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    if (perceptionWrapper instanceof ReadResult) {
      ReadResult result = (ReadResult) perceptionWrapper;
      RefinedPerception[] ps = (RefinedPerception[]) result.getPerceptions()
          .toArray(new RefinedPerception[] {});
      strategy.update(ps[0]);
    }
  }

  @Override
  public Action decide(Object... parameters) {
    return null;
  }

  @Override
  protected void manageChangedResult(ChangedResult result) {
    this.changes++;
  }

  @Override
  protected boolean shouldRead() {
    return changes % 3 == 0 && changes != 0;
  }

  @Override
  protected boolean shouldEvaluate() {
    return shouldRead();
  }

  @Override
  protected void read() {
    notifyObservers(new ReadAction(), VWEvaluatorBrain.class);
  }

  @Override
  public Evaluation evaluate() {
    return strategy.evaluate(null, 0, 0);
  }

  @Override
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
    Logger.getGlobal().log(Level.INFO, "Evaluator Starting");
    this.state = ThreadState.JUST_STARTED;
    this.state = ThreadState.AFTER_DECIDE;

    waitForServerBeforeExecution();
    execute(null); // the action will be a read action if shouldRead().
  }

  // ******* THREAD RELATED METHODS ******* //

  @Override
  public ThreadState getState() {
    return this.state;
  }

  @Override
  public void resume() {
    this.canProceed = true;
  }

  @Override
  public void waitForServerBeforeExecution() {
    while (!this.canProceed) {
      Logger.getGlobal().log(Level.INFO, "Evaluator is waiting");
    }
  }

  public void setState(ThreadState state) {
    this.state = state;
  }

  // ******* NOT USED METHODS ******* //

  /**
   * Not used, start is called via
   * {@link VacuumWorldMindInterface#start(int, boolean, Set)}.
   */
  @Override
  public void start() {
  }

  /**
   * Not used. Cycling is done via
   * {@link VacuumWorldMindInterface#start(int, boolean, Set)}.
   */
  @Override
  protected void stepCycle(Object perviousActionResultWrapper) {
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
  }

@Override
public boolean canProceed() {
	return this.canProceed;
}
}
