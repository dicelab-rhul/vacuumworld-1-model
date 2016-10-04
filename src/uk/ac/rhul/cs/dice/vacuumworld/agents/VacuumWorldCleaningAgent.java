package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class VacuumWorldCleaningAgent extends AbstractAgent implements VacuumWorldAgentInterface {
	private static final int PERCEPTION_RANGE = 2;
	private static final boolean CAN_SEE_BEHIND = false;

	private static final int ACTION_ACTUATOR_INDEX = 0;
	private static final int SERVER_MESSAGE_SENSOR_INDEX = 1;
	private static final int ACTION_RESULT_SENSOR_INDEX = 0;
	private static final int MIND_SIGNAL_ACTUATOR_INDEX = 1;

	private VacuumWorldCoordinates currentLocation;
	private AgentFacingDirection facingDirection;

	public VacuumWorldCleaningAgent(AbstractAgentAppearance appearance, List<Sensor> sensors, List<Actuator> actuators, AbstractAgentMind mind, AbstractAgentBrain brain, AgentFacingDirection facingDirection) {
		super(appearance, sensors, actuators, mind, brain);

		this.facingDirection = facingDirection;
		this.currentLocation = null;
	}

	@Override
	public final int getPerceptionRange() {
		return PERCEPTION_RANGE;
	}

	@Override
	public final boolean canSeeBehind() {
		return CAN_SEE_BEHIND;
	}

	@Override
	public final int getActionActuatorIndex() {
		return ACTION_ACTUATOR_INDEX;
	}

	public final int getMindSignalActuatorIndex() {
		return MIND_SIGNAL_ACTUATOR_INDEX;
	}

	@Override
	public final int getActionResultSensorIndex() {
		return ACTION_RESULT_SENSOR_INDEX;
	}

	public final int getServerMessageSensorIndex() {
		return SERVER_MESSAGE_SENSOR_INDEX;
	}

	@Override
	public Object simulate() {
		return null;
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldDefaultBrain) {
			manageBrainRequest(arg);
		}
		else if (o instanceof VacuumWorldDefaultSensor) {
			manageSensorRequest(arg);
		}
	}

	private void manageSensorRequest(Object arg) {
		if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			notifyObservers(arg, VacuumWorldDefaultBrain.class);
		}
	}

	private void manageBrainRequest(Object arg) {
		if (arg instanceof AbstractAction) {
			manageBrainRequest((AbstractAction) arg);
		}
	}

	private void manageBrainRequest(AbstractAction action) {
		VacuumWorldEvent event = new VacuumWorldEvent(action, System.currentTimeMillis(), this);

		String sensorToBeNotifiedBackId = selectSensorToBeNotifiedBackId(action);
		event.setSensorToCallBackId(sensorToBeNotifiedBackId);

		String actuatorRecipientId = selectActuatorRecipientId(action);
		event.setActuatorRecipient(actuatorRecipientId);

		notifyAgentActuators(event, actuatorRecipientId);
	}

	private void notifyAgentActuators(Object arg, String actuatorId) {
		List<CustomObserver> recipients = this.getObservers();

		for (CustomObserver recipient : recipients) {
			notifyIfNeeded(recipient, arg, actuatorId);
		}
	}

	private void notifyIfNeeded(CustomObserver recipient, Object arg, String actuatorId) {
		if (recipient instanceof VacuumWorldDefaultActuator) {
			VacuumWorldDefaultActuator a = (VacuumWorldDefaultActuator) recipient;

			if (a.getActuatorId().equals(actuatorId)) {
				a.update(this, arg);
			}
		}
	}

	private String selectActuatorRecipientId(Object arg) {
		if (AbstractAction.class.isAssignableFrom(arg.getClass())) {
			return ((VacuumWorldDefaultActuator) getActuators().get(ACTION_ACTUATOR_INDEX)).getActuatorId();
		}

		return null;
	}

	private String selectSensorToBeNotifiedBackId(EnvironmentalAction action) {
		if (AbstractAction.class.isAssignableFrom(action.getClass())) {
			return ((VacuumWorldDefaultSensor) getSensors().get(ACTION_RESULT_SENSOR_INDEX)).getSensorId();
		}

		return null;
	}

	public VacuumWorldCoordinates getCurrentLocation() {
		return this.currentLocation;
	}

	public void setCurrentLocation(VacuumWorldCoordinates coordinates) {
		this.currentLocation = coordinates;
	}

	public AgentFacingDirection getFacingDirection() {
		return this.facingDirection;
	}

	public void turn(boolean right) {
		if(right) {
			turnRight();
		}
		else {
			turnLeft();
		}
	}
	
	private void turnLeft() {
		this.facingDirection = this.facingDirection.getLeftDirection();
	}

	private void turnRight() {
		this.facingDirection = this.facingDirection.getRightDirection();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((currentLocation == null) ? 0 : currentLocation.hashCode());
		result = prime * result + ((facingDirection == null) ? 0 : facingDirection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VacuumWorldCleaningAgent)) {
			return false;
		}

		return this.getId().equals(((VacuumWorldCleaningAgent) obj).getId());
	}

	@Override
	public String toString() {
		return "{" + this.getClass().getSimpleName() + "(" + this.currentLocation.getX() + "," + this.currentLocation.getY() + "," + this.facingDirection + "),Sensors: " + this.getSensors().size() + ",Actuators: " + this.getActuators().size() + "}";
	}
}