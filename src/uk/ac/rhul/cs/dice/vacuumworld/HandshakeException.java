package uk.ac.rhul.cs.dice.vacuumworld;

public class HandshakeException extends Exception {
	private static final long serialVersionUID = -243988454479246092L;

	public HandshakeException(Exception e) {
		this.initCause(e);
	}
	
	public HandshakeException(String message) {
		super(message);
	}
}