package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class User extends AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> {
	private ActorFacingDirection facingDirection;
	private VacuumWorldCoordinates currentLocation;
	
	public User(UserAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, AbstractAgentMind mind, AbstractAgentBrain brain, ActorFacingDirection facingDirection) {
		super(appearance, sensors, actuators, mind, brain);
	
		this.facingDirection = facingDirection;
		this.currentLocation = null;
	}

	@Override
	public String getId() {
		return super.getId().toString();
	}
	
	public ActorFacingDirection getFacingDirection() {
		return this.facingDirection;
	}
	
	@Override
	public Object simulate() {
		return null;
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof UserBrain) {
			manageBrainRequest(arg);
		}
		else if (o instanceof UserSensor) {
			manageSensorRequest(arg);
		}
	}
	
	private void manageSensorRequest(Object arg) {
		if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			notifyObservers(arg, UserBrain.class);
		}
	}

	private void manageBrainRequest(Object arg) {
		if (arg instanceof AbstractAction) {
			manageBrainRequest((AbstractAction) arg);
		}
	}
	
	private void manageBrainRequest(AbstractAction action) {
		VacuumWorldEvent event = new VacuumWorldEvent(action, System.currentTimeMillis(), this);

		String sensorToBeNotifiedBackId = getSeeingSensors().get(0).getSensorId();
		event.setSensorToCallBackId(sensorToBeNotifiedBackId);

		String actuatorRecipientId = selectActuatorRecipientId(action);
		event.setActuatorRecipient(actuatorRecipientId);

		notifyAgentActuators(event, actuatorRecipientId);
	}
	
	private String selectActuatorRecipientId(AbstractAction action) {
		if(PhysicalAction.class.isAssignableFrom(action.getClass())) {
			return getPhysicalActuators().get(0).getActuatorId();
		}
		else if(SpeechAction.class.isAssignableFrom(action.getClass())) {
			return getSpeakingActuators().get(0).getActuatorId();
		}
		else {
			return null;
		}
	}

	private void notifyAgentActuators(Object arg, String actuatorId) {
		List<CustomObserver> recipients = this.getObservers();

		for (CustomObserver recipient : recipients) {
			notifyIfNeeded(recipient, arg, actuatorId);
		}
	}

	private void notifyIfNeeded(CustomObserver recipient, Object arg, String actuatorId) {
		if (recipient instanceof UserActuator) {
			UserActuator a = (UserActuator) recipient;

			if (a.getActuatorId().equals(actuatorId)) {
				a.update(this, arg);
			}
		}
	}

	public List<UserSensor> getSeeingSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.SEEING_SENSOR);
	}
	
	public List<UserSensor> getListeningSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.LISTENING_SENSOR);
	}
	
	public List<UserSensor> getUndefinedSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.UNDEFINED);
	}
	
	private List<UserSensor> getSpecificSensors(VacuumWorldSensorRole role) {
		List<Sensor<VacuumWorldSensorRole>> candidates = getSensors().stream().filter((Sensor<VacuumWorldSensorRole> sensor) -> role.equals(sensor.getRole())).collect(Collectors.toList());
		List<UserSensor> toReturn = new ArrayList<>();
		
		for(Sensor<VacuumWorldSensorRole> sensor : candidates) {
			if(sensor instanceof UserSensor) {
				toReturn.add((UserSensor) sensor);
			}
		}
		
		return toReturn;
	}
	
	public List<UserActuator> getPhysicalActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.PHYSICAL_ACTUATOR);
	}
	
	public List<UserActuator> getSpeakingActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.SPEAKING_ACTUATOR);
	}
	
	public List<UserActuator> getundefinedActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.UNDEFINED);
	}
	
	private List<UserActuator> getSpecificActuators(VacuumWorldActuatorRole role) {
		List<Actuator<VacuumWorldActuatorRole>> candidates = getActuators().stream().filter((Actuator<VacuumWorldActuatorRole> actuator) -> role.equals(actuator.getRole())).collect(Collectors.toList());
		List<UserActuator> toReturn = new ArrayList<>();
		
		for(Actuator<VacuumWorldActuatorRole> actuator : candidates) {
			if(actuator instanceof UserActuator) {
				toReturn.add((UserActuator) actuator);
			}
		}
		
		return toReturn;
	}
	
	public VacuumWorldCoordinates getCurrentLocation() {
		return this.currentLocation;
	}

	public void setCurrentLocation(VacuumWorldCoordinates coordinates) {
		this.currentLocation = coordinates;
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
		result = prime * result + ((this.currentLocation == null) ? 0 : this.currentLocation.hashCode());
		result = prime * result + ((this.facingDirection == null) ? 0 : this.facingDirection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		}

		return this.getId().equals(((User) obj).getId());
	}
}