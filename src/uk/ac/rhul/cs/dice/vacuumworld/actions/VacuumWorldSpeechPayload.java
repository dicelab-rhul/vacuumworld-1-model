package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;

public class VacuumWorldSpeechPayload implements Payload<String> {
	private boolean isGreeting;
	private String payload;

	public VacuumWorldSpeechPayload(String payload, boolean isGreeting) {
		this.payload = payload;
		this.isGreeting = isGreeting;
	}
	
	public boolean isGreetingAction() {
		return this.isGreeting;
	}

	@Override
	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String getPayload() {
		return this.payload;
	}
}