package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class VacuumWorldMemory {
    private Set<VacuumWorldCoordinates> perceived;
    
    public VacuumWorldMemory() {
	this.perceived = new HashSet<>();
    }
    
    public Set<VacuumWorldCoordinates> getPerceived() {
	return this.perceived;
    }
    
    public void addPerceived(VacuumWorldCoordinates coordinates) {
	if(this.perceived == null) {
	    this.perceived = new HashSet<>();
	}
	
	this.perceived.add(coordinates);
    }
    
    public Set<VacuumWorldCoordinates> getNotExplored(Set<VacuumWorldCoordinates> global) {
	return global.stream().filter(coordinates -> !this.perceived.contains(coordinates)).collect(Collectors.toSet());
    }
    
    public long countNotExplored(Set<VacuumWorldCoordinates> global) {
	return global.stream().filter(coordinates -> !this.perceived.contains(coordinates)).count();
    }
}