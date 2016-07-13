package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.Set;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldServer;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;
import util.Utils;

public class VacuumWorldMonitorMind extends AbstractAgentMind {

  private EvaluationStrategy<?> strategy;
  private EnvironmentalAction nextAction = null;
  //private Set<EnvironmentalAction> actions;
  private MonitoringResult perception;

  private Logger logger;

  public VacuumWorldMonitorMind(VacuumWorldStepEvaluationStrategy strategy) {
    this.strategy = strategy;
    logger = Utils
        .fileLogger("C:/Users/Ben/workspace/vacuumworldmodel/logs/eval/evaluation.log");
  }

  @Override
  public EnvironmentalAction decide(Object... parameters) {
    nextAction = (EnvironmentalAction) getAvailableActionsForThisCicle().toArray()[0];
    return nextAction;
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    notifyObservers(null, VacuumWorldMonitorBrain.class);
    if(perception!= null) {
      strategy.update(((VacuumWorldSpaceRepresentation) (perception)
          .getPerception()).clone());
    }
  }

  @Override
  public void execute(EnvironmentalAction action) {
    notifyObservers(nextAction, VacuumWorldMonitorBrain.class);
    if (VacuumWorldServer.getCycleNumber() % 5 == 0
        && VacuumWorldServer.getCycleNumber() != 0) {
      Evaluation e = strategy.evaluate(null, 0,
          VacuumWorldServer.getCycleNumber());
      logger.info(((VacuumWorldStepCollectiveEvaluation) e).represent());
    }
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorBrain) {
      this.perception = (MonitoringResult) arg;
    }
  }

  public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
    //this.actions = actions;
	for(Class<? extends AbstractAction> action : actions) {
		addAvailableActionForThisCicle(action);
	}
  }
}
