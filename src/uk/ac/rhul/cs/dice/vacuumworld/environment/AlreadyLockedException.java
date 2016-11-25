package uk.ac.rhul.cs.dice.vacuumworld.environment;

public class AlreadyLockedException extends Exception {
    private static final long serialVersionUID = 8276590997989030515L;

    public AlreadyLockedException(String message) {
	super(message);
    }
}