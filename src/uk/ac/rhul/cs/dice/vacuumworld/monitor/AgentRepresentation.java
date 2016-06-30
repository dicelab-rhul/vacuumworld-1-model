package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public class AgentRepresentation {

  private String _id;
  private VacuumWorldAgentType type;
  private int sensors;
  private int actuators;
  private boolean clean;
  private boolean successfulClean;
  private AgentFacingDirection direction;
  private int x;
  private int y;

  public AgentRepresentation(String _id, VacuumWorldAgentType type,
      int sensors, int actuators, AgentFacingDirection direction, int x, int y) {
    super();
    this._id = _id;
    this.type = type;
    this.sensors = sensors;
    this.actuators = actuators;
    this.direction = direction;
    this.x = x;
    this.y = y;
    this.setClean(false);
    this.setSuccessfulClean(false);
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public VacuumWorldAgentType getType() {
    return type;
  }

  public void setType(VacuumWorldAgentType type) {
    this.type = type;
  }

  public int getSensors() {
    return sensors;
  }

  public void setSensors(int sensors) {
    this.sensors = sensors;
  }

  public int getActuators() {
    return actuators;
  }

  public void setActuators(int actuators) {
    this.actuators = actuators;
  }

  public AgentFacingDirection getDirection() {
    return direction;
  }

  public void setDirection(AgentFacingDirection direction) {
    this.direction = direction;
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

  public boolean isClean() {
    return clean;
  }

  public void setClean(boolean clean) {
    this.clean = clean;
  }

  public boolean isSuccessfulClean() {
    return successfulClean;
  }

  public void setSuccessfulClean(boolean successfulClean) {
    this.successfulClean = successfulClean;
  }
}
