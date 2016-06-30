package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class VacuumWorldSpace extends EnvironmentalSpace {
	private int[] dimensions;
	private static final String BAD_DIRECTION = "Bad agent direction: ";
	
	public VacuumWorldSpace(int[] dimensions) {		
		super(new HashMap<LocationKey, Location>());
		
		this.dimensions = dimensions;
		
		for(int i = 0; i < this.dimensions[0]; i++) {
			for(int j = 0; j < this.dimensions[1]; j++) {
				VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);
				this.addLocation(coordinates, new VacuumWorldLocation(coordinates, VacuumWorldLocationType.NORMAL, this.dimensions[0] - 1, this.dimensions[1] - 1));
			}
		}
	}
	
	public VacuumWorldSpace(int[] dimensions, Map<LocationKey, Location> grid) {		
		super(grid);
		this.dimensions = dimensions;
	}
	
	public int[] getDimensions() {
		return this.dimensions;
	}
	
	public List<VacuumWorldCleaningAgent> getAgents() {
		List<VacuumWorldCleaningAgent> agents = new ArrayList<>();
		
		for(Location location : getLocations()) {
			if(((VacuumWorldLocation) location).isAnAgentPresent()) {
				agents.add(((VacuumWorldLocation) location).getAgent());
			}
		}
		
		return agents;
	}
	
	public VacuumWorldLocation getNorthernLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x, y - 1));
	}
	
	public VacuumWorldLocation getNorthernLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getNorthernCoordinates());
	}
	
	public VacuumWorldLocation getSouthernLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x, y + 1));
	}
	
	public VacuumWorldLocation getSouthernLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getSouthernCoordinates());
	}
	
	public VacuumWorldLocation getWesternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x - 1, y));
	}
	
	public VacuumWorldLocation getWesternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getWesternCoordinates());
	}
	
	public VacuumWorldLocation getEasternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x + 1, y));
	}
	
	public VacuumWorldLocation getEasternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getEasternCoordinates());
	}
	
	public VacuumWorldLocation getNorthWesternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x - 1, y - 1));
	}
	
	public VacuumWorldLocation getNorthWesternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getNorthWesternCoordinates());
	}
	
	public VacuumWorldLocation getNorthEasternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x + 1, y - 1));
	}
	
	public VacuumWorldLocation getNorthEasternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getNorthEasternCoordinates());
	}
	
	public VacuumWorldLocation getSouthWesternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x - 1, y + 1));
	}
	
	public VacuumWorldLocation getSouthWesternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getSouthWesternCoordinates());
	}
	
	public VacuumWorldLocation getSouthEasternLocation(int x, int y) {
		return (VacuumWorldLocation) this.getLocation(new VacuumWorldCoordinates(x + 1, y + 1));
	}
	
	public VacuumWorldLocation getSouthEasternLocation(VacuumWorldCoordinates coordinates) {
		return (VacuumWorldLocation) this.getLocation(coordinates.getSouthEasternCoordinates());
	}
	
	public VacuumWorldLocation getFrontLocation(VacuumWorldCoordinates coordinates, AgentFacingDirection agentDirection) {
		switch(agentDirection) {
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
	
	public VacuumWorldLocation getFrontLeftLocation(VacuumWorldCoordinates coordinates, AgentFacingDirection agentDirection) {
		switch(agentDirection) {
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
	
	public VacuumWorldLocation getFrontRightLocation(VacuumWorldCoordinates coordinates, AgentFacingDirection agentDirection) {
		switch(agentDirection) {
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
	
	public VacuumWorldLocation getLeftLocation(VacuumWorldCoordinates coordinates, AgentFacingDirection agentDirection) {
		switch(agentDirection) {
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
	
	public VacuumWorldLocation getRightLocation(VacuumWorldCoordinates coordinates, AgentFacingDirection agentDirection) {
		switch(agentDirection) {
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
		if(o instanceof VacuumWorldDefaultActuator && arg instanceof VacuumWorldEvent) {
			manageActuatorRequest((VacuumWorldEvent) arg);
		}
		else if(o instanceof VacuumWorldPhysics && DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			managePhysicsRequest((DefaultActionResult) arg);
		}
	}

	private void managePhysicsRequest(DefaultActionResult result) {		
		notifyAgentSensor(result, result.getRecipientId());
	}

	private void manageActuatorRequest(VacuumWorldEvent event) {
		notifyObservers(new Object[]{event, this}, VacuumWorldPhysics.class);
	}
	
	private void notifyAgentSensor(Object arg, String sensorId) {
		List<CustomObserver> recipients = this.getObservers();
		
		for(CustomObserver recipient : recipients) {
			notifyIfNeeded(recipient, arg, sensorId);
		}
	}

	private void notifyIfNeeded(CustomObserver recipient, Object arg, String sensorId) {
		if(recipient instanceof VacuumWorldDefaultSensor) {
			VacuumWorldDefaultSensor s = (VacuumWorldDefaultSensor) recipient;
			
			if(s.getSensorId().equals(sensorId)) {
				s.update(this, arg);
			}
		}
	}
}