package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.ActuatorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.SensorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.TurningDirection;

public class User extends AbstractAgent {
    private VacuumWorldCoordinates oldLocation;
    private ActorFacingDirection facingDirection;
    private ActorFacingDirection oldFacingDirection;
    private VacuumWorldCoordinates currentLocation;

    public User(UserAppearance appearance, List<Sensor> sensors, List<Actuator> actuators, UserMind mind, UserBrain brain, ActorFacingDirection facingDirection) {
	super(appearance, sensors, actuators, mind, brain);

	this.oldFacingDirection = null;
	this.facingDirection = facingDirection;
	this.oldLocation = null;
	this.currentLocation = null;
    }

    @Override
    public String getId() {
	return super.getId().toString();
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
	if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass()) || arg instanceof VacuumWorldSpeechActionResult) {
	    notifyObservers(arg, UserBrain.class);
	}
    }

    private void manageBrainRequest(Object arg) {
	if (arg instanceof EnvironmentalAction) {
	    manageBrainRequest((EnvironmentalAction) arg);
	}
    }

    private void manageBrainRequest(EnvironmentalAction action) {
	action.setActor(this);
	VacuumWorldEvent event = new VacuumWorldEvent(action, System.currentTimeMillis(), this);

	String sensorToBeNotifiedBackId = getSeeingSensors().get(0).getSensorId();
	event.setSensorToCallBackId(sensorToBeNotifiedBackId);

	String actuatorRecipientId = selectActuatorRecipientId(action);
	event.setActuatorRecipientId(actuatorRecipientId);

	getObservers().forEach(recipient -> notifyIfNeeded(recipient, event, actuatorRecipientId));
    }

    private String selectActuatorRecipientId(EnvironmentalAction action) {
	if (PhysicalAction.class.isAssignableFrom(action.getClass()) || PerceiveAction.class.isAssignableFrom(action.getClass())) {
	    return getPhysicalActuators().get(0).getActuatorId();
	}
	else if (SpeechAction.class.isAssignableFrom(action.getClass())) {
	    return getSpeakingActuators().get(0).getActuatorId();
	}
	else {
	    return null;
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

    @Override
    public UserMind getMind() {
	return (UserMind) super.getMind();
    }

    @Override
    public UserBrain getBrain() {
	return (UserBrain) super.getBrain();
    }

    @Override
    public UserAppearance getExternalAppearance() {
	return (UserAppearance) super.getExternalAppearance();
    }

    public List<UserSensor> getSeeingSensors() {
	return getSpecificSensors(SensorPurpose.LOOK);
    }

    public List<UserSensor> getListeningSensors() {
	return getSpecificSensors(SensorPurpose.HEAR);
    }

    public List<UserSensor> getUndefinedSensors() {
	return getSpecificSensors(SensorPurpose.UNDEFINED);
    }

    private List<UserSensor> getSpecificSensors(SensorPurpose purpose) {
	List<Sensor> candidates = getSensors().stream().filter(sensor -> purpose.equals(sensor.getPurpose())).collect(Collectors.toList());
	
	return candidates.stream().filter(sensor -> sensor instanceof UserSensor).map(sensor -> (UserSensor) sensor).collect(Collectors.toList());
    }

    public List<UserActuator> getPhysicalActuators() {
	return getSpecificActuators(ActuatorPurpose.ACT);
    }

    public List<UserActuator> getSpeakingActuators() {
	return getSpecificActuators(ActuatorPurpose.SPEAK);
    }

    public List<UserActuator> getundefinedActuators() {
	return getSpecificActuators(ActuatorPurpose.UNDEFINED);
    }

    private List<UserActuator> getSpecificActuators(ActuatorPurpose purpose) {
	List<Actuator> candidates = getActuators().stream().filter(actuator -> purpose.equals(actuator.getPurpose())).collect(Collectors.toList());
	
	return candidates.stream().filter(actuator -> actuator instanceof UserActuator).map(actuator -> (UserActuator) actuator).collect(Collectors.toList());
    }

    public VacuumWorldCoordinates getOldLocation() {
	return this.oldLocation;
    }

    public VacuumWorldCoordinates getCurrentLocation() {
	return this.currentLocation;
    }

    public void setCurrentLocation(VacuumWorldCoordinates coordinates) {
	this.currentLocation = coordinates;
    }

    public ActorFacingDirection getOldFacingDirection() {
	return this.oldFacingDirection;
    }

    public ActorFacingDirection getFacingDirection() {
	return this.facingDirection;
    }

    public void turn(TurningDirection turningDirection) {
	switch (turningDirection) {
	case LEFT:
	    turnLeft();
	    break;
	case RIGHT:
	    turnRight();
	    break;
	default:
	    break;
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

    @Override
    public int getPerceptionRange() {
	return Integer.MAX_VALUE;
    }

    @Override
    public boolean canSeeBehind() {
	return true;
    }
}