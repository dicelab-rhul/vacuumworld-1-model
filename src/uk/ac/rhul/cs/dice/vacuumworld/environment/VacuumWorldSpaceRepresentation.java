package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * minimized view of what is happening in the real {@link VacuumWorldSpace}. The
 * representation is kept in sync by {@link VacuumWorldMonitoringContainer}
 * which will use {@link MonitoringUpdateEvent MonitoringUpdateEvents} sent from
 * {@link VacuumWorldPhysics}.
 * 
 * @author Ben Wilkins
 *
 */
public class VacuumWorldSpaceRepresentation implements Space, RefinedPerception {
	private int width;
	private int height;

	private Map<String, AgentRepresentation> agents = new HashMap<>();
	private Map<VacuumWorldCoordinates, DirtRepresentation> dirts = new HashMap<>();
	private Set<DirtRepresentation> removedDirts = new HashSet<>();
	private List<SpeechAction> speechActions = new ArrayList<>();

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Map<String, AgentRepresentation> getAgents() {
		return this.agents;
	}

	public void setAgents(Map<String, AgentRepresentation> agents) {
		this.agents = agents;
	}

	public Map<VacuumWorldCoordinates, DirtRepresentation> getDirts() {
		return this.dirts;
	}

	public void setDirts(Map<VacuumWorldCoordinates, DirtRepresentation> dirts) {
		this.dirts = dirts;
	}

	public AgentRepresentation getAgent(String agentId) {
		return this.agents.get(agentId);
	}

	public Set<DirtRepresentation> getRemovedDirts() {
		return this.removedDirts;
	}

	public void setRemovedDirts(Set<DirtRepresentation> removedDirts) {
		this.removedDirts = removedDirts;
	}

	/**
	 * Should be called when some {@link Dirt} is cleaned. Moves {@link Dirt}
	 * from dirts to removedDirts.
	 * 
	 * @param coordsOfCleanedDirt
	 *            coordinates of the cleaned dirt
	 */
	public void dirtCleaned(VacuumWorldCoordinates coordsOfCleanedDirt) {
		this.removedDirts.add(this.dirts.get(coordsOfCleanedDirt));
		this.dirts.remove(coordsOfCleanedDirt);
	}

	public synchronized VacuumWorldSpaceRepresentation replicate() {
		VacuumWorldSpaceRepresentation replica = new VacuumWorldSpaceRepresentation();
		replica.setHeight(this.height);
		replica.setWidth(this.width);
		replica.setSpeechActions(new ArrayList<SpeechAction>(this.speechActions));
		replica.setAgents(duplicateAgentsMap());
		replica.setDirts(duplicateDirtsMap());
		replica.setRemovedDirts(duplicateRemovedDirts());
		
		return replica;
	}

	private Set<DirtRepresentation> duplicateRemovedDirts() {
		Set<DirtRepresentation> removedDirtsCopy = new HashSet<>();
		
		for(DirtRepresentation dirt : this.removedDirts) {
			removedDirtsCopy.add(dirt.duplicate());
		}
		
		return removedDirtsCopy;
	}

	private Map<VacuumWorldCoordinates, DirtRepresentation> duplicateDirtsMap() {
		Map<VacuumWorldCoordinates, DirtRepresentation> dirtsCopy = new HashMap<>();
		
		for(Entry<VacuumWorldCoordinates, DirtRepresentation> entry : this.dirts.entrySet()) {
			dirtsCopy.put(entry.getKey().duplicate(), entry.getValue().duplicate());
		}
		
		return dirtsCopy;
	}

	private Map<String, AgentRepresentation> duplicateAgentsMap() {
		Map<String, AgentRepresentation> agentsCopy = new HashMap<>();
		
		for(Entry<String, AgentRepresentation> entry : this.agents.entrySet()) {
			agentsCopy.put(entry.getKey(), entry.getValue().duplicate());
		}
		
		return agentsCopy;
	}

	public List<SpeechAction> getSpeechActions() {
		return this.speechActions;
	}

	public void setSpeechActions(List<SpeechAction> speechActions) {
		this.speechActions = speechActions;
	}
}