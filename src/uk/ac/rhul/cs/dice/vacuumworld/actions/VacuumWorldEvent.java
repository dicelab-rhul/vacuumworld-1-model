package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;

public class VacuumWorldEvent extends AbstractEvent {
	private String sensorToCallBackId;
	private String actuatorRecipient;
	
	public VacuumWorldEvent(Action action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}

	@Override
	public String represent() {
		return "at time " + getTimestamp() + " agent " + ((VacuumWorldCleaningAgent) getActor()).getExternalAppearance().getName() +
				" attempted a " + getAction().getClass().getSimpleName();
	}

	@Override
	public boolean isPossible(Physics physics, Space context) {
		return getAction().isPossible(physics, context);
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return getAction().isNecessary(physics, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return getAction().perform(physics, context);
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return getAction().succeeded(physics, context);
	}

	public String getSensorToCallBackId() {
		return this.sensorToCallBackId;
	}

	public void setSensorToCallBackId(String sensorToCallBackId) {
		this.sensorToCallBackId = sensorToCallBackId;
	}

	public String getActuatorRecipient() {
		return this.actuatorRecipient;
	}

	public void setActuatorRecipient(String actuatorRecipient) {
		this.actuatorRecipient = actuatorRecipient;
	}
}
