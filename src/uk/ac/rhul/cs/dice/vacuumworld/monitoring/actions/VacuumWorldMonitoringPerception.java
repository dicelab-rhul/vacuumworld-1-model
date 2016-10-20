package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldMonitoringPerception extends VacuumWorldPerception {

	public VacuumWorldMonitoringPerception(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, VacuumWorldCoordinates actorCoordinates) {
		super(perception, actorCoordinates);
	}
	
	@Override
	public ActorFacingDirection getActorCurrentFacingDirection() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isDirtOnActorCurrentLocation() {
		return false;
	}
	
	@Override
	public VacuumWorldAgentType getAgentType() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean canAgentClean() {
		return false;
	}
	
	@Override
	public boolean canAgentSpotCompatibleDirt() {
		return false;
	}
	
	@Override
	public List<VacuumWorldLocation> getLocationsWithCompatibleDirt() {
		throw new UnsupportedOperationException();
	}
}