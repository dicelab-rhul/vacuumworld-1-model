package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;

/**
 * 
 * This class wraps the actual content of a message.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana
 *
 */
public class VacuumWorldSpeechPayload implements Payload<String> {
    private boolean isGreeting;
    private String payload;

    public VacuumWorldSpeechPayload(String payload, boolean isGreeting) {
	this.payload = payload;
	this.isGreeting = isGreeting;
    }

    /**
     * 
     * Returns whether this communication is a greeting from someone.
     * 
     * @return true if this communication is a greeting from someone, false otherwise.
     * 
     */
    public boolean isGreetingAction() {
	return this.isGreeting;
    }

    /**
     * 
     * Stores the payload.
     * 
     */
    @Override
    public void setPayload(String payload) {
	this.payload = payload;
    }

    /**
     * 
     * Returns the actual message.
     * 
     */
    @Override
    public String getPayload() {
	return this.payload;
    }
}