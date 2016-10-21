package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;

public abstract class VacuumWorldAbstractAgentMind<P extends Perception> extends AbstractAgentMind<P> {
	private int perceptionRange;
	private boolean canSeeBehind;
	
	public VacuumWorldAbstractAgentMind(Random rng, String bodyId) {
		super(rng, bodyId);
	}
	
	public VacuumWorldAbstractAgentMind(String bodyId) {
		super(new Random(System.currentTimeMillis()), bodyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadAvailableActionsForThisMindFromArbitraryParameters(Object... mindActions) {
		if(this instanceof VacuumWorldDefaultMind) {
			for(Class<? extends EnvironmentalAction<VacuumWorldPerception>> candidate : ConfigData.getCleaningAgentActions()) {
				loadAvailableActionForThisMind((Class<? extends EnvironmentalAction<P>>) candidate);
			}
		}
		else if(this instanceof VacuumWorldMonitoringAgentMind) {
			for(Class<? extends EnvironmentalAction<VacuumWorldMonitoringPerception>> candidate : ConfigData.getMonitoringAgentActions()) {
				loadAvailableActionForThisMind((Class<? extends EnvironmentalAction<P>>) candidate);
			}
		}
		else if(this instanceof UserMind) {
			for(Class<? extends EnvironmentalAction<VacuumWorldPerception>> candidate : ConfigData.getUserActions()) {
				loadAvailableActionForThisMind((Class<? extends EnvironmentalAction<P>>) candidate);
			}
		}
	}

	@Override
	public boolean lastActionSucceeded() {
		return ActionResult.ACTION_DONE.equals(getLastActionResult().getActionResult());
	}
	
	@Override
	public boolean wasLastActionImpossible() {
		return ActionResult.ACTION_IMPOSSIBLE.equals(getLastActionResult().getActionResult());
	}
	
	@Override
	public boolean lastActionFailed() {
		return ActionResult.ACTION_FAILED.equals(getLastActionResult().getActionResult());
	}

	@Override
	public int getPerceptionRange() {
		return this.perceptionRange;
	}

	@Override
	public boolean canSeeBehind() {
		return this.canSeeBehind;
	}

	@Override
	public void setCanSeeBehind(boolean canSeeBehind) {
		this.canSeeBehind = canSeeBehind;
	}

	@Override
	public void setPerceptionRange(int preceptionRange) {
		this.perceptionRange = preceptionRange;
	}
}