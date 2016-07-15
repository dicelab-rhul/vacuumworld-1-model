package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database;

public class SpeechDatabaseRepresentation {

  private String payload;
  private int cycle;
  private String[] recipients;

  public SpeechDatabaseRepresentation(String payload, int cycle,
      String[] recipients) {
    super();
    this.payload = payload;
    this.cycle = cycle;
    this.recipients = recipients;
  }

  public SpeechDatabaseRepresentation() {
    super();
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public int getCycle() {
    return cycle;
  }

  public void setCycle(int cycle) {
    this.cycle = cycle;
  }

  public String[] getRecipients() {
    return recipients;
  }

  public void setRecipients(String[] recipients) {
    this.recipients = recipients;
  }

}
