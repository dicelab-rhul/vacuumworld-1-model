package uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AbstractActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AbstractThreadStateDecide;

public class VacuumWorldMonitoringThreadStateDecide extends AbstractThreadStateDecide<VacuumWorldMonitoringPerception> {
	@Override
	public void run(AbstractActorRunnable<VacuumWorldMonitoringPerception> runnable) {
		if(runnable instanceof VacuumWorldMonitoringActorRunnable) {
			EnvironmentalAction<VacuumWorldMonitoringPerception> nextAction = runnable.getActorMind().decide();
			Mind<VacuumWorldMonitoringPerception> mind = ((VacuumWorldMonitoringActorRunnable) runnable).getActorMind();
			mind.setNextActionForExecution(nextAction);
		}
		else {
			super.run(runnable);
		}
	}
}