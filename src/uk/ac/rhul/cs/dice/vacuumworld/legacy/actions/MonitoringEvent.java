package uk.ac.rhul.cs.dice.vacuumworld.legacy.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;

public class MonitoringEvent extends AbstractEvent {
	private String sensorToCallBackId;
	private String actuatorRecipient;

	public MonitoringEvent(EnvironmentalAction action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}

	@Override
	public String represent() {
		return this.getAction() + " : " + this.getTimestamp() + " : " + this.getActor();
	}

	@Override
	public boolean isPossible(Physics physics, Space context) {
		return this.getAction().isPossible(physics, context);
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return this.getAction().isNecessary(physics, context);
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return this.getAction().perform(physics, context);
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return this.getAction().succeeded(physics, context);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " : " + this.getAction();
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