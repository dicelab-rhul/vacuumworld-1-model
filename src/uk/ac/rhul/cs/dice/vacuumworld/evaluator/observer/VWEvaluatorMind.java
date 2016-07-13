package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
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
  private EnvironmentalAction nextAction;
  private ReadResult perception;

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
  public void execute(EnvironmentalAction action) {
    //Evaluation e = this.evaluate();
    //System.out.println("AN EVALUATION WAS MADE!");
    if(shouldRead()) {
      read();
    } else {
      System.out.println("EVALUATOR DID NOTHING");
    }
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    if(perception != null) {
      RefinedPerception[] ps = (RefinedPerception[]) perception.getPerceptions()
          .toArray(new RefinedPerception[] {});
      strategy.update(ps[0]);
    }
  }

  @Override
  public EnvironmentalAction decide(Object... parameters) {
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
    perception = result;
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
    System.out.println("READING");
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
