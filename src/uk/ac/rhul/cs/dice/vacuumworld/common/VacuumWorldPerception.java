package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;

/**
 * 
 * This class wraps the a {@link Map} from {@link VacuumWorldCoordinates} to {@link VacuumWorldLocation} which a subset of the environment grid.<br/><br/>
 * The map represents the perception of an actor.<br/><br/>
 * This class exposes an API to access various elements in the {@link VacuumWorldPerception}.<br/><br/>
 * Some of the public methods of the API are deprecated and left only for back compatibility. When a better alternative exists, it is signaled.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana.
 *
 */
public class VacuumWorldPerception implements VWPerception {
    private Map<VacuumWorldCoordinates, VacuumWorldLocation> perception;
    private VacuumWorldCoordinates actorCoordinates;

    /**
     * 
     * Constructor with the perceived {@link Map} and the current actor {@link VacuumWorldCoordinates}.
     * 
     * @param perception the perceived {@link Map}.
     * @param actorCoordinates the current actor {@link VacuumWorldCoordinates}.
     * 
     */
    public VacuumWorldPerception(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, VacuumWorldCoordinates actorCoordinates) {
	this.perception = perception != null ? perception : new HashMap<>();
	this.actorCoordinates = actorCoordinates;
    }

    /**
     * 
     * @deprecated This is NOT FOR STUDENTS' USE. Adds an entry to the perceived {@link Map}.
     * 
     * @param coordinates the key.
     * @param location the value.
     * 
     */
    @Deprecated
    public void addPerceivedLocation(VacuumWorldCoordinates coordinates, VacuumWorldLocation location) {
	this.perception.put(coordinates, location);
    }

    /**
     * 
     * @deprecated This is NOT FOR STUDENTS' USE. Removes an entry from the perceived {@link Map}.
     * 
     * @param coordinates the key.
     * @param location the value.
     * 
     */
    @Deprecated
    public void removePerceivedLocation(VacuumWorldCoordinates coordinates) {
	this.perception.remove(coordinates);
    }

    @Override
    public Map<VacuumWorldCoordinates, VacuumWorldLocation> getPerceivedMap() {
	return this.perception;
    }

    /**
     * 
     * @deprecated This is NOT FOR STUDENTS' USE. Replaces the perceived {@link Map}.
     * 
     * @param perception the new perceived {@link Map}.
     * 
     */
    @Deprecated
    public void replacePerceivedMap(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception) {
	this.perception = perception;
    }

    @Override
    public VacuumWorldCoordinates getActorCoordinates() {
	return this.actorCoordinates;
    }

    /**
     * 
     * @deprecated Use {@link #getCurrentActorLocation()}.
     * 
     */
    @Deprecated
    public VacuumWorldLocation getActorCurentLocation() {
	return this.perception.get(this.actorCoordinates);
    }
    
    @Override
    public VacuumWorldLocation getCurrentActorLocation() {
	return this.perception.get(this.actorCoordinates);
    }

    @Override
    public ActorFacingDirection getActorCurrentFacingDirection() {
	if (getCurrentActorLocation().isAnAgentPresent()) {
	    return getCurrentActorLocation().getAgent().getFacingDirection();
	} 
	else if (getCurrentActorLocation().isAUserPresent()) {
	    return getCurrentActorLocation().getUser().getFacingDirection();
	} 
	else {
	    return null;
	}
    }

    @Override
    public boolean isDirtOnCurrentActorLocation() {
	return getCurrentActorLocation().isDirtPresent();
    }
    
    /**
     * 
     * @deprecated Use {@link #isDirtOnCurrentActorLocation()}.
     * 
     */
    public boolean isDirtOnActorCurrentLocation() {
	return getActorCurentLocation().isDirtPresent();
    }

    /**
     * 
     * @deprecated Use {@link #getCurrentActorColorIfAgent()}.
     * 
     */
    @Deprecated
    public VacuumWorldAgentType getAgentType() {
	if (!getActorCurentLocation().isAnAgentPresent()) {
	    return null;
	}

	return getActorCurentLocation().getAgent().getExternalAppearance().getType();
    }

