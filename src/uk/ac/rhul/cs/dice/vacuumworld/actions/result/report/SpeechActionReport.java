package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class SpeechActionReport extends AbstractActionReport {
	private Map<String, VacuumWorldSpeechPayload> speeches;
	
	public SpeechActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates) {
		super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates);
	
		this.speeches = new HashMap<>();
	}
	
	public Map<String, VacuumWorldSpeechPayload> getSpeeches() {
		return this.speeches;
	}
	
	public VacuumWorldSpeechPayload getSpeechDirectedToSpecificActor(String recipientId) {
		return this.speeches.getOrDefault(recipientId, null);
	}
	
	public void addSpeech(String recipient, VacuumWorldSpeechPayload payload) {
		this.speeches.put(recipient, payload);
	}
}