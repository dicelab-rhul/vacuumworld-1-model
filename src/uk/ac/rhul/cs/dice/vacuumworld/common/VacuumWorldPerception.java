package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
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
}