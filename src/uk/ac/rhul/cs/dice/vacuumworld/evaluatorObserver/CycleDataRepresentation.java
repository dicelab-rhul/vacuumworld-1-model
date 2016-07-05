package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

/**
 * The representation of the {@link VacuumWorldLocation} and
 * {@link AgentFacingDirection} of a {@link VacuumWorldCleaningAgent} in a given
 * cycle. </br> Uses: in {@link AgentDatabaseRepresentation} to representation
 * cyclic data. This representation will be converted to a JSON String by
 * {@link ObjectMapper} indirectly via {@link AgentDatabaseRepresentation}.
 * </br> See also: {@link VWMongoBridge}.
 * 
 * @author Ben Wilkins
 *
 */
public class CycleDataRepresentation {
  private int x;
  private int y;
  private AgentFacingDirection dir;

  /**
   * Constructor.
   * 
   * @param x
   *          position
   * @param y
   *          position
   * @param dir
   *          direction of facing
   */
  public CycleDataRepresentation(int x, int y, AgentFacingDirection dir) {
    super();
    this.x = x;
    this.y = y;
    this.dir = dir;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public AgentFacingDirection getDir() {
    return dir;
  }

  public void setDir(AgentFacingDirection dir) {
    this.dir = dir;
  }

}
