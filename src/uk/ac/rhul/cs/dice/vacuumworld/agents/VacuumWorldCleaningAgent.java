package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.utils.TurningDirection;

public class VacuumWorldCleaningAgent extends AbstractAgent<VacuumWorldSensorPurpose, VacuumWorldActuatorPurpose> {
    private static final int PERCEPTION_RANGE = 2;
    private static final boolean CAN_SEE_BEHIND = false;

    private VacuumWorldCoordinates oldLocation;
    private VacuumWorldCoordinates currentLocation;
    private ActorFacingDirection oldFacingDirection;
    private ActorFacingDirection facingDirection;

    public VacuumWorldCleaningAgent(VacuumWorldAgentAppearance appearance, List<Sensor<VacuumWorldSensorPurpose>> sensors, List<Actuator<VacuumWorldActuatorPurpose>> actuators, VacuumWorldDefaultMind mind, VacuumWorldDefaultBrain brain, ActorFacingDirection facingDirection) {
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
	return getSpecificSensors(VacuumWorldSensorPurpose.SEEING_SENSOR);
    }

    public List<VacuumWorldDefaultSensor> getListeningSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.LISTENING_SENSOR);
    }

    public List<VacuumWorldDefaultSensor> getUndefinedSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.UNDEFINED);
    }

    private List<VacuumWorldDefaultSensor> getSpecificSensors(VacuumWorldSensorPurpose role) {
	List<Sensor<VacuumWorldSensorPurpose>> candidates = getSensors().stream().filter(sensor -> role.equals(sensor.getRole())).collect(Collectors.toList());
	
	return candidates.stream().filter(sensor -> sensor instanceof VacuumWorldDefaultSensor).map(sensor -> (VacuumWorldDefaultSensor) sensor).collect(Collectors.toList());
    }

    public List<VacuumWorldDefaultActuator> getPhysicalActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.PHYSICAL_ACTUATOR);
    }

    public List<VacuumWorldDefaultActuator> getSpeakingActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.SPEAKING_ACTUATOR);
    }

    public List<VacuumWorldDefaultActuator> getundefinedActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.UNDEFINED);
    }

    private List<VacuumWorldDefaultActuator> getSpecificActuators(VacuumWorldActuatorPurpose role) {
	List<Actuator<VacuumWorldActuatorPurpose>> candidates = getActuators().stream().filter(actuator -> role.equals(actuator.getRole())).collect(Collectors.toList());
	
	return candidates.stream().filter(actuator -> actuator instanceof VacuumWorldDefaultActuator).map(actuator -> (VacuumWorldDefaultActuator) actuator).collect(Collectors.toList());
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
	if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass()) || VacuumWorldSpeechActionResult.class.isAssignableFrom(arg.getClass())) {
	    notifyObservers(arg, VacuumWorldDefaultBrain.class);
	}
    }

    private void manageBrainRequest(Object arg) {
	if (arg instanceof EnvironmentalAction) {
	    manageBrainRequest((EnvironmentalAction) arg);
	}
    }

    private void manageBrainRequest(EnvironmentalAction action) {
	this.oldLocation = this.currentLocation;
	this.oldFacingDirection = this.facingDirection;

	action.setActor(this);
	VacuumWorldEvent event = new VacuumWorldEvent(action, System.currentTimeMillis(), this);

	String sensorToBeNotifiedBackId = getSeeingSensors().get(0).getSensorId();
	event.setSensorToCallBackId(sensorToBeNotifiedBackId);

	String actuatorRecipientId = selectActuatorRecipientId(action);
	event.setActuatorRecipientId(actuatorRecipientId);

	getObservers().forEach(recipient -> notifyIfNeeded(recipient, event, actuatorRecipientId));
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
	else if (SpeechAction.class.isAssignableFrom(arg.getClass())) {
	    return getSpeakingActuators().get(0).getActuatorId();
	}

	return null;
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