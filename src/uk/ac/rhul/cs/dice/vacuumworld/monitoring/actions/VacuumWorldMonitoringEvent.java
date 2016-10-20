package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;

public class VacuumWorldMonitoringEvent extends VacuumWorldEvent {

	public VacuumWorldMonitoringEvent(EnvironmentalAction action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}
	
	@Override
	public String represent() {
		return "at time " + getTimestamp() + " agent " + ((VacuumWorldMonitoringAgent) getActor()).getExternalAppearance().getName() + " attempted a " + getAction().getClass().getSimpleName();
	}
}