package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.SensingAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.ActuatorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.SensorPurpose;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;

public class VacuumWorldMonitoringAgent extends AbstractAgent {

    public VacuumWorldMonitoringAgent(VacuumWorldMonitoringAgentAppearance appearance, List<Sensor> sensors, List<Actuator> actuators, VacuumWorldMonitoringAgentMind mind, VacuumWorldMonitoringAgentBrain brain) {
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
	return getSpecificSensors(SensorPurpose.LOOK);
    }

    public List<VacuumWorldMonitoringAgentSensor> getListeningSensors() {
	return getSpecificSensors(SensorPurpose.HEAR);
    }

    public List<VacuumWorldMonitoringAgentSensor> getDatabaseSensors() {
	return getSpecificSensors(SensorPurpose.GET_DB_DATA);
    }

    public List<VacuumWorldMonitoringAgentSensor> getUndefinedSensors() {
	return getSpecificSensors(SensorPurpose.UNDEFINED);
    }

    private List<VacuumWorldMonitoringAgentSensor> getSpecificSensors(SensorPurpose purpose) {
	List<Sensor> candidates = getSensors().stream().filter((Sensor sensor) -> purpose.equals(sensor.getPurpose())).collect(Collectors.toList());
	
	return candidates.stream().filter(sensor -> sensor instanceof VacuumWorldMonitoringAgentSensor).map(sensor -> (VacuumWorldMonitoringAgentSensor) sensor).collect(Collectors.toList());
    }

    public List<VacuumWorldMonitoringAgentActuator> getPhysicalActuators() {
	return getSpecificActuators(ActuatorPurpose.ACT);
    }

    public List<VacuumWorldMonitoringAgentActuator> getSpeakingActuators() {
	return getSpecificActuators(ActuatorPurpose.SPEAK);
    }

    public List<VacuumWorldMonitoringAgentActuator> getDatabaseActuators() {
	return getSpecificActuators(ActuatorPurpose.INTERACT_WITH_DB);
    }

    public List<VacuumWorldMonitoringAgentActuator> getundefinedActuators() {
	return getSpecificActuators(ActuatorPurpose.UNDEFINED);
    }

    private List<VacuumWorldMonitoringAgentActuator> getSpecificActuators(ActuatorPurpose purpose) {
	List<Actuator> candidates = getActuators().stream().filter((Actuator actuator) -> purpose.equals(actuator.getPurpose())).collect(Collectors.toList());
	
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