    /**
     * 
     * @deprecated Use {@link #canCurrentActorCleanOnHisCurrentLocation()}.
     * 
     */
    @Deprecated
    public boolean canAgentClean() {
	if (!isDirtOnActorCurrentLocation() || !getActorCurentLocation().isAnAgentPresent()) {
	    return false;
	}

	DirtType dirtType = getActorCurentLocation().getDirt().getExternalAppearance().getDirtType();

	return DirtType.agentAndDirtCompatible(dirtType, getAgentType());
    }

    /**
     * 
     * @deprecated Use {@link #canCurrentActorSpotCompatibleDirt()}.
     * 
     */
    @Deprecated
    public boolean canAgentSpotCompatibleDirt() {
	if (!getActorCurentLocation().isAnAgentPresent()) {
	    return false;
	}

	for (VacuumWorldLocation location : this.perception.values()) {
	    if (isCompatibleDirtPresent(location)) {
		return true;
	    }
	}

	return false;
    }

    private boolean isCompatibleDirtPresent(VacuumWorldLocation location) {
	if (!location.isDirtPresent()) {
	    return false;
	}
	else {
	    return areAgentAndDirtCompatible(location.getDirt());
	}
    }

    private boolean areAgentAndDirtCompatible(Dirt dirt) {
	if (!getCurrentActorLocation().isAnAgentPresent()) {
	    return false;
	}

	DirtType dirtType = dirt.getExternalAppearance().getDirtType();

	return DirtType.agentAndDirtCompatible(dirtType, getCurrentActorColorIfAgent());
    }

    /**
     * 
     * @deprecated Use {@link #getLocationsWithDirtCompatibleWithCurrentActor()}.
     * 
     */
    @Deprecated
    public List<VacuumWorldLocation> getLocationsWithCompatibleDirt() {
	List<VacuumWorldLocation> locationsWithCompatibleDirt = new ArrayList<>();

	for (VacuumWorldLocation location : this.perception.values()) {
	    if (isCompatibleDirtPresent(location)) {
		locationsWithCompatibleDirt.add(location);
	    }
	}

	return locationsWithCompatibleDirt;
    }

