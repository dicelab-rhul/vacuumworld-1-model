package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;

public class VacuumWorldSpeechPayload implements Payload<String> {

  private String payload;
  
  public VacuumWorldSpeechPayload(String payload) {
    this.payload = payload;
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