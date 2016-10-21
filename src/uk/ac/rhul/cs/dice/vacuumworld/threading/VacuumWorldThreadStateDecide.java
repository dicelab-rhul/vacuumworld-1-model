package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldThreadStateDecide extends AbstractThreadStateDecide<VacuumWorldPerception> {
	@Override
	public void run(AbstractActorRunnable<VacuumWorldPerception> runnable) {
		if(runnable instanceof VacuumWorldActorRunnable) {
			EnvironmentalAction<VacuumWorldPerception> nextAction = runnable.getActorMind().decide();
			Mind<VacuumWorldPerception> mind = ((VacuumWorldActorRunnable) runnable).getActorMind();
			mind.setNextActionForExecution(nextAction);
		}
		else {
			super.run(runnable);
		}
	}
}