package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldPerception implements Perception {
	private Map<VacuumWorldCoordinates, VacuumWorldLocation> perception;
	private VacuumWorldCoordinates agentCoordinates;
	
	public VacuumWorldPerception(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, VacuumWorldCoordinates agentCoordinates) {
		this.perception = perception != null ? perception : new HashMap<>();
		this.agentCoordinates = agentCoordinates;
	}
	
	public void addPerceivedLocation(VacuumWorldCoordinates coordinates, VacuumWorldLocation location) {
		this.perception.put(coordinates, location);
	}
	
	public void removePerceivedLocation(VacuumWorldCoordinates coordinates) {
		this.perception.remove(coordinates);
	}
	
	public Map<VacuumWorldCoordinates, VacuumWorldLocation> getPerceivedMap() {
		return this.perception;
	}
	
	public void replacePerceivedMap(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception) {
		this.perception = perception;
	}
	
	public VacuumWorldCoordinates getAgentCoordinates() {
		return this.agentCoordinates;
	}
	
	public VacuumWorldLocation getAgentCurentLocation() {
		return this.perception.get(this.agentCoordinates);
	}
	
	public AgentFacingDirection getAgentCurrentFacingDirection() {
		return getAgentCurentLocation().getAgent().getFacingDirection();
	}
	
	public boolean isDirtOnAgentCurrentLocation() {
		return getAgentCurentLocation().isDirtPresent();
	}
	
	public VacuumWorldAgentType getAgentType() {
		return ((VacuumWorldAgentAppearance) getAgentCurentLocation().getAgent().getExternalAppearance()).getType();
	}
	
	public boolean canAgentClean() {
		if(!isDirtOnAgentCurrentLocation()) {
			return false;
		}
		
		DirtType dirtType = ((DirtAppearance) getAgentCurentLocation().getDirt().getExternalAppearance()).getDirtType();
		
		return DirtType.agentAndDirtCompatible(dirtType, getAgentType());
	}
	
	public boolean canAgentSpotCompatibleDirt() {
		for(VacuumWorldLocation location : this.perception.values()) {
			if(isCompatibleDirtPresent(location)) {
				return true;
			}
			else {
				continue;
			}
		}
		
		return false;
	}

	private boolean isCompatibleDirtPresent(VacuumWorldLocation location) {
		if(!location.isDirtPresent()) {
			return false;
		}
		else {
			return areAgentAndDirtCompatible(location.getDirt());
		}
	}

	private boolean areAgentAndDirtCompatible(Dirt dirt) {
		DirtType dirtType = ((DirtAppearance) dirt.getExternalAppearance()).getDirtType();
		
		return DirtType.agentAndDirtCompatible(dirtType, getAgentType());
	}
	
	public List<VacuumWorldLocation> getLocationsWithCompatibleDirt() {
		List<VacuumWorldLocation> locationsWithCompatibleDirt = new ArrayList<>();
		
		for(VacuumWorldLocation location : this.perception.values()) {
			if(isCompatibleDirtPresent(location)) {
				locationsWithCompatibleDirt.add(location);
			}
			else {
				continue;
			}
		}
		
		return locationsWithCompatibleDirt;
	}
	
	public List<VacuumWorldCleaningAgent> getAgentsInPerception(String agentId) {
		List<VacuumWorldCleaningAgent> agents = new ArrayList<>();
		
		this.perception.values().forEach((VacuumWorldLocation location) -> addAgentToAgentsListIfNecessary(agents, location, agentId));
		
		return agents;
	}

	private void addAgentToAgentsListIfNecessary(List<VacuumWorldCleaningAgent> agents, VacuumWorldLocation location, String agentId) {
		if(!location.isAnAgentPresent()) {
			return;
		}
		
		if(!agentId.equals(location.getAgent().getId())) {
			agents.add(location.getAgent());
		}
	}
}