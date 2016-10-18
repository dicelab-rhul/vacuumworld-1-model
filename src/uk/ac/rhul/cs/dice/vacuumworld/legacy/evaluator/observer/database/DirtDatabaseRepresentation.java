package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorAgent;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.DirtRepresentation;

/**
 * The representation of a {@link Dirt} in a MongoDB. Converting an instance of
 * this class to and from JSON Strings using {@link ObjectMapper} is how the
 * MongoDB communicates {@link Dirt} data with {@link VacuumWorldMongoBridge}.
 * </br>
 * See also: {@link DirtRepresentation}. Implements (@link RefinedPerception} as
 * this class will be a part of the {@link Perception} of an
 * {@link EvaluatorAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class DirtDatabaseRepresentation implements RefinedPerception {
	// IMPORTANT TO KEEP variable names as are for JSON generation!
	private String _id;
	private DirtType type;
	private int x;
	private int y;
	private int startCycle;
	private int endCycle;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the id of the dirt
	 * @param type
	 *            the {@link DirtType} of the dirt
	 * @param x
	 *            position
	 * @param y
	 *            position
	 * @param startCycle
	 *            the cycle that the dirt was created
	 * @param endCycle
	 *            the cycle that the dirt was cleaned
	 */
	public DirtDatabaseRepresentation(String id, DirtType type, int x, int y, int startCycle, int endCycle) {
		this._id = id;
		this.type = type;
		this.x = x;
		this.y = y;
		this.startCycle = startCycle;
		this.endCycle = endCycle;
	}

	// Needed explicit for json mapper.
	public DirtDatabaseRepresentation() {
		super();
	}

	public DirtType getType() {
		return this.type;
	}

	public void setType(DirtType type) {
		this.type = type;
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

	public int getStartCycle() {
		return this.startCycle;
	}

	public void setStartCycle(int startCycle) {
		this.startCycle = startCycle;
	}

	public int getEndCycle() {
		return this.endCycle;
	}

	public void setEndCycle(int endCycle) {
		this.endCycle = endCycle;
	}

	public String getId() {
		return this._id;
	}

	public void setId(String id) {
		this._id = id;
	}

	@Override
	public String toString() {
		return this._id + " : " + this.type + " [Start:" + this.startCycle + ", End: " + this.endCycle + "]";
	}
}