package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldRandomMind extends VacuumWorldDefaultMind {
	private static final String NAME = "RANDOM";
	
	public static final String getName() {
		return NAME;
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		VacuumWorldPerception perception = getPerception();
		
		if(perception != null) {
			updateAvailableActions(perception);
		}
		
		this.getAvailableActions().remove(SpeechAction.class);
		
		return decideActionRandomly();
	}
}