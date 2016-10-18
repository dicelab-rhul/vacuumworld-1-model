package uk.ac.rhul.cs.dice.vacuumworld.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;

public class VacuumWorldThreadStateDecide extends DefaultThreadStateDecide {
	@Override
	public void run(ActorRunnable runnable) {
		if(runnable instanceof VacuumWorldActorRunnable) {
			EnvironmentalAction nextAction = runnable.getActorMind().decide();
			Mind mind = ((VacuumWorldActorRunnable) runnable).getActorMind();
			setNextAction(nextAction, mind);
		}
		else {
			super.run(runnable);
		}
	}

	private void setNextAction(EnvironmentalAction nextAction, Mind mind) {
		if(mind instanceof VacuumWorldDefaultMind) {
			((VacuumWorldDefaultMind) mind).setNextActionForExecution(nextAction);
		}
		else if(mind instanceof UserMind) {
			((UserMind) mind).setNextActionForExecution(nextAction);
		}
		else {
			throw new IllegalArgumentException("Bad mind: " + mind.getClass().getSimpleName());
		}
	}
}