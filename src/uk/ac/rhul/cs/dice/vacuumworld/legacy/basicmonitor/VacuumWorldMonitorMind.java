package uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor;

import java.util.Set;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldSpaceRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitorMind extends AbstractAgentMind {
	private EvaluationStrategy<?> strategy;
	private EnvironmentalAction nextAction = null;
	private MonitoringResult perception;

	private Logger logger;

	public VacuumWorldMonitorMind(VacuumWorldStepEvaluationStrategy strategy) {
		this.strategy = strategy;
		
		if (ConfigData.getLoggingFlag()) {
			this.logger = VWUtils.fileLogger("logs/eval/evaluation.log", false);
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		try {
			this.nextAction = getAvailableActionsForThisCycle().get(0).newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			VWUtils.log(e);
		}
		
		return this.nextAction;
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitorBrain.class);
		
		if (this.perception != null) {
			this.strategy.update(((VacuumWorldSpaceRepresentation) (this.perception).getPerception()).replicate());
		}
	}

	@Override
	public void execute(EnvironmentalAction action) {
		notifyObservers(this.nextAction, VacuumWorldMonitorBrain.class);

		if (VWUtils.getCycleNumber() % 5 == 0 && VWUtils.getCycleNumber() != 0) {
			Evaluation e = this.strategy.evaluate(null, 0, VWUtils.getCycleNumber());
			
			if (ConfigData.getLoggingFlag()) {
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