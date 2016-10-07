package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurningAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPerceptionResultWrapper;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.Obstacle;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AlreadyLockedException;
import uk.ac.rhul.cs.dice.vacuumworld.environment.Lockable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldPhysics extends AbstractPhysics implements VacuumWorldPhysicsInterface {
	private ConcurrentMap<Long, VacuumWorldCleaningAgent> activeAgents;
	private ConcurrentMap<Long, List<String>> sensorsToNotify;

	public VacuumWorldPhysics() {
		this.activeAgents = new ConcurrentHashMap<>();
		this.sensorsToNotify = new ConcurrentHashMap<>();
	}

	@Override
	public synchronized Result attempt(Event event, Space context) {
		initMaps(event);
		
		Result result = event.getAction().attempt(this, context);
		
		if (result instanceof VacuumWorldSpeechPerceptionResultWrapper) {
			doPerceptionAndSensorIds(((VacuumWorldSpeechPerceptionResultWrapper) result).getPerceptionResult(), context);
			return result;
		}
		else {
			VacuumWorldActionResult toReturn = attempt(result);
			doPerceptionAndSensorIds(toReturn, context);
			return toReturn;
		}
	}

	private VacuumWorldActionResult attempt(Result result) {
		if (result instanceof VacuumWorldActionResult) {
			return (VacuumWorldActionResult) result;
		}
		else if (result instanceof DefaultActionResult) {
			return new VacuumWorldActionResult((DefaultActionResult) result);
		}
		else {
			Utils.logWithClass(this.getClass().getSimpleName(), "An unknown \"Result\" has been encountered in VacuumWorldPhysics: " + result.getClass() + ".");
			return null;
		}
	}

	private void initMaps(Event event) {
		this.activeAgents.put(Thread.currentThread().getId(), (VacuumWorldCleaningAgent) event.getActor());
		this.sensorsToNotify.putIfAbsent(Thread.currentThread().getId(), new ArrayList<>());
		this.sensorsToNotify.get(Thread.currentThread().getId()).add(((VacuumWorldEvent) event).getSensorToCallBackId());
	}

	private void doPerceptionAndSensorIds(VacuumWorldActionResult result, Space context) {
		VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context, this.activeAgents.get(Thread.currentThread().getId()).getPerceptionRange(), this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
		result.setPerception(newPerception);
		
		if (!result.getActionResult().equals(ActionResult.ACTION_DONE)) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	private synchronized VacuumWorldLocation getCurrentActorLocation(VacuumWorldSpace context) {
		VacuumWorldCoordinates coordinates = this.activeAgents.get(Thread.currentThread().getId()).getCurrentLocation();

		return (VacuumWorldLocation) context.getLocation(coordinates);
	}

	private synchronized void releaseWriteLockIfNecessary(Lockable lockable) {
		lockable.releaseExclusiveWriteLock();
	}
	
	private synchronized void releaseReadLockIfNecessary(Lockable lockable) {
		lockable.releaseSharedReadLock();
	}

	/**
	 * A turning action is always possible.
	 */
	@Override
	public synchronized boolean isPossible(TurnLeftAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnLeftAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnLeftAction action, Space context) {
		try {
			return doTurn((VacuumWorldSpace) context, action, false);

		}
		catch (Exception e) {
			return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation((VacuumWorldSpace) context)), e);
		}
	}

	@Override
	public synchronized boolean succeeded(TurnLeftAction action, Space context) {
		return this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection() == action.getAgentOldFacingDirection().getLeftDirection();
	}

	/**
	 * A turning action is always possible.
	 */
	@Override
	public synchronized boolean isPossible(TurnRightAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnRightAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnRightAction action, Space context) {
		try {
			return doTurn((VacuumWorldSpace) context, action, true);

		}
		catch (Exception e) {
			return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation((VacuumWorldSpace) context)), e);
		}
	}
	
	private synchronized Result doTurn(VacuumWorldSpace context, TurningAction action, boolean rightOrLeft) throws AlreadyLockedException {
		VacuumWorldLocation agentLocation = getCurrentActorLocation(context);
		agentLocation.getExclusiveWriteLock();
		action.setAgentOldFacingDirection(agentLocation.getAgent().getFacingDirection());
		agentLocation.getAgent().turn(rightOrLeft);
		agentLocation.releaseExclusiveWriteLock();

		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}

	@Override
	public synchronized boolean succeeded(TurnRightAction action, Space context) {
		return this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection() == action.getAgentOldFacingDirection().getRightDirection();
	}

	@Override
	public synchronized boolean isPossible(MoveAction action, Space context) {
		AgentFacingDirection agentFacingDirection = this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection();
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldCoordinates originalCooridinates = agentLocation.getCoordinates();
		VacuumWorldLocation targetLocation = ((VacuumWorldSpace) context).getFrontLocation(originalCooridinates, agentFacingDirection);

		return !checkForWall(agentLocation, agentFacingDirection) && !checkForObstacle(targetLocation);
	}

	private synchronized boolean checkForWall(VacuumWorldLocation agentLocation, AgentFacingDirection agentFacingDirection) {
		return agentLocation.getNeighborLocation(agentFacingDirection) == VacuumWorldLocationType.WALL;
	}

	private synchronized boolean checkForObstacle(VacuumWorldLocation targetLocation) {
		if (targetLocation == null) {
			return true;
		}

		return checkForGenericObstacle(targetLocation) || checkForAnotherAgent(targetLocation);
	}

	private synchronized boolean checkForAnotherAgent(VacuumWorldLocation targetLocation) {
		return targetLocation.getAgent() != null;
	}

	private synchronized boolean checkForGenericObstacle(VacuumWorldLocation targetLocation) {
		Obstacle potentialObstacle = targetLocation.getObstacle();

		if (potentialObstacle == null || potentialObstacle instanceof Dirt) {
			return false;
		}

		return true;
	}

	@Override
	public synchronized boolean isNecessary(MoveAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(MoveAction action, Space context) {
		try {
			return doMove(action, (VacuumWorldSpace) context);
		}
		catch (Exception e) {
			return manageFailedMove(e, (VacuumWorldSpace) context);
		}
	}
	
	private Result doMove(MoveAction action, VacuumWorldSpace context) throws AlreadyLockedException {
		AgentFacingDirection agentFacingDirection = this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection();
		VacuumWorldLocation agentLocation = getCurrentActorLocation(context);
		VacuumWorldCoordinates originalCooridinates = agentLocation.getCoordinates();
		VacuumWorldLocation targetLocation = context.getFrontLocation(originalCooridinates, agentFacingDirection);

		agentLocation.getExclusiveWriteLock();
		targetLocation.getExclusiveWriteLock();

		action.setOldLocationCoordinates(originalCooridinates);

		return doMove(agentLocation, targetLocation, originalCooridinates, agentFacingDirection, context);
	}

	private Result doMove(VacuumWorldLocation agentLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, AgentFacingDirection agentFacingDirection, VacuumWorldSpace context) {
		((VacuumWorldLocation) context.getLocation(agentLocation.getCoordinates())).removeAgent();
		targetLocation.addAgent(this.activeAgents.get(Thread.currentThread().getId()));
		targetLocation.getAgent().setCurrentLocation(originalCooridinates.getNewCoordinates(agentFacingDirection));
		this.activeAgents.get(Thread.currentThread().getId()).setCurrentLocation(originalCooridinates.getNewCoordinates(agentFacingDirection));
		agentLocation.releaseExclusiveWriteLock();
		targetLocation.releaseExclusiveWriteLock();

		Utils.logWithClass(this.getClass().getSimpleName(), "Agent: old position: " + originalCooridinates + ", new position: " + targetLocation.getCoordinates() + ", facing position: " + agentFacingDirection + ".");
		
		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}

	private synchronized Result manageFailedMove(Exception e, VacuumWorldSpace context) {
		AgentFacingDirection agentFacingDirection = this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection();
		VacuumWorldLocation agentLocation = getCurrentActorLocation(context);
		VacuumWorldCoordinates originalCooridinates = agentLocation.getCoordinates();
		
		return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation(context), context.getFrontLocation(originalCooridinates, agentFacingDirection)), e);
	}
	
	private synchronized Result manageFailedAction(boolean readUnlock, boolean writeUnlock, List<VacuumWorldLocation> readLockedLocations, List<VacuumWorldLocation> writeLockedLocations, Exception e) {
		Utils.log(e);
		this.activeAgents.remove(Thread.currentThread().getId());
		releaseLocksIfNecessary(readUnlock, writeUnlock, readLockedLocations, writeLockedLocations);
		
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}
	
	private synchronized void releaseLocksIfNecessary(boolean read, boolean write, List<VacuumWorldLocation> readLockLocations, List<VacuumWorldLocation> writeLockLocations) {
		if(read) {
			releaseReadLocksIfNecessary(readLockLocations);
		}
		
		if(write) {
			releaseWriteLocksIfNecessary(writeLockLocations);
		}
	}

	private void releaseWriteLocksIfNecessary(List<VacuumWorldLocation> writeLockLocations) {
		for(VacuumWorldLocation location : writeLockLocations) {
			releaseWriteLockIfNecessary(location);
		}
	}

	private void releaseReadLocksIfNecessary(List<VacuumWorldLocation> readLockLocations) {
		for(VacuumWorldLocation location : readLockLocations) {
			releaseReadLockIfNecessary(location);
		}
	}

	@Override
	public synchronized boolean succeeded(MoveAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldLocation agentOldLocation = (VacuumWorldLocation) ((EnvironmentalSpace) context).getLocation(action.getOldLocationCoordinates());

		return !(agentOldLocation.isAnAgentPresent()) && this.activeAgents.get(Thread.currentThread().getId()).equals(agentLocation.getAgent());
	}

	@Override
	public synchronized boolean isPossible(CleanAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		
		if (agentLocation == null) {
			return false;
		}
		
		if(!agentLocation.isDirtPresent()) {
			return false;
		}
		
		return checkCompatibility(agentLocation);
	}

	private boolean checkCompatibility(VacuumWorldLocation agentLocation) {
		VacuumWorldCleaningAgent agent = this.activeAgents.get(Thread.currentThread().getId());
		VacuumWorldAgentType agentType = ((VacuumWorldAgentAppearance) agent.getExternalAppearance()).getType();
		DirtType dirtType = ((DirtAppearance) agentLocation.getDirt().getExternalAppearance()).getDirtType();
		
		return DirtType.agentAndDirtCompatible(dirtType, agentType);
	}

	@Override
	public synchronized boolean isNecessary(CleanAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(CleanAction action, Space context) {
		try {
			return doClean((VacuumWorldSpace) context);
		}
		catch (Exception e) {
			return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation((VacuumWorldSpace) context)), e);
		}
	}

	private Result doClean(VacuumWorldSpace context) throws AlreadyLockedException {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		agentLocation.getExclusiveWriteLock();
		agentLocation.removeDirt();
		agentLocation.releaseExclusiveWriteLock();

		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}

	@Override
	public synchronized boolean succeeded(CleanAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		return !(agentLocation.isDirtPresent());
	}

	@Override
	public synchronized boolean isPossible(PerceiveAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(PerceiveAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(PerceiveAction action, Space context) {
		try {
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
		catch (Exception e) {
			return manageFailedAction(false, false, null, null, e);
		}
	}

	private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context, int perceptionRange, boolean canSeeBehind) {
		VacuumWorldCleaningAgent current = this.activeAgents.get(Thread.currentThread().getId());
		AgentFacingDirection direction = current.getFacingDirection();
		int[] overheads = getOverheads(direction, perceptionRange, canSeeBehind);

		return perceive(context, overheads);
	}

	private synchronized int[] getOverheads(AgentFacingDirection facingDirection, int perceptionRange, boolean canSeeBehind) {
		int[] overheads = new int[4];
		int counter = 0;
		
		for(AgentFacingDirection direction : AgentFacingDirection.values()) {
			overheads[counter++] = !canSeeBehind && direction.isOpposite(facingDirection) ? 0 : perceptionRange - 1;
		}
		
		return overheads;
	}
	
	private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context, int[] overheads) {
		VacuumWorldCoordinates currentCoordinates = getCurrentActorLocation(context).getCoordinates();
		int currentX = currentCoordinates.getX();
		int currentY = currentCoordinates.getY();

		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = perceive(context, overheads, currentX, currentY);
		return new VacuumWorldPerception(perception, currentCoordinates);
	}

	private synchronized Map<VacuumWorldCoordinates, VacuumWorldLocation> perceive(VacuumWorldSpace context, int[] overheads, int currentX, int currentY) {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = new HashMap<>();

		for (int i = currentX - overheads[2]; i <= currentX + overheads[3]; i++) {
			for (int j = currentY - overheads[0]; j <= currentY + overheads[1]; j++) {
				VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);
				VacuumWorldLocation location = (VacuumWorldLocation) context.getLocation(coordinates);

				if (location != null) {
					perception.put(coordinates, location);
				}
			}
		}

		return perception;
	}

	/**
	 * A perceiving action has no post-conditions to check.
	 */
	@Override
	public synchronized boolean succeeded(PerceiveAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isPossible(SpeechAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(SpeechAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(SpeechAction action, Space context) {
		try {
			return doSpeech(action, (VacuumWorldSpace) context);
		} 
		catch (Exception e) {
			return manageFailedAction(false, false, null, null, e);
		}
	}

	private Result doSpeech(SpeechAction action, VacuumWorldSpace context) {
		VacuumWorldSpeechActionResult result = createSpeechActionResult(action, context);
		VacuumWorldActionResult actionResult = new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread.currentThread().getId()));
		
		return new VacuumWorldSpeechPerceptionResultWrapper(result, actionResult);
	}

	private VacuumWorldSpeechActionResult createSpeechActionResult(SpeechAction action, VacuumWorldSpace context) {
		VacuumWorldSpeechActionResult result = new VacuumWorldSpeechActionResult(ActionResult.ACTION_DONE, action);
		List<String> sensorids = new ArrayList<>();
		
		if (result.getRecipientsIds() == null || result.getRecipientsIds().isEmpty()) {
			context.getAgents().forEach((VacuumWorldCleaningAgent agent) -> addSensorToList(agent, sensorids, result)); 
			result.setRecipientsIds(sensorids);
		}
		else {
			action.getRecipientsIds().forEach((String id) -> addSensorsToList(context.getAgentById(id), sensorids));
			result.setRecipientsIds(sensorids);
		}
		
		return result;
	}

	private synchronized void addSensorsToList(VacuumWorldCleaningAgent agent, List<String> sensorids) {
		String sensorId = ((VacuumWorldDefaultSensor) agent.getSensors().get(agent.getActionResultSensorIndex())).getSensorId();
		sensorids.add(sensorId);
	}

	public synchronized void addSensorToList(VacuumWorldCleaningAgent agent, List<String> sensorids, VacuumWorldSpeechActionResult result) {
		String sensorId = ((VacuumWorldDefaultSensor) agent.getSensors().get(agent.getActionResultSensorIndex())).getSensorId();
		
		if (!agent.getId().equals(result.getSender())) {
			sensorids.add(sensorId);
		}
	}
	
	@Override
	public boolean succeeded(SpeechAction action, Space context) {
		return true;
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldSpace && arg instanceof Object[]) {
			manageEnvironmentRequest((Object[]) arg);
		}
	}

	private synchronized void manageEnvironmentRequest(Object[] arg) {
		if (arg.length != 2) {
			return;
		}

		if (arg[0] instanceof VacuumWorldEvent && arg[1] instanceof VacuumWorldSpace) {
			attemptEvent((VacuumWorldEvent) arg[0], (VacuumWorldSpace) arg[1]);
		}
	}

	private synchronized void attemptEvent(VacuumWorldEvent event, VacuumWorldSpace context) {
		Result result = event.attempt(this, context);
		ActionResult code = result.getActionResult();
		MonitoringUpdateEvent monitoringUpdateEvent = new MonitoringUpdateEvent(event.getAction(), event.getTimestamp(), event.getActor(), code);
		notifyObservers(monitoringUpdateEvent, VacuumWorldMonitoringContainer.class);

		this.activeAgents.remove(Thread.currentThread().getId());
		this.sensorsToNotify.remove(Thread.currentThread().getId());

		notifyObservers(result, VacuumWorldSpace.class);
	}
}