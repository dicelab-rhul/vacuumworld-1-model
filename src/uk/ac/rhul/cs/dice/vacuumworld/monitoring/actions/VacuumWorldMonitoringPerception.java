package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.Map;

import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldMonitoringPerception extends VacuumWorldPerception {

    public VacuumWorldMonitoringPerception(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, VacuumWorldCoordinates actorCoordinates) {
	super(perception, actorCoordinates);
    }
}