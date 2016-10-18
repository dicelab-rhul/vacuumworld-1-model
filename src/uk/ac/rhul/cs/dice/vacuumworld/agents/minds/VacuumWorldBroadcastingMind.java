package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;

public class VacuumWorldBroadcastingMind extends VacuumWorldDefaultMind {

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		String[] choices = new String[]{"N", "S", "W", "E"};
		
		return buildSpeechAction(getBodyId(), new ArrayList<>(), new VacuumWorldSpeechPayload("move" + choices[getRNG().nextInt()%choices.length], false));
	}
}