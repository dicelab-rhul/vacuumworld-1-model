package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

/**
 * The representation of the {@link VacuumWorldLocation},
 * {@link ActorFacingDirection} and {@link SpeechAction} if it was performed of
 * a {@link VacuumWorldCleaningAgent} in a given cycle. </br>
 * Uses: in {@link AgentDatabaseRepresentation} to representation cyclic data.
 * This representation will be converted to a JSON String by
 * {@link ObjectMapper} indirectly via {@link AgentDatabaseRepresentation}.
 * </br>
 * See also: {@link VacuumWorldMongoBridge}.
 * 
 * @author Ben Wilkins
 *
 */
public class CycleDatabaseRepresentation {
	// IMPORTANT TO KEEP variable names as are for JSON generation!
	private int x;
	private int y;
	private ActorFacingDirection dir;
	private SpeechDatabaseRepresentation speech;

	/**
	 * Constructor.
	 * 
	 * @param x
	 *            position
	 * @param y
	 *            position
	 * @param dir
	 *            direction of facing
	 * @param the
	 *            speech action that was performed on this cycle
	 */
	public CycleDatabaseRepresentation(int x, int y, ActorFacingDirection dir, SpeechDatabaseRepresentation speech) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.setSpeech(speech);
	}

	/**
	 * Constructor.
	 * 
	 * @param x
	 *            position
	 * @param y
	 *            position
	 * @param dir
	 *            direction of facing
	 */
	public CycleDatabaseRepresentation(int x, int y, ActorFacingDirection dir) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.setSpeech(null);
	}

	// Needed explicit for json mapper.
	public CycleDatabaseRepresentation() {
		super();
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public ActorFacingDirection getDir() {
		return this.dir;
	}

	public void setDir(ActorFacingDirection dir) {
		this.dir = dir;
	}

	@Override
	public String toString() {
		return "[" + this.x + "," + this.y + "," + this.dir + "]";
	}

	public SpeechDatabaseRepresentation getSpeech() {
		return speech;
	}

	public void setSpeech(SpeechDatabaseRepresentation speech) {
		this.speech = speech;
	}
}