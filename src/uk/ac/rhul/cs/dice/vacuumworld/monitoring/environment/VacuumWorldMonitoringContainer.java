package uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;

public class VacuumWorldMonitoringContainer extends EnvironmentalSpace {
	private Map<String, VacuumWorldMonitoringAgent> monitoringAgents;
	
	public VacuumWorldMonitoringContainer() {
		this.monitoringAgents = new HashMap<>();
	}
	
	public void addMonitoringAgents(Map<String, VacuumWorldMonitoringAgent> monitoringAgents) {
		this.monitoringAgents = monitoringAgents;
	}
	
	public void addMonitoringAgents(List<VacuumWorldMonitoringAgent> monitoringAgents) {
		for(VacuumWorldMonitoringAgent agent : monitoringAgents) {
			this.monitoringAgents.put(agent.getId(), agent);
		}
	}
	
	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VacuumWorldMonitoringActionResult) {
			manageMonitoringBridgeRequest((VacuumWorldMonitoringActionResult) arg);
		}
		else if(o instanceof VacuumWorldMonitoringAgentActuator && arg instanceof VacuumWorldMonitoringEvent) {
			notifyObservers(new VWPair<VacuumWorldMonitoringEvent, VacuumWorldMonitoringContainer>((VacuumWorldMonitoringEvent) arg, this), VacuumWorldMonitoringPhysics.class);
		}
	}
	
	private void manageMonitoringBridgeRequest(VacuumWorldMonitoringActionResult result) {
		List<String> senderSensorIds = result.getRecipientsIds();
		notifyActor(senderSensorIds, result);
	}

	private void notifyActor(List<String> senderSensorIds, VacuumWorldMonitoringActionResult result) {
		notifyObserversIfNeeded(senderSensorIds, result);
	}
	
	private void notifyObserversIfNeeded(List<String> sensorsToNotifyIds, VacuumWorldMonitoringActionResult result) {
		List<CustomObserver> recipients = this.getObservers();
		
		for(CustomObserver recipient : recipients) {
			if(recipient instanceof VacuumWorldMonitoringAgentSensor) {
				notifyAgentSensorIfNeeded((VacuumWorldMonitoringAgentSensor) recipient, sensorsToNotifyIds, result);
			}
		}
	}
	
	private void notifyAgentSensorIfNeeded(VacuumWorldMonitoringAgentSensor recipient, List<String> sensorsToNotifyIds, VacuumWorldMonitoringActionResult result) {
		if(sensorsToNotifyIds.contains(recipient.getSensorId())) {
			recipient.update(this, result);
		}
	}

	public Map<String, VacuumWorldMonitoringAgent> getMonitoringAgentsMap() {
		return this.monitoringAgents;
	}
}