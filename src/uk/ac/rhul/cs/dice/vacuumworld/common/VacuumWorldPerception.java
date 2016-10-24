package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldPerception implements Perception {
	private Map<VacuumWorldCoordinates, VacuumWorldLocation> perception;
	private VacuumWorldCoordinates actorCoordinates;
	
	public VacuumWorldPerception(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, VacuumWorldCoordinates actorCoordinates) {
		this.perception = perception != null ? perception : new HashMap<>();
		this.actorCoordinates = actorCoordinates;
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
	
	public VacuumWorldCoordinates getActorCoordinates() {
		return this.actorCoordinates;
	}
	
	public VacuumWorldLocation getActorCurentLocation() {
		return this.perception.get(this.actorCoordinates);
	}
	
	public ActorFacingDirection getActorCurrentFacingDirection() {
		if(getActorCurentLocation().isAnAgentPresent()) {
			return getActorCurentLocation().getAgent().getFacingDirection();
		}
		else if(getActorCurentLocation().isAUserPresent()) {
			return getActorCurentLocation().getUser().getFacingDirection();
		}
		else {
			return null;
		}
	}
	
	public boolean isDirtOnActorCurrentLocation() {
		return getActorCurentLocation().isDirtPresent();
	}
	
	public VacuumWorldAgentType getAgentType() {
		if(!getActorCurentLocation().isAnAgentPresent()) {
			return null;
		}
		
		return getActorCurentLocation().getAgent().getExternalAppearance().getType();
	}
	
	public boolean canAgentClean() {
		if(!isDirtOnActorCurrentLocation() || !getActorCurentLocation().isAnAgentPresent()) {
			return false;
		}
		
		DirtType dirtType = getActorCurentLocation().getDirt().getExternalAppearance().getDirtType();
		
		return DirtType.agentAndDirtCompatible(dirtType, getAgentType());
	}
	
	public boolean canAgentSpotCompatibleDirt() {
		if(!getActorCurentLocation().isAnAgentPresent()) {
			return false;
		}
		
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
		if(!getActorCurentLocation().isAnAgentPresent()) {
			return false;
		}
		
		DirtType dirtType = dirt.getExternalAppearance().getDirtType();
		
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
	
	public List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerception(String actorId) {
		List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> actors = new ArrayList<>();
		
		this.perception.values().forEach((VacuumWorldLocation location) -> addActorToActorsListIfNecessary(actors, location, actorId));
		
		return actors;
	}

	private void addActorToActorsListIfNecessary(List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> actors, VacuumWorldLocation location, String actorId) {
		if(!location.isAnAgentPresent()) {
			return;
		}
		
		if(!actorId.equals(location.getAgent().getId())) {
			actors.add(location.getAgent());
		}
	}
}