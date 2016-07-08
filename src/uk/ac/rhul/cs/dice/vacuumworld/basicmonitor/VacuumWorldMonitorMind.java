package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldServer;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;

public class VacuumWorldMonitorMind extends AbstractAgentMind {

  private EvaluationStrategy<?> strategy;
  private Action nextAction = null;
  private Set<Action> actions;
  
  public VacuumWorldMonitorMind(VacuumWorldStepEvaluationStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Action decide(Object... parameters) {
    nextAction = (Action) actions.toArray()[0];
    return nextAction;
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    if (perceptionWrapper instanceof MonitoringResult) {
      strategy
          .update(((VacuumWorldSpaceRepresentation) ((MonitoringResult) perceptionWrapper)
              .getPerception()).clone());
      if(VacuumWorldServer.getCycleNumber() % 5 == 0 && VacuumWorldServer.getCycleNumber() != 0) {
        Evaluation e = strategy.evaluate(null, 0, VacuumWorldServer.getCycleNumber());
        System.out.println(((VacuumWorldStepCollectiveEvaluation)e).represent());
      } 
    }
  }

  @Override
  public void execute(Action action) {
    notifyObservers(nextAction, VacuumWorldMonitorBrain.class);
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorBrain) {
      perceive(arg);
    }
  }
  
  public void setAvailableActions(Set<Action> actions) {
    this.actions = actions;
  }
}
