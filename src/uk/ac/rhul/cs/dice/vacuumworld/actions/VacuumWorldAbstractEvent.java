package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;

public abstract class VacuumWorldAbstractEvent extends AbstractEvent {
	private String sensorToCallBackId;
	private String actuatorRecipient;
	
	public VacuumWorldAbstractEvent(EnvironmentalAction action, Long timestamp, Actor actor) {
		super(action, timestamp, actor);
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