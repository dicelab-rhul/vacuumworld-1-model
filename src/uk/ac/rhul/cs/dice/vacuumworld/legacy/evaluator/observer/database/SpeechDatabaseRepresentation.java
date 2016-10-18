package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database;

public class SpeechDatabaseRepresentation {
	public static final String ALLRECIPIENTS = "A";
	private String payload;
	private String[] recipients;

	public SpeechDatabaseRepresentation(String payload, String[] recipients) {
		this.payload = payload;
		this.recipients = recipients;
	}

	public SpeechDatabaseRepresentation() {
		super();
	}

	public String getPayload() {
		return this.payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String[] getRecipients() {
		return this.recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}
}