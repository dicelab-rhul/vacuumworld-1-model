package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechPerceptionResultWrapper;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserSensor;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldSpace extends EnvironmentalSpace {
	private int[] dimensions;
	private static final String BAD_DIRECTION = "Bad agent direction: ";
	private Map<String, VacuumWorldCleaningAgent> agents;
	private JsonObject initialStateRepresentation;
	
	private User user;
	
	public VacuumWorldSpace(int[] dimensions, Map<VacuumWorldCoordinates, VacuumWorldLocation> grid, User user) {
		super(checkedCast(grid));
		
		this.user = user;
		this.dimensions = dimensions;
		this.agents = new HashMap<>();
		
		fillAgentsMap();
	}
	
	@SuppressWarnings("unchecked")
	private static Map<LocationKey, Location> checkedCast(Map<? extends LocationKey, ? extends Location> grid) {
		for(Entry<? extends LocationKey, ? extends Location> entry : grid.entrySet()) {
			if(!LocationKey.class.isAssignableFrom(entry.getKey().getClass()) || !Location.class.isAssignableFrom(entry.getValue().getClass())) {
				return new HashMap<>();
			}
		}
		
		return (Map<LocationKey, Location>) grid;
	}

	private void fillAgentsMap() {
		for (VacuumWorldLocation location : getAllLocations()) {
			if (location.isAnAgentPresent()) {
				VacuumWorldCleaningAgent a = location.getAgent();
				this.agents.put(a.getId(), a);
			}
		}
	}
	
	public void setJsonRepresentation(JsonObject representation) {
		this.initialStateRepresentation = representation;
	}
	
	public JsonObject getInitialStateRepresentation() {
		return this.initialStateRepresentation;
	}

	public boolean isUserPresent() {
		return this.user != null;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public int[] getDimensions() {
		return this.dimensions;
	}

	public Map<String, VacuumWorldCleaningAgent> getAgentsMap() {
		return this.agents;
	}

	public VacuumWorldCleaningAgent getAgentById(String id) {
		return this.agents.get(id);
	}
	
	public Set<VacuumWorldCleaningAgent> getAgents() {
		return new HashSet<>(this.agents.values());
	}
	
	public Map<VacuumWorldCoordinates, VacuumWorldLocation> getFullGrid() {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> toReturn = new HashMap<>();
		
		for(Entry<LocationKey, Location> entry : super.getGrid().entrySet()) {
			if(entry.getKey() instanceof VacuumWorldCoordinates && entry.getValue() instanceof VacuumWorldLocation) {
				toReturn.put((VacuumWorldCoordinates) entry.getKey(), (VacuumWorldLocation) entry.getValue());
			}
		}
		
		return toReturn;
	}
	
	public Collection<VacuumWorldLocation> getAllLocations() {
		return getFullGrid().values();
	}

	@Override
	public VacuumWorldLocation getLocation(LocationKey locationKey) {
		return (VacuumWorldLocation) super.getLocation(locationKey);
	}
	
	public VacuumWorldLocation getNorthernLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x, y - 1));
	}

	public VacuumWorldLocation getNorthernLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getNorthernCoordinates());
	}

	public VacuumWorldLocation getSouthernLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x, y + 1));
	}

	public VacuumWorldLocation getSouthernLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getSouthernCoordinates());
	}

	public VacuumWorldLocation getWesternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x - 1, y));
	}

	public VacuumWorldLocation getWesternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getWesternCoordinates());
	}

	public VacuumWorldLocation getEasternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x + 1, y));
	}

	public VacuumWorldLocation getEasternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getEasternCoordinates());
	}

	public VacuumWorldLocation getNorthWesternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x - 1, y - 1));
	}

	public VacuumWorldLocation getNorthWesternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getNorthWesternCoordinates());
	}

	public VacuumWorldLocation getNorthEasternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x + 1, y - 1));
	}

	public VacuumWorldLocation getNorthEasternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getNorthEasternCoordinates());
	}

	public VacuumWorldLocation getSouthWesternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x - 1, y + 1));
	}

	public VacuumWorldLocation getSouthWesternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getSouthWesternCoordinates());
	}

	public VacuumWorldLocation getSouthEasternLocation(int x, int y) {
		return this.getLocation(new VacuumWorldCoordinates(x + 1, y + 1));
	}

	public VacuumWorldLocation getSouthEasternLocation(VacuumWorldCoordinates coordinates) {
		return this.getLocation(coordinates.getSouthEasternCoordinates());
	}

	public VacuumWorldLocation getFrontLocation(VacuumWorldCoordinates coordinates, ActorFacingDirection agentDirection) {
		switch (agentDirection) {
		case NORTH:
			return getNorthernLocation(coordinates);
		case SOUTH:
			return getSouthernLocation(coordinates);
		case WEST:
			return getWesternLocation(coordinates);
		case EAST:
			return getEasternLocation(coordinates);
		default:
			throw new IllegalArgumentException(BAD_DIRECTION + agentDirection);
		}
	}

	public VacuumWorldLocation getFrontLeftLocation(VacuumWorldCoordinates coordinates, ActorFacingDirection agentDirection) {
		switch (agentDirection) {
		case NORTH:
			return getNorthWesternLocation(coordinates);
		case SOUTH:
			return getSouthEasternLocation(coordinates);
		case WEST:
			return getSouthWesternLocation(coordinates);
		case EAST:
			return getNorthEasternLocation(coordinates);
		default:
			throw new IllegalArgumentException(BAD_DIRECTION + agentDirection);
		}
	}

	public VacuumWorldLocation getFrontRightLocation(VacuumWorldCoordinates coordinates, ActorFacingDirection agentDirection) {
		switch (agentDirection) {
		case NORTH:
			return getNorthEasternLocation(coordinates);
		case SOUTH:
			return getSouthWesternLocation(coordinates);
		case WEST:
			return getNorthWesternLocation(coordinates);
		case EAST:
			return getSouthEasternLocation(coordinates);
		default:
			throw new IllegalArgumentException(BAD_DIRECTION + agentDirection);
		}
	}

	public VacuumWorldLocation getLeftLocation(VacuumWorldCoordinates coordinates, ActorFacingDirection agentDirection) {
		switch (agentDirection) {
		case NORTH:
			return getWesternLocation(coordinates);
		case SOUTH:
			return getEasternLocation(coordinates);
		case WEST:
			return getSouthernLocation(coordinates);
		case EAST:
			return getNorthernLocation(coordinates);
		default:
			throw new IllegalArgumentException(BAD_DIRECTION + agentDirection);
		}
	}

	public VacuumWorldLocation getRightLocation(VacuumWorldCoordinates coordinates, ActorFacingDirection agentDirection) {
		switch (agentDirection) {
		case NORTH:
			return getEasternLocation(coordinates);
		case SOUTH:
			return getWesternLocation(coordinates);
		case WEST:
			return getNorthernLocation(coordinates);
		case EAST:
			return getSouthernLocation(coordinates);
		default:
			throw new IllegalArgumentException(BAD_DIRECTION + agentDirection);
		}
	}

	@Override
	public void update(CustomObservable o, Object arg) {
	    if ((o instanceof VacuumWorldDefaultActuator || o instanceof UserActuator) && arg instanceof VacuumWorldEvent) {
		manageActuatorRequest((VacuumWorldEvent) arg);
	    }
	    else if (o instanceof VacuumWorldPhysics) {
		managePhysicsRequest(arg);
	    }
	}
	
	private void managePhysicsRequest(Object arg) {
	    if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass())) {
		managePhysicsRequest((VacuumWorldActionResult) arg);
	    } 
	    else if (VacuumWorldSpeechPerceptionResultWrapper.class.isAssignableFrom(arg.getClass())) {
		manageSpeechActionResult((VacuumWorldSpeechPerceptionResultWrapper) arg);
	    }
	}

	private void manageSpeechActionResult(VacuumWorldSpeechPerceptionResultWrapper wrapper) {
	    List<String> recipientActorsIds = wrapper.getSpeechResult().getRecipientsIds();
	    List<String> senderSensorIds = wrapper.getRecipientsIds();
	    
	    logResult(wrapper.getPerceptionResult());
	    
	    notifyActor(senderSensorIds, wrapper.getPerceptionResult());
	    notifyTargets(recipientActorsIds, wrapper.getSpeechResult());
	}

	private void notifyActor(List<String> senderSensorIds, VacuumWorldActionResult result) {
	    notifyObserversIfNeeded(senderSensorIds, result);
	}
	
	private void notifyTargets(List<String> recipientActorsIds, VacuumWorldSpeechActionResult result) {
	    List<VacuumWorldCleaningAgent> recipientAgents = this.agents.values().stream().filter(agent -> recipientActorsIds.contains(agent.getId())).collect(Collectors.toList());
	    recipientAgents.forEach(agent -> notifyListeningSensors(agent, result));
	    
	    if(this.user != null) {
		notifyUserIfNecessary(result, recipientActorsIds);
	    }
	}

	private void notifyUserIfNecessary(VacuumWorldSpeechActionResult result, List<String> recipientActorsIds) {
	    if(recipientActorsIds.contains(this.user.getId())) {
		notifyListeningSensors(this.user, result);
	    }
	}

	private void notifyListeningSensors(VacuumWorldCleaningAgent agent, VacuumWorldSpeechActionResult result) {
	    agent.getListeningSensors().forEach(sensor -> sensor.update(this, result));
	}
	
	private void notifyListeningSensors(User user, VacuumWorldSpeechActionResult result) {
	    user.getListeningSensors().forEach(sensor -> sensor.update(this, result));
	}

	private void notifyObserversIfNeeded(List<String> sensorsToNotifyIds, VacuumWorldActionResult result) {
		List<CustomObserver> recipients = this.getObservers();
		
		for(CustomObserver recipient : recipients) {
			if(recipient instanceof VacuumWorldDefaultSensor) {
				notifyAgentSensorIfNeeded((VacuumWorldDefaultSensor) recipient, sensorsToNotifyIds, result);
			}
			else if(recipient instanceof UserSensor) {
				notifyUserSensorIfNeeded((UserSensor) recipient, sensorsToNotifyIds, result);
			}
		}
	}

	private void notifyUserSensorIfNeeded(UserSensor recipient, List<String> sensorsToNotifyIds, VacuumWorldActionResult result) {
		if(sensorsToNotifyIds.contains(recipient.getSensorId())) {
			recipient.update(this, result);
		}
	}

	private void notifyAgentSensorIfNeeded(VacuumWorldDefaultSensor recipient, List<String> sensorsToNotifyIds, VacuumWorldActionResult result) {
		if(sensorsToNotifyIds.contains(recipient.getSensorId())) {
			recipient.update(this, result);
		}
	}

	private void managePhysicsRequest(VacuumWorldActionResult result) {
		logResult(result);
		
		List<String> senderSensorIds = result.getRecipientsIds();
		notifyActor(senderSensorIds, result);
	}
	
	private void logResult(VacuumWorldActionResult result) {
		switch(result.getActionResult()) {
		case ACTION_DONE:
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was successful!");
			break;
		case ACTION_FAILED:
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was recognized as possible, but it failed during the execution!");
			break;
		case ACTION_IMPOSSIBLE:
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was recognized as impossible and it was not performed!");
			break;
		default:
			throw new IllegalArgumentException(VWUtils.ACTOR + result.getActorId() + ": unknown result: " + result.getActionResult());
		}
	}

	private void manageActuatorRequest(VacuumWorldEvent event) {
	notifyObservers(new VWPair<VacuumWorldEvent, VacuumWorldSpace>(event, this), VacuumWorldPhysics.class);
	}
}