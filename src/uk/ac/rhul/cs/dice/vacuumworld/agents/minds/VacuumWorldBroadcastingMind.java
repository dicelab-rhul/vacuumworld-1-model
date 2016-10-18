package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;

public class VacuumWorldBroadcastingMind extends VacuumWorldDefaultMind {

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		return buildSpeechAction(getBodyId(), new ArrayList<>(), new VacuumWorldSpeechPayload("moveN", false));
	}
}