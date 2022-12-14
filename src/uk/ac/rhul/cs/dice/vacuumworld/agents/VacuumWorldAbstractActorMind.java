package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;

public abstract class VacuumWorldAbstractActorMind extends AbstractAgentMind {
    private int perceptionRange;
    private boolean canSeeBehind;

    public VacuumWorldAbstractActorMind(Random rng, String bodyId) {
	super(rng, bodyId);
    }

    public VacuumWorldAbstractActorMind(String bodyId) {
	super(new Random(System.currentTimeMillis()), bodyId);
    }

    /**
     * 
     * Loads the available action prototypes for this mind (in general, not for a specific cycle).
     * 
     */
    @Override
    public void loadAvailableActionsForThisMindFromArbitraryParameters(Object... mindActions) {
	if (this instanceof VacuumWorldDefaultMind) {
	    ConfigData.getCleaningAgentActions().forEach(this::loadAvailableActionForThisMind);
	}
	else if (this instanceof VacuumWorldMonitoringAgentMind) {
	    ConfigData.getMonitoringAgentActions().forEach(this::loadAvailableActionForThisMind);
	}
	else if (this instanceof UserMind) {
	    ConfigData.getUserActions().forEach(this::loadAvailableActionForThisMind);
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