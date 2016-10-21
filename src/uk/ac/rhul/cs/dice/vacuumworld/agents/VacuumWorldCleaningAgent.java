package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class VacuumWorldCleaningAgent extends AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole, VacuumWorldPerception> {
	private static final int PERCEPTION_RANGE = 2;
	private static final boolean CAN_SEE_BEHIND = false;

	private VacuumWorldCoordinates currentLocation;
	private ActorFacingDirection facingDirection;

	public VacuumWorldCleaningAgent(VacuumWorldAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> actuators, VacuumWorldDefaultMind mind, VacuumWorldDefaultBrain brain, ActorFacingDirection facingDirection) {
		super(appearance, sensors, actuators, mind, brain);

		this.facingDirection = facingDirection;
		this.currentLocation = null;
	}

	@Override
	public String getId() {
		return super.getId().toString();
	}
	
	@Override
	public VacuumWorldDefaultMind getMind() {
		return (VacuumWorldDefaultMind) super.getMind();
	}
	
	@Override
	public VacuumWorldDefaultBrain getBrain() {
		return (VacuumWorldDefaultBrain) super.getBrain();
	}
	
	@Override
	public VacuumWorldAgentAppearance getExternalAppearance() {
		return (VacuumWorldAgentAppearance) super.getExternalAppearance();
	}
	
	public List<VacuumWorldDefaultSensor> getSeeingSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.SEEING_SENSOR);
	}
	
	public List<VacuumWorldDefaultSensor> getListeningSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.LISTENING_SENSOR);
	}
	
	public List<VacuumWorldDefaultSensor> getUndefinedSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.UNDEFINED);
	}
	
	private List<VacuumWorldDefaultSensor> getSpecificSensors(VacuumWorldSensorRole role) {
		List<Sensor<VacuumWorldSensorRole>> candidates = getSensors().stream().filter((Sensor<VacuumWorldSensorRole> sensor) -> role.equals(sensor.getRole())).collect(Collectors.toList());
		List<VacuumWorldDefaultSensor> toReturn = new ArrayList<>();
		
		for(Sensor<VacuumWorldSensorRole> sensor : candidates) {
			if(sensor instanceof VacuumWorldDefaultSensor) {
				toReturn.add((VacuumWorldDefaultSensor) sensor);
			}
		}
		
		return toReturn;
	}
	
	public List<VacuumWorldDefaultActuator> getPhysicalActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.PHYSICAL_ACTUATOR);
	}
	
	public List<VacuumWorldDefaultActuator> getSpeakingActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.SPEAKING_ACTUATOR);
	}
	
	public List<VacuumWorldDefaultActuator> getundefinedActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.UNDEFINED);
	}
	
	private List<VacuumWorldDefaultActuator> getSpecificActuators(VacuumWorldActuatorRole role) {
		List<Actuator<VacuumWorldActuatorRole, VacuumWorldPerception>> candidates = getActuators().stream().filter((Actuator<VacuumWorldActuatorRole, VacuumWorldPerception> actuator) -> role.equals(actuator.getRole())).collect(Collectors.toList());
		List<VacuumWorldDefaultActuator> toReturn = new ArrayList<>();
		
		for(Actuator<VacuumWorldActuatorRole, VacuumWorldPerception> actuator : candidates) {
			if(actuator instanceof VacuumWorldDefaultActuator) {
				toReturn.add((VacuumWorldDefaultActuator) actuator);
			}
		}
		
		return toReturn;
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

	@SuppressWarnings("unchecked")
	private void manageBrainRequest(Object arg) {
		if (arg instanceof EnvironmentalAction<?>) {
			manageBrainRequest((EnvironmentalAction<VacuumWorldPerception>) arg);
		}
	}

	private void manageBrainRequest(EnvironmentalAction<VacuumWorldPerception> action) {
		action.setActor(this);
		VacuumWorldEvent event = new VacuumWorldEvent(action, System.currentTimeMillis(), this);

		String sensorToBeNotifiedBackId = selectSensorToBeNotifiedBackId(action);
		event.setSensorToCallBackId(sensorToBeNotifiedBackId);

		String actuatorRecipientId = selectActuatorRecipientId(action);
		event.setActuatorRecipientId(actuatorRecipientId);

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
		if (PhysicalAction.class.isAssignableFrom(arg.getClass()) || PerceiveAction.class.isAssignableFrom(arg.getClass())) {
			return getPhysicalActuators().get(0).getActuatorId();
		}
		else if(SpeechAction.class.isAssignableFrom(arg.getClass())) {
			return getSpeakingActuators().get(0).getActuatorId();
		}

		return null;
	}

	private String selectSensorToBeNotifiedBackId(EnvironmentalAction<VacuumWorldPerception> action) {
		if (EnvironmentalAction.class.isAssignableFrom(action.getClass())) {
			return getSeeingSensors().get(0).getSensorId();
		}

		return null;
	}

	public VacuumWorldCoordinates getCurrentLocation() {
		return this.currentLocation;
	}

	public void setCurrentLocation(VacuumWorldCoordinates coordinates) {
		this.currentLocation = coordinates;
	}

	public ActorFacingDirection getFacingDirection() {
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
		result = prime * result + ((this.currentLocation == null) ? 0 : this.currentLocation.hashCode());
		result = prime * result + ((this.facingDirection == null) ? 0 : this.facingDirection.hashCode());
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