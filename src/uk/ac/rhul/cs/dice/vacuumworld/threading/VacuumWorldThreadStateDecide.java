package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public class VacuumWorldThreadStateDecide extends DefaultThreadStateDecide {
	@Override
	public void run(AgentRunnable runnable) {
		if(runnable instanceof VacuumWorldCleaningAgentRunnable) {
			EnvironmentalAction nextAction = runnable.getAgentMind().decide();
			((VacuumWorldCleaningAgentRunnable) runnable).getAgentMind().setNextActionForExecution(nextAction);
		}
		else {
			super.run(runnable);
		}
	}
}