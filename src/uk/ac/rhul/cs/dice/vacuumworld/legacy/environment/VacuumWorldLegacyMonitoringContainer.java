package uk.ac.rhul.cs.dice.vacuumworld.legacy.environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Agent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractSensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurningAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor.VacuumWorldMonitorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor.VacuumWorldMonitorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.VWEvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.VWEvaluatorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldLegacyMonitoringContainer extends EnvironmentalSpace {
	private List<Agent> monitoringAgents;
	private VacuumWorldMonitoringPhysics physics;
	private VacuumWorldSpace subContainerSpace;
	private VacuumWorldSpaceRepresentation vacuumWorldSpaceRepresentation;

	private Logger logger;

	public VacuumWorldLegacyMonitoringContainer(VacuumWorldMonitoringPhysics physics, VacuumWorldSpace space) {
		this.physics = physics;
		this.subContainerSpace = space;
		this.monitoringAgents = new ArrayList<>();
		this.vacuumWorldSpaceRepresentation = new VacuumWorldSpaceRepresentation();
		
		initLogger();
	}

	private void initLogger() {
		if (ConfigData.getLoggingFlag()) {
			File lck = new File("logs/eval/container.log.lck");
			File log = new File("logs/eval/container.log");
			
			deleteFileIfNecessary(lck, log);
			
			this.logger = VWUtils.fileLogger(log.getPath(), true);
		}
	}

	private void deleteFileIfNecessary(File lck, File log) {
		try {
			if (lck.exists()) {
				Files.delete(lck.toPath());
				Files.delete(log.toPath());
			}
		} 
		catch (IOException e) {
			VWUtils.log(e);
		}
	}

	public List<VacuumWorldMonitorAgent> getMonitorAgents() {
		List<VacuumWorldMonitorAgent> list = new ArrayList<>();
		
		for (Agent a : this.monitoringAgents) {
			if (a instanceof VacuumWorldMonitorAgent) {
				list.add((VacuumWorldMonitorAgent) a);
			}
		}
		return list;
	}

	public List<VWObserverAgent> getObserverAgents() {
		List<VWObserverAgent> list = new ArrayList<>();
		
		for (Agent a : this.monitoringAgents) {
			if (a instanceof VWObserverAgent) {
				list.add((VWObserverAgent) a);
			}
		}
		return list;
	}

	public List<VWEvaluatorAgent> getEvaluatorAgents() {
		List<VWEvaluatorAgent> list = new ArrayList<>();
		
		for (Agent a : this.monitoringAgents) {
			if (a instanceof VWEvaluatorAgent) {
				list.add((VWEvaluatorAgent) a);
			}
		}
		return list;
	}

	public VacuumWorldMonitoringPhysics getPhysics() {
		return this.physics;
	}

	public VacuumWorldSpace getSubContainerSpace() {
		return this.subContainerSpace;

	}

	public void addMonitorAgent(VacuumWorldMonitorAgent agent) {
		this.monitoringAgents.add(agent);
		this.addObserver(agent.getSensors().get(agent.getActionResultSensorIndex()));
		((VacuumWorldMonitorActuator) agent.getActuators().get(agent.getActionActuatorIndex())).addObserver(this);
	}

	public void addObserverAgent(VWObserverAgent agent) {
		this.monitoringAgents.add(agent);
		this.addObserver(agent.getSensors().get(agent.getActionResultSensorIndex()));
		((VWObserverActuator) agent.getActuators().get(agent.getActionActuatorIndex())).addObserver(this);
	}

	public void addEvaluatorAgent(VWEvaluatorAgent agent) {
		this.monitoringAgents.add(agent);
		((VWEvaluatorActuator) agent.getActuators().get(agent.getActionActuatorIndex())).addObserver(this);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldPhysics && arg instanceof MonitoringUpdateEvent) {
			manageSubContainerMessage((MonitoringUpdateEvent) arg);
		}
		else if ((o instanceof VWObserverActuator || o instanceof VacuumWorldMonitorActuator) && arg instanceof MonitoringEvent) {
			manageActuatorRequest((MonitoringEvent) arg);
		}
		else if (o instanceof VacuumWorldMonitoringPhysics && DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			managePhysicsRequest((DefaultActionResult) arg);
		}
	}

	private void manageSubContainerMessage(MonitoringUpdateEvent event) {
		if (ConfigData.getLoggingFlag()) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), "Message from subcontainer: " + event.represent());
			this.logger.info(event.getAction().getClass().getSimpleName() + ":" + event.getResult() + ":" + event.getActor().toString());
		}

		if (event.getResult().equals(ActionResult.ACTION_DONE) && !(event.getActor() instanceof User)) {
			AgentRepresentation agent = this.vacuumWorldSpaceRepresentation.getAgent((String) ((AbstractAgent<?,?>) event.getActor()).getId());
			EnvironmentalAction a = event.getAction();
			agent.setClean(false);
			agent.setSuccessfulClean(false);
			manageAction(a, agent);
		}
	}

	private void manageAction(EnvironmentalAction action, AgentRepresentation agent) {
		if (action instanceof SpeechAction) {
			manageSpeechAction((SpeechAction) action, agent);
		} 
		else {
			manageActionHelper(action, agent);
		}
	}
	
	private void manageActionHelper(EnvironmentalAction action, AgentRepresentation agent) {
		// There is no last speech action
		agent.setLastSpeechAction(null);
		
		if (action instanceof CleanAction) {
			manageClean(agent);
		}
		else if (action instanceof MoveAction) {
			manageMove(agent);
		}
		else if (action instanceof TurningAction) {
			manageTurn(action, agent);
		}
	}

	private void manageSpeechAction(SpeechAction a, AgentRepresentation agent) {
		this.vacuumWorldSpaceRepresentation.getSpeechActions().add(a);
		agent.setLastSpeechAction(a);
	}

	private void manageTurn(EnvironmentalAction a, AgentRepresentation agent) {
		if (a instanceof TurnLeftAction) {
			agent.setDirection(agent.getDirection().getLeftDirection());
		}
		else if (a instanceof TurnRightAction) {
			agent.setDirection(agent.getDirection().getRightDirection());
		}
	}

	private void manageMove(AgentRepresentation agent) {
		VacuumWorldCoordinates coord = new VacuumWorldCoordinates(agent.getX(), agent.getY());
		coord = coord.getNewCoordinates(agent.getDirection());
		agent.setX(coord.getX());
		agent.setY(coord.getY());
	}

	private void manageClean(AgentRepresentation agent) {
		agent.setClean(true);
		VacuumWorldCoordinates coord = new VacuumWorldCoordinates(agent.getX(), agent.getY());

		if (this.vacuumWorldSpaceRepresentation.getDirts().get(coord) != null) {
			this.vacuumWorldSpaceRepresentation.dirtCleaned(coord);
			agent.setSuccessfulClean(true);
		}
	}

	private void managePhysicsRequest(DefaultActionResult result) {
		notifyAgentsSensors(result, result.getRecipientsIds());
	}

	private void manageActuatorRequest(MonitoringEvent event) {
		notifyObservers(new Object[] { event, this }, VacuumWorldMonitoringPhysics.class);
	}

	private void notifyAgentsSensors(Object arg, List<String> sensorsIds) {
		List<CustomObserver> recipients = this.getObservers();

		for (CustomObserver recipient : recipients) {
			notifyIfNeeded(recipient, arg, sensorsIds);
		}
	}

	private void notifyIfNeeded(CustomObserver recipient, Object arg, List<String> sensorsIds) {
		if (recipient instanceof AbstractSensor) {
			AbstractSensor<?> s = (AbstractSensor<?>) recipient;
			notifySensorsIfNeeded(s, arg, sensorsIds);
		}
	}

	private void notifySensorsIfNeeded(AbstractSensor<?> s, Object arg, List<String> sensorsIds) {
		for (String sensorId : sensorsIds) {
			if (s.getSensorId().equals(sensorId)) {
				s.update(this, arg);
			}
		}
	}

	public VacuumWorldSpaceRepresentation getVacuumWorldSpaceRepresentation() {
		return this.vacuumWorldSpaceRepresentation;
	}

	public void setVacuumWorldSpaceRepresentation(VacuumWorldSpaceRepresentation vacuumWorldSpaceRepresentation) {
		this.vacuumWorldSpaceRepresentation = vacuumWorldSpaceRepresentation;
	}

	/**
	 * Creates the representation of VacuumWorldSpace. Should only be called
	 * when the real VacuumWorldSpace is fully set up; contains all agents and
	 * dirt etc.
	 */
	public void createVacuumWorldSpaceRepresentation() {
		Collection<Location> locations = this.subContainerSpace.getLocations();

		for (Location location : locations) {
			if (location instanceof VacuumWorldLocation) {
				createVacuumWorldSpaceRepresentation((VacuumWorldLocation) location);
			}
		}
	}

	private void createVacuumWorldSpaceRepresentation(VacuumWorldLocation location) {
		checkForAgent(location);
		checkForObstacle(location);
	}

	private void checkForObstacle(VacuumWorldLocation location) {
		if (location.isDirtPresent()) {
			Dirt dirt = location.getDirt();
			checkForDirt(dirt, location);
		}
	}

	private void checkForDirt(Dirt dirt, VacuumWorldLocation location) {
		if (dirt != null) {
			this.vacuumWorldSpaceRepresentation.getDirts().put(new VacuumWorldCoordinates(location.getCoordinates().getX(), location.getCoordinates().getY()), new DirtRepresentation(generateDirtId(location.getCoordinates().getX(), location.getCoordinates().getY()), ((DirtAppearance) dirt.getExternalAppearance()).getDirtType()));
		}
	}

	private String generateDirtId(int x, int y) {
		return "d_" + x + y;
	}

	private void checkForAgent(VacuumWorldLocation location) {
		if (location.isAnAgentPresent()) {
			String agentId = (String) location.getAgent().getId();
			AgentRepresentation rep = new AgentRepresentation(agentId, ((VacuumWorldAgentAppearance) location.getAgent().getExternalAppearance()).getType(), location.getAgent().getSensors().size(), location.getAgent().getActuators().size(), location.getAgent().getFacingDirection(), location.getAgent().getCurrentLocation().getX(), location.getAgent().getCurrentLocation().getY());

			this.vacuumWorldSpaceRepresentation.getAgents().put(agentId, rep);
		}
	}
}