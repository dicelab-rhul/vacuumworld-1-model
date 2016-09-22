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
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldMonitorMind extends AbstractAgentMind {

	private EvaluationStrategy<?> strategy;
	private EnvironmentalAction nextAction = null;
	private MonitoringResult perception;

	private Logger logger;

	public VacuumWorldMonitorMind(VacuumWorldStepEvaluationStrategy strategy) {
		this.strategy = strategy;
		if(VacuumWorldServer.LOG) {
		  this.logger = Utils.fileLogger("logs/eval/evaluation.log", false);
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		try {
      this.nextAction = getAvailableActionsForThisCycle().get(0).newInstance();
    } catch (InstantiationException | IllegalAccessException e ) {
      e.printStackTrace();
    }
		return this.nextAction;
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitorBrain.class);
		if (this.perception != null) {
			this.strategy.update(((VacuumWorldSpaceRepresentation) (this.perception).getPerception()).clone());
		}
	}

	@Override
	public void execute(EnvironmentalAction action) {
		notifyObservers(this.nextAction, VacuumWorldMonitorBrain.class);
		
		if (Utils.getCycleNumber() % 5 == 0 && Utils.getCycleNumber() != 0) {
			Evaluation e = this.strategy.evaluate(null, 0, Utils.getCycleNumber());
			if(VacuumWorldServer.LOG) {
			  this.logger.info(((VacuumWorldStepCollectiveEvaluation) e).represent());
			}
		}
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitorBrain) {
			this.perception = (MonitoringResult) arg;
		}
	}

	public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
		for (Class<? extends AbstractAction> action : actions) {
			addAvailableActionForThisCycle(action);
		}
	}
}