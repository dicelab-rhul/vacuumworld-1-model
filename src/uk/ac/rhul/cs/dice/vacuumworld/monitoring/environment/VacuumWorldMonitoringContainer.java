package uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringContainer extends EnvironmentalSpace {
    private Map<String, VacuumWorldMonitoringAgent> monitoringAgents;

    public VacuumWorldMonitoringContainer() {
	this.monitoringAgents = new HashMap<>();
    }

    public void addMonitoringAgents(Map<String, VacuumWorldMonitoringAgent> monitoringAgents) {
	this.monitoringAgents = monitoringAgents;
    }

    public void addMonitoringAgents(List<VacuumWorldMonitoringAgent> monitoringAgents) {
	for (VacuumWorldMonitoringAgent agent : monitoringAgents) {
	    this.monitoringAgents.put(agent.getId(), agent);
	}
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldMonitoringPhysics && arg instanceof VacuumWorldMonitoringActionResult) {
	    manageMonitoringPhysicsRequest((VacuumWorldMonitoringActionResult) arg);
	}
	else if (o instanceof VacuumWorldMonitoringAgentActuator && arg instanceof VacuumWorldMonitoringEvent) {
	    notifyObservers(new VWPair<VacuumWorldMonitoringEvent, VacuumWorldMonitoringContainer>((VacuumWorldMonitoringEvent) arg, this), VacuumWorldMonitoringPhysics.class);
	}
    }

    private void manageMonitoringPhysicsRequest(VacuumWorldMonitoringActionResult result) {
	logResult(result);
	getObservers().stream().filter(recipient -> recipient instanceof VacuumWorldMonitoringAgentSensor).forEach(recipient -> notifyAgentSensorIfNeeded((VacuumWorldMonitoringAgentSensor) recipient, result.getRecipientsIds(), result));
    }

    private void logResult(VacuumWorldMonitoringActionResult result) {
	switch (result.getActionResult()) {
	case ACTION_DONE:
	    VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was successful!");
	    break;
	case ACTION_FAILED:
	    VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was recognized as possible, but it failed during the execution!");
	    break;
	case ACTION_IMPOSSIBLE:
	    VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + result.getActorId() + ": the action was recognized as impossible and it was not performed!");
	    break;
	default:
	    throw new IllegalArgumentException(VWUtils.ACTOR + result.getActorId() + ": unknown result: " + result.getActionResult());
	}
    }

    private void notifyAgentSensorIfNeeded(VacuumWorldMonitoringAgentSensor recipient, List<String> sensorsToNotifyIds, VacuumWorldMonitoringActionResult result) {
	if (sensorsToNotifyIds.contains(recipient.getSensorId())) {
	    recipient.update(this, result);
	}
    }

    public Map<String, VacuumWorldMonitoringAgent> getMonitoringAgentsMap() {
	return this.monitoringAgents;
    }
}