package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

/**
 * The representation of a {@link Dirt} used internally by
 * {@link VacuumWorldMonitoringContainer} to build a useful representation of
 * what is happening inside its sub container - {@link VacuumWorldSpace}. </br>
 * See also: {@link DirtDatabaseRepresentation},
 * {@link VacuumWorldSpaceRepresentation}. </br> Note: The position of the dirt
 * is stored by {@link VacuumWorldSpaceRepresentation}.
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
   * @param _id
   *          the id of the dirt
   * @param type
   *          the {@link DirtType} of the dirt
   */
  public DirtRepresentation(String _id, DirtType type) {
    super();
    this._id = _id;
    this.type = type;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public DirtType getType() {
    return type;
  }

  public void setType(DirtType type) {
    this.type = type;
  }
}
