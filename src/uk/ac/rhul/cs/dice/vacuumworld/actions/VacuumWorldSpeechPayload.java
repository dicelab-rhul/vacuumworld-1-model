package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;

public class VacuumWorldSpeechPayload implements Payload {
	private Object payload;
	
	@Override
	public void setPayload(Object payload) {
		this.payload = payload;
	}

	@Override
	public Object getPayload() {
		return this.payload;
	}
}