    /**
     * 
     * @deprecated Use {@link #getActorsInPerception()}.
     * 
     */
    @Deprecated
    public List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerception(String actorId) {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent() || location.isAUserPresent()).map(location -> location.isAnAgentPresent() ? location.getAgent() : location.isAUserPresent() ? location.getUser() : null).collect(Collectors.toList());
    }
    
    @Override
    public List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent() || location.isAUserPresent()).map(location -> location.isAnAgentPresent() ? location.getAgent() : location.isAUserPresent() ? location.getUser() : null).collect(Collectors.toList());
    }

    @Override
    public int getPerceptionSize() {
	return this.perception.size();
    }

    @Override
    public List<VacuumWorldCoordinates> getCoordinatesInPerceptionList() {
	return new ArrayList<>(this.perception.keySet());
    }

    @Override
    public List<VacuumWorldLocation> getLocationsInPerceptionList() {
	return new ArrayList<>(this.perception.values());
    }

    @Override
    public VacuumWorldLocation getSpecificPerceivedLocation(VacuumWorldCoordinates coordinates) {
	return this.perception.get(coordinates);
    }

    @Override
    public VacuumWorldAgentType getCurrentActorColorIfAgent() {
	if(!getCurrentActorLocation().isAnAgentPresent()) {
	    return null;
	}
	else {
	    return getCurrentActorLocation().getAgent().getExternalAppearance().getType();
	}
    }

    @Override
    public boolean canCurrentActorCleanOnHisCurrentLocation() {
	if (!isDirtOnCurrentActorLocation() || !getCurrentActorLocation().isAnAgentPresent()) {
	    return false;
	}

	DirtType dirtType = getCurrentActorLocation().getDirt().getExternalAppearance().getDirtType();

	return DirtType.agentAndDirtCompatible(dirtType, getCurrentActorColorIfAgent());
    }

    @Override
    public List<VacuumWorldLocation> getLocationsWithDirtCompatibleWithCurrentActor() {
	return this.perception.values().stream().filter(this::isCompatibleDirtPresent).collect(Collectors.toList());
    }

    @Override
    public List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerceptionIncludingSelf() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent() || location.isAUserPresent()).map(location -> location.isAnAgentPresent() ? location.getAgent() : location.isAUserPresent() ? location.getUser() : null).collect(Collectors.toList());
    }

    @Override
    public List<String> getActorsIdsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent() || location.isAUserPresent()).map(location -> location.isAnAgentPresent() ? location.getAgent().getId() : location.isAUserPresent() ? location.getUser().getId() : null).collect(Collectors.toList());
    }

    @Override
    public List<String> getActorsIdsInPerceptionIncludingSelf() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent() || location.isAUserPresent()).map(location -> location.isAnAgentPresent() ? location.getAgent().getId() : location.isAUserPresent() ? location.getUser().getId() : null).collect(Collectors.toList());
    }

    @Override
    public List<Dirt> getDirtsInPerception() {
	return this.perception.values().stream().filter(location -> location.isDirtPresent()).map(location -> location.getDirt()).collect(Collectors.toList());
    }

    @Override
    public List<Dirt> getDirtsOfSpecificTypeInPerception(DirtType type) {
	return this.perception.values().stream().filter(location -> location.isDirtPresent()).filter(location -> type.equals(location.getDirt().getExternalAppearance().getDirtType())).map(location -> location.getDirt()).collect(Collectors.toList());
    }

    @Override
    public int countDirtsInPerception() {
	return (int) this.perception.values().stream().filter(location -> location.isDirtPresent()).count();
    }

    @Override
    public int countDirtsOfSpecificTypeInPerception(DirtType type) {
	return (int) this.perception.values().stream().filter(location -> location.isDirtPresent()).filter(location -> type.equals(location.getDirt().getExternalAppearance().getDirtType())).count();
    }

    @Override
    public boolean doesCurrentActorHaveWallInFront() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getNeighborLocationType(getActorCurrentFacingDirection()));
    }

    @Override
    public boolean doesCurrentActorHaveWallOnHisLeft() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getNeighborLocationType(getActorCurrentFacingDirection().getLeftDirection()));
    }

    @Override
    public boolean doesCurrentActorHaveWallOnHisRight() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getNeighborLocationType(getActorCurrentFacingDirection().getRightDirection()));
    }

    @Override
    public boolean doesCurrentActorHaveWallOnHisBack() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getNeighborLocationType(getActorCurrentFacingDirection().getOppositeDirection()));
    }

    @Override
    public boolean doesCurrentActorHaveWallOnNorth() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getNorthernLocationType());
    }

    @Override
    public boolean doesCurrentActorHaveWallOnSouth() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getSouthernLocationType());
    }

    @Override
    public boolean doesCurrentActorHaveWallOnWest() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getWesternLocationType());
    }

    @Override
    public boolean doesCurrentActorHaveWallOnEast() {
	return VacuumWorldLocationType.WALL.equals(getCurrentActorLocation().getEasternLocationType());
    }

    @Override
    public boolean canCurrentActorSpotCompatibleDirt() {
	if (!getCurrentActorLocation().isAnAgentPresent()) {
	    return false;
	}

	return this.perception.values().stream().anyMatch(this::isCompatibleDirtPresent);
    }

    @Override
    public List<Dirt> getCompatibleDirtsInPerception() {
	return this.perception.values().stream().filter(location -> location.isDirtPresent()).filter(location -> DirtType.agentAndDirtCompatible(location.getDirt().getExternalAppearance().getDirtType(), getCurrentActorColorIfAgent())).map(location -> location.getDirt()).collect(Collectors.toList());
    }

    @Override
    public int countCompatibleDirtsInPerception() {
	return (int) this.perception.values().stream().filter(location -> location.isDirtPresent()).filter(location -> DirtType.agentAndDirtCompatible(location.getDirt().getExternalAppearance().getDirtType(), getCurrentActorColorIfAgent())).count();
    }

    @Override
    public List<VacuumWorldCleaningAgent> getGreenAgentsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.GREEN.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<VacuumWorldCleaningAgent> getGreenAgentsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.GREEN.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<String> getGreenAgentsIdsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.GREEN.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public List<String> getGreenAgentsIdsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.GREEN.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public List<VacuumWorldCleaningAgent> getOrangeAgentsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.ORANGE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<VacuumWorldCleaningAgent> getOrangeAgentsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.ORANGE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<String> getOrangeAgentsIdsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.ORANGE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public List<String> getOrangeAgentsIdsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.ORANGE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public List<VacuumWorldCleaningAgent> getWhiteAgentsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.WHITE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<VacuumWorldCleaningAgent> getWhiteAgentsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.WHITE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent()).collect(Collectors.toList());
    }

    @Override
    public List<String> getWhiteAgentsIdsInPerception() {
	return this.perception.values().stream().filter(location -> !location.getCoordinates().equals(getActorCoordinates())).filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.WHITE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public List<String> getWhiteAgentsIdsInPerceptionIncludingSelfIfApplicable() {
	return this.perception.values().stream().filter(location -> location.isAnAgentPresent()).filter(location -> VacuumWorldAgentType.WHITE.equals(location.getAgent().getExternalAppearance().getType())).map(location -> location.getAgent().getId()).collect(Collectors.toList());
    }

    @Override
    public User getUserInPerceptionIfPresent() {
	if(getCurrentActorLocation().isAUserPresent()) {
	    return getCurrentActorLocation().getUser();
	}
	else {
	    return null;
	}
    }

    @Override
    public String getUserIdInPerceptionIfPresent() {
	if(getCurrentActorLocation().isAUserPresent()) {
	    return getCurrentActorLocation().getUser().getId();
	}
	else {
	    return null;
	}
    }

    @Override
    public boolean isCurrentActorAGreenAgent() {
	if(getCurrentActorLocation().isAnAgentPresent()) {
	    return VacuumWorldAgentType.GREEN.equals(getCurrentActorLocation().getAgent().getExternalAppearance().getType());
	}
	else {
	    return false;
	}
    }

    @Override
    public boolean isCurrentActorAnOrangeAgent() {
	if(getCurrentActorLocation().isAnAgentPresent()) {
	    return VacuumWorldAgentType.ORANGE.equals(getCurrentActorLocation().getAgent().getExternalAppearance().getType());
	}
	else {
	    return false;
	}
    }

    @Override
    public boolean isCurrentActorAWhiteAgent() {
	if(getCurrentActorLocation().isAnAgentPresent()) {
	    return VacuumWorldAgentType.WHITE.equals(getCurrentActorLocation().getAgent().getExternalAppearance().getType());
	}
	else {
	    return false;
	}
    }

    @Override
    public boolean isCurrentActorAUser() {
	return getCurrentActorLocation().isAUserPresent();
    }

    @Override
    public AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> getCurentActor() {
	if(getCurrentActorLocation().isAUserPresent()) {
	    return getCurrentActorLocation().getUser();
	}
	else if (getCurrentActorLocation().isAnAgentPresent()){
	    return getCurrentActorLocation().getAgent();
	}
	else {
	    return null;
	}
    }

    @Override
    public String getCurentActorId() {
	if(getCurrentActorLocation().isAUserPresent()) {
	    return getCurrentActorLocation().getUser().getId();
	}
	else if (getCurrentActorLocation().isAnAgentPresent()){
	    return getCurrentActorLocation().getAgent().getId();
	}
	else {
	    return null;
	}
    }
}