package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;

public class VWEvaluatorMind extends EvaluatorMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private boolean canProceed;
  
  public VWEvaluatorMind(EvaluationStrategy strategy) {
    super(strategy);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Action decide(Object... parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void execute(Action action) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void manageReadResult(ReadResult result) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void manageChangedResult(ChangedResult result) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected boolean shouldRead() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected boolean shouldEvaluate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void start() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void stepCycle(Object perviousActionResultWrapper) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ThreadState getState() {
    return this.state;
  }

  @Override
  public void resume() {
    this.canProceed = true;
  }

  @Override
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void waitForServerBeforeExecution() {
    while (!this.canProceed)
      ;
  }
}
