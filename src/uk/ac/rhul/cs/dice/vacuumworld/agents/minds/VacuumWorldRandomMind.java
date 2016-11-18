package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VWPerception;

public class VacuumWorldRandomMind extends VacuumWorldDefaultMind {

    public VacuumWorldRandomMind(String bodyId) {
	super(bodyId);
    }

    @Override
    public EnvironmentalAction decide(Object... parameters) {
	VWPerception perception = getPerception();

	if (perception != null) {
	    updateAvailableActions(perception);
	}

	this.getAvailableActionsForThisCycle().remove(SpeechAction.class);

	return decideActionRandomly();
    }
}