package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;

public abstract class VacuumWorldAbstractEvent<P extends Perception> extends AbstractEvent<P> {
	private String sensorToCallBackId;
	private String actuatorRecipient;
	
	public VacuumWorldAbstractEvent(EnvironmentalAction<P> action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
	}

	@Override
	public boolean isPossible(Physics<P> physics, Space context) {
		return getAction().isPossible(physics, context);
	}

	@Override
	public boolean isNecessary(Physics<P> physics, Space context) {
		return getAction().isNecessary(physics, context);
	}

	@Override
	public Result<P> perform(Physics<P> physics, Space context) {
		return getAction().perform(physics, context);
	}

	@Override
	public boolean succeeded(Physics<P> physics, Space context) {
		return getAction().succeeded(physics, context);
	}

	@Override
	public String getSensorToCallBackId() {
		return this.sensorToCallBackId;
	}

	@Override
	public void setSensorToCallBackId(String sensorToCallBackId) {
		this.sensorToCallBackId = sensorToCallBackId;
	}

	@Override
	public String getActuatorRecipientId() {
		return this.actuatorRecipient;
	}

	@Override
	public void setActuatorRecipientId(String actuatorRecipient) {
		this.actuatorRecipient = actuatorRecipient;
	}
}