package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
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
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;

public class VacuumWorldMonitoringAgent extends AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> {

	public VacuumWorldMonitoringAgent(VacuumWorldMonitoringAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, VacuumWorldMonitoringAgentMind mind, VacuumWorldMonitoringAgentBrain brain) {
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
		return getSpecificSensors(VacuumWorldSensorRole.SEEING_SENSOR);
	}
	
	public List<VacuumWorldMonitoringAgentSensor> getListeningSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.LISTENING_SENSOR);
	}
	
	public List<VacuumWorldMonitoringAgentSensor> getDatabaseSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.DATABASE_SENSOR);
	}
	
	public List<VacuumWorldMonitoringAgentSensor> getUndefinedSensors() {
		return getSpecificSensors(VacuumWorldSensorRole.UNDEFINED);
	}
	
	private List<VacuumWorldMonitoringAgentSensor> getSpecificSensors(VacuumWorldSensorRole role) {
		List<Sensor<VacuumWorldSensorRole>> candidates = getSensors().stream().filter((Sensor<VacuumWorldSensorRole> sensor) -> role.equals(sensor.getRole())).collect(Collectors.toList());
		List<VacuumWorldMonitoringAgentSensor> toReturn = new ArrayList<>();
		
		for(Sensor<VacuumWorldSensorRole> sensor : candidates) {
			if(sensor instanceof VacuumWorldMonitoringAgentSensor) {
				toReturn.add((VacuumWorldMonitoringAgentSensor) sensor);
			}
		}
		
		return toReturn;
	}
	
	public List<VacuumWorldMonitoringAgentActuator> getPhysicalActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.PHYSICAL_ACTUATOR);
	}
	
	public List<VacuumWorldMonitoringAgentActuator> getSpeakingActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.SPEAKING_ACTUATOR);
	}
	
	public List<VacuumWorldMonitoringAgentActuator> getDatabaseActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.DATABASE_ACTUATOR);
	}
	
	public List<VacuumWorldMonitoringAgentActuator> getundefinedActuators() {
		return getSpecificActuators(VacuumWorldActuatorRole.UNDEFINED);
	}
	
	private List<VacuumWorldMonitoringAgentActuator> getSpecificActuators(VacuumWorldActuatorRole role) {
		List<Actuator<VacuumWorldActuatorRole>> candidates = getActuators().stream().filter((Actuator<VacuumWorldActuatorRole> actuator) -> role.equals(actuator.getRole())).collect(Collectors.toList());
		List<VacuumWorldMonitoringAgentActuator> toReturn = new ArrayList<>();
		
		for(Actuator<VacuumWorldActuatorRole> actuator : candidates) {
			if(actuator instanceof VacuumWorldMonitoringAgentActuator) {
				toReturn.add((VacuumWorldMonitoringAgentActuator) actuator);
			}
		}
		
		return toReturn;
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringAgentBrain && arg instanceof EnvironmentalAction) {
			manageBrainRequest((EnvironmentalAction) arg);
		}
		else if(o instanceof VacuumWorldMonitoringAgentSensor && VacuumWorldMonitoringActionResult.class.isAssignableFrom(arg.getClass())) {
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

		notifyAgentActuators(event, actuatorRecipientId);
	}

	private void notifyAgentActuators(VacuumWorldMonitoringEvent event, String actuatorRecipientId) {
		List<CustomObserver> recipients = this.getObservers();

		for (CustomObserver recipient : recipients) {
			notifyIfNeeded(recipient, event, actuatorRecipientId);
		}
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
		else if(SpeechAction.class.isAssignableFrom(action.getClass())) {
			return getSpeakingActuators().get(0).getActuatorId();
		}
		else if(DatabaseAction.class.isAssignableFrom(action.getClass())) {
			return getDatabaseActuators().get(0).getActuatorId();
		}

		return null;
	}

	private String selectSensorToBeNotifiedBackId(EnvironmentalAction action) {
		if (PhysicalAction.class.isAssignableFrom(action.getClass()) || SensingAction.class.isAssignableFrom(action.getClass())) {
			return getSeeingSensors().get(0).getSensorId();
		}
		else if(DatabaseAction.class.isAssignableFrom(action.getClass())) {
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