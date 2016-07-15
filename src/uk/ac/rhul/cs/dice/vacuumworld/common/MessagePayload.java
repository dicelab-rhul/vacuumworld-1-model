package uk.ac.rhul.cs.dice.vacuumworld.common;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;

public class MessagePayload implements Payload {

  private String message;

  @Override
  public void setPayload(Object payload) {
    this.message = (String)payload;
  }

  @Override
  public Object getPayload() {
    return message;
  }
}
