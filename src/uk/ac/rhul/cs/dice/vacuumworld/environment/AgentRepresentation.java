package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.AgentDatabaseRepresentation;

/**
 * The representation of a {@link VacuumWorldCleaningAgent} used internally by
 * {@link VacuumWorldMonitoringContainer} to build a useful representation of
 * what is happening inside its sub container - {@link VacuumWorldSpace}. </br>
 * See also: {@link AgentDatabaseRepresentation},
 * {@link VacuumWorldSpaceRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class AgentRepresentation implements Cloneable {

  private String _id;
  private VacuumWorldAgentType type;
  private int sensors;
  private int actuators;
  // did the agent perform a clean this cycle
  private boolean clean;
  // was the clean successful
  private boolean successfulClean;
  private AgentFacingDirection direction;
  private int x;
  private int y;
  private SpeechAction lastSpeechAction;

  /**
   * Constructor.
   * 
   * @param _id
   *          the id of the agent
   * @param type
   *          the {@link VacuumWorldAgentType} of the agent
   * @param sensors
   *          the number of sensors the agent has
   * @param actuators
   *          the number of actuators the agent has
   * @param direction
   *          that the agent is currently facing
   * @param x
   *          position
   * @param y
   *          position
   */
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
  
  @Override
  public AgentRepresentation clone() {
    AgentRepresentation rep =  new AgentRepresentation(this._id, this.type, this.sensors,
        this.actuators, this.direction, this.x, this.y);
    rep.setClean(this.isClean());
    rep.setSuccessfulClean(this.isSuccessfulClean());
    rep.setLastSpeechAction(this.lastSpeechAction);
    return rep;
  }

  public SpeechAction getLastSpeechAction() {
    return lastSpeechAction;
  }

  public void setLastSpeechAction(SpeechAction lastSpeechAction) {
    this.lastSpeechAction = lastSpeechAction;
  }
}
