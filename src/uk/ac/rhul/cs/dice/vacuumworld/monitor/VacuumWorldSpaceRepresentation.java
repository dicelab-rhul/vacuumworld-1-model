package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class VacuumWorldSpaceRepresentation implements Space , RefinedPerception {

  private int width, height;

  private Map<String, AgentRepresentation> agents = new HashMap<String, AgentRepresentation>();
  private Map<VacuumWorldCoordinates, DirtType> dirts = new HashMap<VacuumWorldCoordinates, DirtType>();

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public Map<String, AgentRepresentation> getAgents() {
    return agents;
  }

  public void setAgents(Map<String, AgentRepresentation> agents) {
    this.agents = agents;
  }

  public Map<VacuumWorldCoordinates, DirtType> getDirts() {
    return dirts;
  }

  public void setDirts(Map<VacuumWorldCoordinates, DirtType> dirts) {
    this.dirts = dirts;
  }
  
  public AgentRepresentation getAgent(String agentId) {
    return this.agents.get(agentId);
  }

}
