package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadAction;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;

public class VWEvaluatorMind extends EvaluatorMind {
  
  private EvaluationStrategy<?> strategy;
  private int changes = 0;
  private ReadAction readAction = new ReadAction();
  private Action nextAction;

  /**
   * Constructor.
   * 
   * @param strategy
   *          that will be used for evaluation.
   */
  public VWEvaluatorMind(EvaluationStrategy<?> strategy) {
    this.strategy = strategy;
  }

  @Override
  public void execute(Action action) {
    if(nextAction != null) {
      read();
    } else {
      System.out.println("EVALUATOR DID NOTHING");
    }
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
    if(changes > 1) {
      System.out.println("EVALUATOR DECIDED TO READ");
      nextAction = readAction;
      changes = 0;
    } else {
      System.out.println("EVALUATOR DECIDED TO DO NOTHING");
      nextAction = null;
    }
    return nextAction;
  }

  @Override
  protected void manageReadResult(ReadResult result) {
    perceive(result);
    Evaluation e = this.evaluate();
    System.out.println("AN EVALUATION WAS MADE!");
  }
  
  @Override
  protected void manageChangedResult(ChangedResult result) {
    System.out.println("RECIEVED CHANGED RESULT");
    this.changes++;
  }

  @Override
  protected boolean shouldRead() {
    return nextAction instanceof ReadAction;
  }

  @Override
  protected boolean shouldEvaluate() {
    return shouldRead();
  }

  @Override
  protected void read() {
    notifyObservers(nextAction, VWEvaluatorBrain.class);
  }

  @Override
  public Evaluation evaluate() {
    return strategy.evaluate(null, 0, 0);
  }

  /** 
   * Not used.
   */
  @Override
  public void updateCon(CustomObservable o, Object arg) {
  }
}
