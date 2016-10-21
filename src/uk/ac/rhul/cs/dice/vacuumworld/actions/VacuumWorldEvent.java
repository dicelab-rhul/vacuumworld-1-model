package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldEvent extends VacuumWorldAbstractEvent<VacuumWorldPerception> {
	
	public VacuumWorldEvent(EnvironmentalAction<VacuumWorldPerception> action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}

	@Override
	public String represent() {
		return "at time " + getTimestamp() + " agent " + ((VacuumWorldCleaningAgent) getActor()).getExternalAppearance().getName() + " attempted a " + getAction().getClass().getSimpleName();
	}
}