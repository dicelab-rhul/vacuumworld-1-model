package uk.ac.rhul.cs.dice.vacuumworld.model.server;

import java.util.ArrayList;
import java.util.List;

public class VacuumWorldLog {
    private List<String> log;
    
    public VacuumWorldLog() {
	this.log = new ArrayList<>();
    }
    
    public VacuumWorldLog(String entry) {
	this.log = new ArrayList<>();
	this.log.add(entry);
    }
    
    public VacuumWorldLog(List<String> entries) {
	this.log = entries;
    }
    
    public List<String> getLog() {
	return this.log;
    }
    
    public void clear() {
	this.log.clear();
    }
}