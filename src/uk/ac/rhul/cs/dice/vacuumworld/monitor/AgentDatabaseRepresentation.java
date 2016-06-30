package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public class AgentDatabaseRepresentation {

  private String _id;
  private VacuumWorldAgentType type;
  private int sensors;
  private int actuators;
  private int totalCleans;
  private int successfulCleans;
  private CycleDataRepresentation[] cycleList;

  public AgentDatabaseRepresentation(String _id, VacuumWorldAgentType type,
      int sensors, int actuators, int totalCleans, int successfulCleans,
      CycleDataRepresentation[] cycleList) {
    super();
    this._id = _id;
    this.type = type;
    this.sensors = sensors;
    this.actuators = actuators;
    this.totalCleans = totalCleans;
    this.successfulCleans = successfulCleans;
    this.cycleList = cycleList;
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

  public int getTotalCleans() {
    return totalCleans;
  }

  public void setTotalCleans(int totalCleans) {
    this.totalCleans = totalCleans;
  }

  public int getSuccessfulCleans() {
    return successfulCleans;
  }

  public void setSuccessfulCleans(int successfulCleans) {
    this.successfulCleans = successfulCleans;
  }

  public CycleDataRepresentation[] getCycleList() {
    return cycleList;
  }

  public void setCycleList(CycleDataRepresentation[] cycleList) {
    this.cycleList = cycleList;
  }

}
