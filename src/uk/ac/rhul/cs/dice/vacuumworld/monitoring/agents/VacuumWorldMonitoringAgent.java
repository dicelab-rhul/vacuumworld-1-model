package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorPurpose;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorPurpose;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;

public class VacuumWorldMonitoringAgent extends AbstractAgent<VacuumWorldSensorPurpose, VacuumWorldActuatorPurpose> {

    public VacuumWorldMonitoringAgent(VacuumWorldMonitoringAgentAppearance appearance, List<Sensor<VacuumWorldSensorPurpose>> sensors, List<Actuator<VacuumWorldActuatorPurpose>> actuators, VacuumWorldMonitoringAgentMind mind, VacuumWorldMonitoringAgentBrain brain) {
	super(appearance, sensors, actuators, mind, brain);
    }

    @Override
    public String getId() {
	return super.getId().toString();
    }

    @Override
    public VacuumWorldMonitoringAgentMind getMind() {
	return (VacuumWorldMonitoringAgentMind) super.getMind();
    }

    @Override
    public VacuumWorldMonitoringAgentBrain getBrain() {
	return (VacuumWorldMonitoringAgentBrain) super.getBrain();
    }

    @Override
    public VacuumWorldMonitoringAgentAppearance getExternalAppearance() {
	return (VacuumWorldMonitoringAgentAppearance) super.getExternalAppearance();
    }

    @Override
    public Object simulate() {
	return null;
    }

    public List<VacuumWorldMonitoringAgentSensor> getSeeingSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.SEEING_SENSOR);
    }

    public List<VacuumWorldMonitoringAgentSensor> getListeningSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.LISTENING_SENSOR);
    }

    public List<VacuumWorldMonitoringAgentSensor> getDatabaseSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.DATABASE_SENSOR);
    }

    public List<VacuumWorldMonitoringAgentSensor> getUndefinedSensors() {
	return getSpecificSensors(VacuumWorldSensorPurpose.UNDEFINED);
    }

    private List<VacuumWorldMonitoringAgentSensor> getSpecificSensors(VacuumWorldSensorPurpose role) {
	List<Sensor<VacuumWorldSensorPurpose>> candidates = getSensors().stream().filter((Sensor<VacuumWorldSensorPurpose> sensor) -> role.equals(sensor.getPurpose())).collect(Collectors.toList());
	
	return candidates.stream().filter(sensor -> sensor instanceof VacuumWorldMonitoringAgentSensor).map(sensor -> (VacuumWorldMonitoringAgentSensor) sensor).collect(Collectors.toList());
    }

    public List<VacuumWorldMonitoringAgentActuator> getPhysicalActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.PHYSICAL_ACTUATOR);
    }

    public List<VacuumWorldMonitoringAgentActuator> getSpeakingActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.SPEAKING_ACTUATOR);
    }

    public List<VacuumWorldMonitoringAgentActuator> getDatabaseActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.DATABASE_ACTUATOR);
    }

    public List<VacuumWorldMonitoringAgentActuator> getundefinedActuators() {
	return getSpecificActuators(VacuumWorldActuatorPurpose.UNDEFINED);
    }

    private List<VacuumWorldMonitoringAgentActuator> getSpecificActuators(VacuumWorldActuatorPurpose role) {
	List<Actuator<VacuumWorldActuatorPurpose>> candidates = getActuators().stream().filter((Actuator<VacuumWorldActuatorPurpose> actuator) -> role.equals(actuator.getPurpose())).collect(Collectors.toList());
	
	return candidates.stream().filter(actuator -> actuator instanceof VacuumWorldMonitoringAgentActuator).map(actuator -> (VacuumWorldMonitoringAgentActuator) actuator).collect(Collectors.toList());
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldMonitoringAgentBrain && arg instanceof EnvironmentalAction) {
	    manageBrainRequest((EnvironmentalAction) arg);
	}
	else if (o instanceof VacuumWorldMonitoringAgentSensor && VacuumWorldMonitoringActionResult.class.isAssignableFrom(arg.getClass())) {
	    notifyObservers(arg, VacuumWorldMonitoringAgentBrain.class);
	}
    }

    private void manageBrainRequest(EnvironmentalAction action) {
	action.setActor(this);
	VacuumWorldMonitoringEvent event = new VacuumWorldMonitoringEvent(action, System.currentTimeMillis(), this);

	String sensorToBeNotifiedBackId = selectSensorToBeNotifiedBackId(action);
	event.setSensorToCallBackId(sensorToBeNotifiedBackId);

	String actuatorRecipientId = selectActuatorRecipientId(action);
	event.setActuatorRecipientId(actuatorRecipientId);

	getObservers().forEach(recipient -> notifyIfNeeded(recipient, event, actuatorRecipientId));
    }

    private void notifyIfNeeded(CustomObserver recipient, Object arg, String actuatorId) {
	if (recipient instanceof VacuumWorldMonitoringAgentActuator) {
	    VacuumWorldMonitoringAgentActuator a = (VacuumWorldMonitoringAgentActuator) recipient;

	    if (a.getActuatorId().equals(actuatorId)) {
		a.update(this, arg);
	    }
	}
    }

    private String selectActuatorRecipientId(EnvironmentalAction action) {
	if (PhysicalAction.class.isAssignableFrom(action.getClass()) || SensingAction.class.isAssignableFrom(action.getClass())) {
	    return getPhysicalActuators().get(0).getActuatorId();
	}
	else if (SpeechAction.class.isAssignableFrom(action.getClass())) {
	    return getSpeakingActuators().get(0).getActuatorId();
	}
	else if (DatabaseAction.class.isAssignableFrom(action.getClass())) {
	    return getDatabaseActuators().get(0).getActuatorId();
	}

	return null;
    }

    private String selectSensorToBeNotifiedBackId(EnvironmentalAction action) {
	if (PhysicalAction.class.isAssignableFrom(action.getClass()) || SensingAction.class.isAssignableFrom(action.getClass())) {
	    return getSeeingSensors().get(0).getSensorId();
	}
	else if (DatabaseAction.class.isAssignableFrom(action.getClass())) {
	    return getDatabaseSensors().get(0).getSensorId();
	}

	return null;
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