package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldRandomMind extends VacuumWorldDefaultMind {
	
	public VacuumWorldRandomMind(String bodyId) {
		super(bodyId);
	}

	@Override
	public EnvironmentalAction<VacuumWorldPerception> decide(Object... parameters) {
		VacuumWorldPerception perception = getPerception();
		
		if(perception != null) {
			updateAvailableActions(perception);
		}
		
		this.getAvailableActionsForThisCycle().remove(SpeechAction.class);
		
		return decideActionRandomly();
	}
}