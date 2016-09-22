package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import java.util.HashSet;

import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.AgentDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.DirtDatabaseRepresentation;

public class DatabasePerception implements RefinedPerception {

  private HashSet<AgentDatabaseRepresentation> agents;
  private HashSet<DirtDatabaseRepresentation> dirts;
  private Integer lastCycleRead = null;

  public DatabasePerception(HashSet<AgentDatabaseRepresentation> agents,
      HashSet<DirtDatabaseRepresentation> dirts) {
    this.agents = agents;
    this.dirts = dirts;
  }

  public HashSet<AgentDatabaseRepresentation> getAgents() {
    return agents;
  }

  public HashSet<DirtDatabaseRepresentation> getDirts() {
    return dirts;
  }

  public Integer getLastCycleRead() {
    return lastCycleRead;
  }

  public void setLastCycleRead(Integer lastCycleRead) {
    this.lastCycleRead = lastCycleRead;
  }

}
