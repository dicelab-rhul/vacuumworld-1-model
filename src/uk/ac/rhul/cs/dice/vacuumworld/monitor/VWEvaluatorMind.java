package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

public class VWEvaluatorMind extends EvaluatorMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private boolean canProceed = false;

  private ArrayList<RefinedPerception> perceptions;
  private int changes = 0;
  
  /**
   * Constructor.
   * 
   * @param strategy
   *          that will be used for evaluation.
   */
  public VWEvaluatorMind(EvaluationStrategy strategy) {
    super(strategy);
    this.perceptions = new ArrayList<RefinedPerception>();
  }

  @Override
  public void execute(Action action) {
    //System.out.println("EVALUATOR EXECUTE");
    this.read();
    Evaluation e = this.evaluate(perceptions);
    if(e != null) {
      System.out.println("AN EVALUATION WAS MADE!");
    }
    this.state = ThreadState.AFTER_PERCEIVE;
    return;
  }

  @Override
  protected void manageReadResult(ReadResult result) {
    //System.out.println("RECEIVED READ RESULT");
    if(result.getActionResult().equals(ActionResult.ACTION_DONE)) {
      
      RefinedPerception[] ps = (RefinedPerception[]) result.getPerceptions().toArray(new RefinedPerception[]{});
      if(ps.length > 1) {
        Logger.getGlobal().log(Level.SEVERE, "INCORRECT READ SIZE FOR EVALUATION!");
      } else {
        perceptions.add(ps[0]);
      }
    } else {
      Logger.getGlobal().log(Level.SEVERE, "READ RESULT FAILED ",
          result.getFailureReason());
    }
  }

  @Override
  protected void manageChangedResult(ChangedResult result) {
    //System.out.println("RECIEVED CHANGED RESULT!");
    this.changes++;
  }

  @Override
  protected boolean shouldRead() {
    return changes % 3 == 0 && changes != 0;
  }

  @Override
  protected boolean shouldEvaluate() {
    return shouldRead(); // if a read has just been made then evaluate
  }

  @Override
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
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
    while (!this.canProceed)
      ;
  }

  public void setState(ThreadState state) {
    this.state = state;
  }

  // ******* NOT USED METHODS ******* //

  /**
   * Perceptions are made the same way as {@link VacuumWorldCleaningAgent}s.
   * Evaluator perceptions are made actively and are not recieved from the
   * environment as a result if performing an action.
   */
  @Override
  public void perceive(Object perceptionWrapper) {
  }

  /**
   * Not used. An {@link VWEvaluatorMind} makes no decisions.
   */
  @Override
  public Action decide(Object... parameters) {
    return null;
  }

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
   System.out.println("EVALUATOR:" + getMethodName(1));
  }
  
  public static String getMethodName(final int depth)
  {
    final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

    //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
    // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
    return ste[ste.length - 1 - depth].getMethodName(); //Thank you Tom Tresansky
  }
  
  
  
  
  
  
  
}
