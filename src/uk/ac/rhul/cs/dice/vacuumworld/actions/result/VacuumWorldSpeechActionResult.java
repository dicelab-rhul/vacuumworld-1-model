package uk.ac.rhul.cs.dice.vacuumworld.actions.result;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractActionResult;

/**
 * 
 * This class wraps a message and its metadata.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana
 *
 */
public class VacuumWorldSpeechActionResult extends AbstractActionResult {
    private String sender;
    private VacuumWorldSpeechPayload payload;

    public VacuumWorldSpeechActionResult(ActionResult result, SpeechAction action) {
	super(result, action.getRecipientsIds(), null);

	this.sender = action.getSenderId();
	this.payload = (VacuumWorldSpeechPayload) action.getPayload();
    }

    /**
     * 
     * Returns the id of the sender of this communication.
     * 
     * @return the id of the sender of this communication.
     * 
     */
    public String getSenderId() {
	return this.sender;
    }

    /**
     * 
     * Returns the {@link VacuumWorldSpeechPayload} of this communication.
     * 
     * @return the {@link VacuumWorldSpeechPayload} of this communication.
     * 
     */
    public VacuumWorldSpeechPayload getPayload() {
	return this.payload;
    }

    @Override
    public String toString() {
	return this.sender + "," + this.payload.getPayload();
    }
}