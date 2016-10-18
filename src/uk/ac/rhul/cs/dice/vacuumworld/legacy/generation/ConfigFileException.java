package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

public class ConfigFileException extends Exception {
	private static final long serialVersionUID = -826560803885833972L;

	public ConfigFileException(String configFilePath, String message) {
		super(configFilePath + " doesn't exist or is not in the correct format: " + message + ".");
	}
}