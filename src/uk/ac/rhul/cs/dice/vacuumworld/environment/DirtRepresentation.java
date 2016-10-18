package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldSpaceRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database.DirtDatabaseRepresentation;

/**
 * The representation of a {@link Dirt} used internally by
 * {@link VacuumWorldMonitoringContainer} to build a useful representation of
 * what is happening inside its sub container - {@link VacuumWorldSpace}. </br>
 * See also: {@link DirtDatabaseRepresentation},
 * {@link VacuumWorldSpaceRepresentation}. </br>
 * Note: The position of the dirt is stored by
 * {@link VacuumWorldSpaceRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class DirtRepresentation {
	private String _id;
	private DirtType type;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the id of the dirt
	 * @param type
	 *            the {@link DirtType} of the dirt
	 */
	public DirtRepresentation(String id, DirtType type) {
		this._id = id;
		this.type = type;
	}

	public DirtRepresentation duplicate() {
		return new DirtRepresentation(this._id, this.type);
	}

	public String getId() {
		return this._id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public DirtType getType() {
		return this.type;
	}

	public void setType(DirtType type) {
		this.type = type;
	}
}