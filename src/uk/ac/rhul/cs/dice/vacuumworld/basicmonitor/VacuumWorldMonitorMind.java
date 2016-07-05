package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldServer;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;

public class VacuumWorldMonitorMind extends AbstractAgentMind implements
    VacuumWorldMindInterface {

  private ThreadState state;
  private boolean canProceed = false;
  private Action nextAction = null;
  private EvaluationStrategy<?> strategy;

  public VacuumWorldMonitorMind(VacuumWorldStepEvaluationStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
    this.state = ThreadState.JUST_STARTED;

    this.nextAction = decide();
    this.state = ThreadState.AFTER_DECIDE;

    waitForServerBeforeExecution();
    execute(this.nextAction);
  }

  @Override
  public Action decide(Object... parameters) {
    System.out.println(this.getClass().getSimpleName() + "decide");
    return new TotalPerceptionAction();
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    System.out.println(this.getClass().getSimpleName() + "perceive");
    if (perceptionWrapper instanceof MonitoringResult) {
      strategy
          .update(((VacuumWorldSpaceRepresentation) ((MonitoringResult) perceptionWrapper)
              .getPerception()).clone());
      strategy.evaluate(null, 0, VacuumWorldServer.getCycleNumber());
    }
    this.state = ThreadState.AFTER_PERCEIVE;
    return;
  }

  @Override
  public void execute(Action action) {
    System.out.println(this.getClass().getSimpleName() + "execute");
    notifyObservers(action, VacuumWorldMonitorBrain.class);
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorBrain) {
      System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
          + o.getClass().getSimpleName() + " " + arg);
      perceive(arg);
    }
  }

  public void waitForServerBeforeExecution() {
    while (!this.canProceed)
      ;
  }

  public void resume() {
    this.canProceed = true;
  }

  public ThreadState getState() {
    return this.state;
  }

@Override
public boolean canProceed() {
	return this.canProceed;
}

}
