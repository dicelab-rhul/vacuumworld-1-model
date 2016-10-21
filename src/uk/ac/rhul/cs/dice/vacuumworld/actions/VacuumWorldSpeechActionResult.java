package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldSpeechActionResult extends DefaultActionResult<VacuumWorldPerception> {
	private String sender;
	private VacuumWorldSpeechPayload payload;

	public VacuumWorldSpeechActionResult(ActionResult result, SpeechAction action) {
		super(result, action.getRecipientsIds(), null);
		
		this.sender = action.getSenderId();
		this.payload = (VacuumWorldSpeechPayload) action.getPayload();
	}

	public String getSenderId() {
		return this.sender;
	}

	public VacuumWorldSpeechPayload getPayload() {
		return this.payload;
	}

	@Override
	public String toString() {
		return this.sender + "," + this.payload.getPayload();
	}
}