package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.ArrayList;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public class AgentAwarenessRepresentation {
  private String myid;
  private ArrayList<String> myactuatorids;
  private ArrayList<String> myEars;
  private ArrayList<String> myEyes;
  private VacuumWorldAgentType type;

  public AgentAwarenessRepresentation(String myid,
      ArrayList<String> myactuatorids, ArrayList<String> myEars,
      ArrayList<String> myEyes, VacuumWorldAgentType type) {
    super();
    this.myid = myid;
    this.myactuatorids = myactuatorids;
    this.myEars = myEars;
    this.myEyes = myEyes;
    this.type = type;
  }

  public String getMyid() {
    return myid;
  }

  public void setMyid(String myid) {
    this.myid = myid;
  }

  public ArrayList<String> getMyactuatorids() {
    return myactuatorids;
  }

  public void setActuatorid(String myactuatorid) {
    this.myactuatorids.add(myactuatorid);
  }

  public ArrayList<String> getMyEars() {
    return myEars;
  }

  public void setMyEars(ArrayList<String> myEars) {
    this.myEars = myEars;
  }

  public ArrayList<String> getMyEyes() {
    return myEyes;
  }

  public void setMyEyes(ArrayList<String> myEyes) {
    this.myEyes = myEyes;
  }

  public VacuumWorldAgentType getType() {
    return type;
  }

  public void setType(VacuumWorldAgentType type) {
    this.type = type;
  }
}
