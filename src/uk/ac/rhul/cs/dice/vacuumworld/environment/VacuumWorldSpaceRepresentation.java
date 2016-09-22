package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverAgent;

/**
 * The representation of {@link VacuumWorldSpace} used by
 * {@link VacuumWorldMonitoringContainer}. It representations only the
 * interesting parts of {@link VacuumWorldSpace} - the
 * {@link VacuumWorldLocation VacuumWorldLocations} that contain either
 * {@link VacuumWorldCleaningAgent VacuumWorldCleaningAgents} or {@link Dirt
 * Dirts} or both, also the dimensions of the space. The purpose of the
 * representation is to give a {@link VWObserverAgent} an effective but
 * minimised view of what is happening in the real {@link VacuumWorldSpace}. The
 * representation is kept in sync by {@link VacuumWorldMonitoringContainer}
 * which will use {@link MonitoringUpdateEvent MonitoringUpdateEvents} sent from
 * {@link VacuumWorldPhysics}.
 * 
 * @author Ben Wilkins
 *
 */
public class VacuumWorldSpaceRepresentation implements Space, RefinedPerception, Cloneable {

  private int width, height;

  private Map<String, AgentRepresentation> agents = new HashMap<String, AgentRepresentation>();
  private Map<VacuumWorldCoordinates, DirtRepresentation> dirts = new HashMap<VacuumWorldCoordinates, DirtRepresentation>();
  // When a dirt is cleaned it should be moved from dirts to this set (allows
  // ease of database updates)
  private Set<DirtRepresentation> removedDirts = new HashSet<DirtRepresentation>();
  
  private ArrayList<SpeechAction> speechActions = new ArrayList<SpeechAction>();
  
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

  public Map<VacuumWorldCoordinates, DirtRepresentation> getDirts() {
    return dirts;
  }

  public void setDirts(Map<VacuumWorldCoordinates, DirtRepresentation> dirts) {
    this.dirts = dirts;
  }

  public AgentRepresentation getAgent(String agentId) {
    return this.agents.get(agentId);
  }

  public Set<DirtRepresentation> getRemovedDirts() {
    return removedDirts;
  }

  public void setRemovedDirts(Set<DirtRepresentation> removedDirts) {
    this.removedDirts = removedDirts;
  }

  /**
   * Should be called when some {@link Dirt} is cleaned. Moves {@link Dirt} from
   * dirts to removedDirts.
   * 
   * @param coordsOfCleanedDirt
   *          coordinates of the cleaned dirt
   */
  public void dirtCleaned(VacuumWorldCoordinates coordsOfCleanedDirt) {
    removedDirts.add(dirts.get(coordsOfCleanedDirt));
    dirts.remove(coordsOfCleanedDirt);
  }
  
  @Override
  public synchronized VacuumWorldSpaceRepresentation clone() {
    VacuumWorldSpaceRepresentation rep =  new VacuumWorldSpaceRepresentation();
    rep.setHeight(this.height);
    rep.setWidth(this.width);
    
    Map<String, AgentRepresentation> agents = new HashMap<String, AgentRepresentation>();
    Map<VacuumWorldCoordinates, DirtRepresentation> dirts = new HashMap<VacuumWorldCoordinates, DirtRepresentation>();
    Set<DirtRepresentation> removedDirts = new HashSet<DirtRepresentation>();
    //clone the maps/set
    Iterator<Entry<String, AgentRepresentation>> agentIter = this.agents.entrySet().iterator();
    while(agentIter.hasNext()) {
      Entry<String, AgentRepresentation> e = agentIter.next();
      agents.put(e.getKey(), e.getValue().clone());
    }
    Iterator<Entry<VacuumWorldCoordinates, DirtRepresentation>> dirtIter = this.dirts.entrySet().iterator();
    while(dirtIter.hasNext()) {
      Entry<VacuumWorldCoordinates, DirtRepresentation> e = dirtIter.next();
      dirts.put(e.getKey().clone(), e.getValue().clone());
    }
    Iterator<DirtRepresentation> rdirtsIter = this.removedDirts.iterator();
    while(rdirtsIter.hasNext()) {
      removedDirts.add(rdirtsIter.next().clone());
    }
    rep.setSpeechActions(new ArrayList<SpeechAction>(speechActions));
    rep.setAgents(agents);
    rep.setDirts(dirts);
    rep.setRemovedDirts(removedDirts);
    return rep;
  }

  public ArrayList<SpeechAction> getSpeechActions() {
    return speechActions;
  }

  public void setSpeechActions(ArrayList<SpeechAction> speechActions) {
    this.speechActions = speechActions;
  }

}
