package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.DropDirtAction;
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
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Obstacle;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AlreadyLockedException;
import uk.ac.rhul.cs.dice.vacuumworld.environment.Lockable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldPhysics extends AbstractPhysics implements VacuumWorldPhysicsInterface {
	private ConcurrentMap<Long, VacuumWorldCleaningAgent> activeAgents;
	private ConcurrentMap<Long, VacuumWorldMonitoringAgent> activeMonitoringAgents;
	private ConcurrentMap<Long, User> activeUsers;
	private ConcurrentMap<Long, List<String>> sensorsToNotify;

	public VacuumWorldPhysics() {
		this.activeAgents = new ConcurrentHashMap<>();
		this.activeUsers = new ConcurrentHashMap<>();
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
			String actorId = getActorId(event);
			VacuumWorldActionResult toReturn = attempt(result, actorId);
			doPerceptionAndSensorIds(toReturn, context);
			
			return toReturn;
		}
	}

	private String getActorId(Event event) {
		String actorId = getActorId();
		
		if(actorId == null) {
			actorId = getActorIdFromEvent(event);
		}
		
		return actorId;
	}

	private String getActorIdFromEvent(Event event) {
		if(event instanceof AbstractEvent) {
			Actor actor = ((AbstractEvent) event).getActor();
			
			if(actor instanceof AbstractAgent) {
				return ((AbstractAgent<?, ?>) actor).getId().toString();
			}
		}
		
		return null;
	}

	private VacuumWorldActionResult attempt(Result result, String actorId) {
		if (result instanceof VacuumWorldActionResult) {
			return (VacuumWorldActionResult) result;
		}
		else if (result instanceof DefaultActionResult) {
			return new VacuumWorldActionResult((DefaultActionResult) result, actorId);
		}
		else {
			throw new IllegalArgumentException(VWUtils.ACTOR + actorId + ": unknown result: " + result.getClass().getSimpleName());
		}
	}

	private void initMaps(Event event) {
		Actor actor = event.getActor();
		
		if(actor instanceof VacuumWorldCleaningAgent) {
			this.activeAgents.put(Thread.currentThread().getId(), (VacuumWorldCleaningAgent) actor);
		}
		else if(actor instanceof User) {
			this.activeUsers.put(Thread.currentThread().getId(), (User) actor);
		}
		
		this.sensorsToNotify.putIfAbsent(Thread.currentThread().getId(), new ArrayList<>());
		this.sensorsToNotify.get(Thread.currentThread().getId()).add(((VacuumWorldEvent) event).getSensorToCallBackId());
	}

	private void doPerceptionAndSensorIds(VacuumWorldActionResult result, Space context) {
		long threadId = Thread.currentThread().getId();
		VacuumWorldPerception newPerception = null;
		
		if(isCurrentActorAgent()) {
			newPerception = perceiveByAgent((VacuumWorldSpace) context, this.activeAgents.get(threadId).getPerceptionRange(), this.activeAgents.get(threadId).canSeeBehind());
		}
		else if(isCurrentActorUser()) {
			newPerception = new VacuumWorldPerception(((VacuumWorldSpace) context).getFullGrid(), getCurrentActorCoordinates());
		}
		
		result.setPerception(newPerception);
		
		if (!result.getActionResult().equals(ActionResult.ACTION_DONE)) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	private synchronized VacuumWorldCoordinates getCurrentActorCoordinates() {
		if(isCurrentActorAgent()) {
			return getActiveAgent().getCurrentLocation();
		}
		else if(isCurrentActorUser()) {
			return getActiveUser().getCurrentLocation();
		}
		else {
			return null;
		}
	}
	
	private synchronized VacuumWorldLocation getCurrentActorLocation(VacuumWorldSpace context) {
		VacuumWorldCoordinates coordinates = getCurrentActorCoordinates();
		
		return coordinates == null ? null : context.getLocation(coordinates);
	}

	private synchronized void releaseWriteLockIfNecessary(Lockable lockable) {
		lockable.releaseExclusiveWriteLock();
	}
	
	private synchronized void releaseReadLockIfNecessary(Lockable lockable) {
		lockable.releaseSharedReadLock();
	}
	
	private String getActorId() {
		long threadId = Thread.currentThread().getId();
		
		if(this.activeAgents.containsKey(threadId)) {
			return this.activeAgents.get(threadId).getId();
		}
		else if(this.activeUsers.containsKey(threadId)) {			
			return this.activeUsers.get(threadId).getId();
		}
		else {
			return null;
		}
	}
	
	private VacuumWorldCleaningAgent getActiveAgent() {
		long threadId = Thread.currentThread().getId();
		
		if(this.activeAgents.containsKey(threadId)) {
			return this.activeAgents.get(threadId);
		}
		else {
			return null;
		}
	}
	
	private User getActiveUser() {
		long threadId = Thread.currentThread().getId();
		
		if(this.activeUsers.containsKey(threadId)) {
			return this.activeUsers.get(threadId);
		}
		else {
			return null;
		}
	}
	
	private boolean isCurrentActorAgent() {
		long threadId = Thread.currentThread().getId();
		
		return this.activeAgents.containsKey(threadId);
	}
	
	private boolean isCurrentActorUser() {
		long threadId = Thread.currentThread().getId();
		
		return this.activeUsers.containsKey(threadId);
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
		long threadId = Thread.currentThread().getId();
		
		if(isCurrentActorAgent()) {
			return this.activeAgents.get(threadId).getFacingDirection() == action.getActorOldFacingDirection().getLeftDirection();
		}
		else if(isCurrentActorUser()) {
			return this.activeUsers.get(threadId).getFacingDirection() == action.getActorOldFacingDirection().getLeftDirection();
		}
		else {
			return false;
		}
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
		VacuumWorldLocation actorLocation = getCurrentActorLocation(context);
		actorLocation.getExclusiveWriteLock();
		
		if(isCurrentActorAgent()) {
			action.setActorOldFacingDirection(actorLocation.getAgent().getFacingDirection());
			actorLocation.getAgent().turn(rightOrLeft);
		}
		else if(isCurrentActorUser()) {
			action.setActorOldFacingDirection(actorLocation.getUser().getFacingDirection());
			actorLocation.getUser().turn(rightOrLeft);
		}
		
		actorLocation.releaseExclusiveWriteLock();
		
		String actorId = getActorId();
		logTurn(action, actorLocation, actorId);
		
		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}

	private void logTurn(TurningAction action, VacuumWorldLocation actorLocation, String actorId) {
		if(isCurrentActorAgent()) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + " old facing direction: " + action.getActorOldFacingDirection() + ", new facing direction: " + actorLocation.getAgent().getFacingDirection() + ".");
		}
		else if(isCurrentActorUser()) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + " old facing direction: " + action.getActorOldFacingDirection() + ", new facing direction: " + actorLocation.getUser().getFacingDirection() + ".");
		}
	}

	@Override
	public synchronized boolean succeeded(TurnRightAction action, Space context) {
		long threadId = Thread.currentThread().getId();
		
		if(isCurrentActorAgent()) {
			return this.activeAgents.get(threadId).getFacingDirection() == action.getActorOldFacingDirection().getRightDirection();
		}
		else if(isCurrentActorUser()) {
			return this.activeUsers.get(threadId).getFacingDirection() == action.getActorOldFacingDirection().getRightDirection();
		}
		else {
			return false;
		}
	}

	@Override
	public synchronized boolean isPossible(MoveAction action, Space context) {
		long threadId = Thread.currentThread().getId();
		
		ActorFacingDirection actorFacingDirection = isCurrentActorAgent() ? this.activeAgents.get(threadId).getFacingDirection() : isCurrentActorUser() ? this.activeUsers.get(threadId).getFacingDirection() : null;
		VacuumWorldLocation actorLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldCoordinates originalCooridinates = actorLocation.getCoordinates();
		VacuumWorldLocation targetLocation = ((VacuumWorldSpace) context).getFrontLocation(originalCooridinates, actorFacingDirection);

		return !checkForWall(actorLocation, actorFacingDirection) && !checkForObstacle(targetLocation);
	}

	private synchronized boolean checkForWall(VacuumWorldLocation actorLocation, ActorFacingDirection actorFacingDirection) {
		return actorLocation.getNeighborLocationType(actorFacingDirection) == VacuumWorldLocationType.WALL;
	}

	private synchronized boolean checkForObstacle(VacuumWorldLocation targetLocation) {
		if (targetLocation == null) {
			return true;
		}

		return checkForGenericObstacle(targetLocation) || checkForAgent(targetLocation) || checkForUser(targetLocation);
	}

	private synchronized boolean checkForUser(VacuumWorldLocation targetLocation) {
		return targetLocation.getUser() != null;
	}

	private synchronized boolean checkForAgent(VacuumWorldLocation targetLocation) {
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
		long threadId = Thread.currentThread().getId();
		
		ActorFacingDirection actorFacingDirection = isCurrentActorAgent() ? this.activeAgents.get(threadId).getFacingDirection() : isCurrentActorUser() ? this.activeUsers.get(threadId).getFacingDirection() : null;
		VacuumWorldLocation actorLocation = getCurrentActorLocation(context);
		VacuumWorldCoordinates originalCooridinates = actorLocation.getCoordinates();
		VacuumWorldLocation targetLocation = context.getFrontLocation(originalCooridinates, actorFacingDirection);

		actorLocation.getExclusiveWriteLock();
		targetLocation.getExclusiveWriteLock();

		action.setOldLocationCoordinates(originalCooridinates);

		return doMove(actorLocation, targetLocation, originalCooridinates, actorFacingDirection, context, threadId);
	}

	private Result doMove(VacuumWorldLocation actorLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection actorFacingDirection, VacuumWorldSpace context, long threadId) {
		if(isCurrentActorAgent()) {
			context.getLocation(actorLocation.getCoordinates()).removeAgent();
			targetLocation.addAgent(this.activeAgents.get(threadId));
			targetLocation.getAgent().setCurrentLocation(originalCooridinates.getNewCoordinates(actorFacingDirection));
			this.activeAgents.get(Thread.currentThread().getId()).setCurrentLocation(originalCooridinates.getNewCoordinates(actorFacingDirection));
		}
		else if(isCurrentActorUser()) {
			context.getLocation(actorLocation.getCoordinates()).removeUser();
			targetLocation.addUser(this.activeUsers.get(threadId));
			targetLocation.getUser().setCurrentLocation(originalCooridinates.getNewCoordinates(actorFacingDirection));
			this.activeUsers.get(Thread.currentThread().getId()).setCurrentLocation(originalCooridinates.getNewCoordinates(actorFacingDirection));
		}
		
		actorLocation.releaseExclusiveWriteLock();
		targetLocation.releaseExclusiveWriteLock();

		String actorId = getActorId();
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + " old position: " + originalCooridinates + ", new position: " + targetLocation.getCoordinates() + ", facing direction: " + actorFacingDirection + ".");
		
		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
	}

	private synchronized Result manageFailedMove(Exception e, VacuumWorldSpace context) {
		long threadId = Thread.currentThread().getId();
		ActorFacingDirection actorFacingDirection = null;
		
		if(isCurrentActorAgent()) {
			actorFacingDirection = this.activeAgents.get(threadId).getFacingDirection();
		}
		else if(isCurrentActorUser()) {
			actorFacingDirection = this.activeUsers.get(threadId).getFacingDirection();
		}
		
		VacuumWorldCoordinates originalCooridinates = getCurrentActorCoordinates();
		
		return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation(context), context.getFrontLocation(originalCooridinates, actorFacingDirection)), e);
	}
	
	private synchronized Result manageFailedAction(boolean readUnlock, boolean writeUnlock, List<VacuumWorldLocation> readLockedLocations, List<VacuumWorldLocation> writeLockedLocations, Exception e) {
		VWUtils.log(e);
		String actorId = getActorId();
		removeCurrentActor();
		releaseLocksIfNecessary(readUnlock, writeUnlock, readLockedLocations, writeLockedLocations);
		
		return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
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
		VacuumWorldLocation actorLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldLocation actorOldLocation = ((VacuumWorldSpace) context).getLocation(action.getOldLocationCoordinates());

		return !(actorOldLocation.isAnAgentPresent()) && sameActors(actorLocation);
	}

	private boolean sameActors(VacuumWorldLocation actorLocation) {
		long threadId = Thread.currentThread().getId();
		
		if(isCurrentActorAgent()) {
			return this.activeAgents.get(threadId).equals(actorLocation.getAgent());
		}
		else if(isCurrentActorUser()) {
			return this.activeUsers.get(threadId).equals(actorLocation.getUser());
		}
		else {
			return false;
		}
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
		DirtType dirtType = ((DirtAppearance) agentLocation.getDirt().getExternalAppearance()).getDirtType();
		agentLocation.removeDirt();
		agentLocation.releaseExclusiveWriteLock();
		
		String actorId = getActorId();
		VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + actorId + " cleaned a " + dirtType.toString() + " piece of dirt on location " + agentLocation.getCoordinates().toString() + ".");
		
		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
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
			String actorId = getActorId();
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
		catch (Exception e) {
			return manageFailedAction(false, false, null, null, e);
		}
	}

	private synchronized VacuumWorldPerception perceiveByAgent(VacuumWorldSpace context, int perceptionRange, boolean canSeeBehind) {
		VacuumWorldCleaningAgent current = this.activeAgents.get(Thread.currentThread().getId());
		ActorFacingDirection direction = current.getFacingDirection();
		int[] overheads = getOverheads(direction, perceptionRange, canSeeBehind);

		return perceive(context, overheads);
	}

	private synchronized int[] getOverheads(ActorFacingDirection facingDirection, int perceptionRange, boolean canSeeBehind) {
		int[] overheads = new int[4];
		int counter = 0;
		
		for(ActorFacingDirection direction : ActorFacingDirection.values()) {
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
				VacuumWorldLocation location = context.getLocation(coordinates);

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
		String actorId = getActorId();
		VacuumWorldActionResult actionResult = new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
		String message = result.getPayload().getPayload();
		String logMessage = buildSpeechActionLogMessage(actorId, message, result.getRecipientsIds());
		VWUtils.logWithClass(this.getClass().getSimpleName(), logMessage);
		
		return new VacuumWorldSpeechPerceptionResultWrapper(result, actionResult);
	}

	private String buildSpeechActionLogMessage(String actorId, String message, List<String> recipientsIds) {
		StringBuilder builder = new StringBuilder(VWUtils.ACTOR + actorId + " spoke to");
		
		if(recipientsIds.isEmpty()) {
			builder.append(" nobody ");
		}
		else {
			addAllReciptientsToLogString(builder, recipientsIds);
		}
		
		builder.append("saying \"" + message + "\".");
		
		return builder.toString();
	}

	private void addAllReciptientsToLogString(StringBuilder builder, List<String> recipientsIds) {
		builder.append(":\n");
		
		for(String recipientId : recipientsIds) {
			builder.append("  " + VWUtils.ACTOR + recipientId + "\n");
		}
		
		builder.append("...");
	}

	private VacuumWorldSpeechActionResult createSpeechActionResult(SpeechAction action, VacuumWorldSpace context) {
		VacuumWorldSpeechActionResult result = new VacuumWorldSpeechActionResult(ActionResult.ACTION_DONE, action);
		
		if (!VWUtils.isCollectionNotNullAndNotEmpty(result.getRecipientsIds())) {
			List<String> recipientsIds = new ArrayList<>();
			context.getAgents().forEach((VacuumWorldCleaningAgent agent) -> addAgentIdToRecipientsList(agent, recipientsIds, result)); 
			addUserIdToRecipientsListIfNecessary(context, recipientsIds, result);
			result.setRecipientsIds(recipientsIds);
		}
		
		return result;
	}

	private void addUserIdToRecipientsListIfNecessary(VacuumWorldSpace context, List<String> recipientsIds, VacuumWorldSpeechActionResult result) {
		User user = context.getUser();
		
		if(user == null) {
			return;
		}
		
		String candidateId = user.getId();
		
		if(!candidateId.equals(result.getSenderId())) {
			recipientsIds.add(candidateId);
		}
	}

	private void addAgentIdToRecipientsList(VacuumWorldCleaningAgent agent, List<String> recipientsIds, VacuumWorldSpeechActionResult result) {
		String candidateId = agent.getId();
		
		if(!candidateId.equals(result.getSenderId())) {
			recipientsIds.add(candidateId);
		}
	}
	
	@Override
	public synchronized boolean succeeded(SpeechAction action, Space context) {
		return true;
	}
	
	@Override
	public synchronized boolean isPossible(DropDirtAction action, Space context) {
		return !getCurrentActorLocation((VacuumWorldSpace) context).isDirtPresent();
	}

	@Override
	public synchronized boolean isNecessary(DropDirtAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(DropDirtAction action, Space context) {
		try {
			VacuumWorldLocation currentActorLocation = getCurrentActorLocation((VacuumWorldSpace) context);
			currentActorLocation.getExclusiveWriteLock();
			
			Double[] dimensions = new Double[] { (double) 1, (double) 1 };
			String name = "Dirt";
			currentActorLocation.setDirt(new Dirt(new DirtAppearance(name, dimensions, action.getDirtToDropType())));
			
			currentActorLocation.releaseExclusiveWriteLock();
			String actorId = getActorId();
			VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + actorId + " dropped a " + action.getDirtToDropType().toString() + " piece of dirt on location " + currentActorLocation.getCoordinates().toString() + ".");
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null, actorId, this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
		catch(Exception e) {
			return manageFailedAction(false, true, null, Arrays.asList(getCurrentActorLocation((VacuumWorldSpace) context)), e);
		}
	}

	@Override
	public synchronized boolean succeeded(DropDirtAction action, Space context) {
		VacuumWorldLocation currentActorLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		
		if(!currentActorLocation.isDirtPresent()) {
			return false;
		}
		
		return ((DirtAppearance) currentActorLocation.getDirt().getExternalAppearance()).getDirtType().equals(action.getDirtToDropType());
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldSpace && arg instanceof Object[]) {
			manageEnvironmentRequest((Object[]) arg);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof Object[]) {
			long threadId = Thread.currentThread().getId();
			Result result = manageBridgeRequest((Object[]) arg, threadId);
			this.activeMonitoringAgents.remove(threadId);
			this.sensorsToNotify.remove(threadId);
			notifyObservers(result, VacuumWorldMonitoringBridge.class);
		}
	}

	private synchronized Result manageBridgeRequest(Object[] arg, long threadId) {
		if (arg.length != 2) {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, new IllegalArgumentException(), null);
		}
		
		if(TotalPerceptionAction.class.isAssignableFrom(arg.getClass())) {
			this.activeMonitoringAgents.putIfAbsent(threadId, (VacuumWorldMonitoringAgent) ((TotalPerceptionAction) arg[0]).getActor());
			this.sensorsToNotify.putIfAbsent(threadId, Arrays.asList(this.activeMonitoringAgents.get(threadId).getId()));
			
			VacuumWorldMonitoringPerception perception = new VacuumWorldMonitoringPerception(((VacuumWorldSpace) arg[1]).getFullGrid(), null);
			
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, this.activeMonitoringAgents.get(threadId).getId(), this.sensorsToNotify.get(threadId), perception);
		}
		
		else {
			return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_FAILED, new IllegalArgumentException(), null);
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
		notifyObservers(monitoringUpdateEvent, VacuumWorldLegacyMonitoringContainer.class);

		removeCurrentActor();
		this.sensorsToNotify.remove(Thread.currentThread().getId());

		notifyObservers(result, VacuumWorldSpace.class);
	}

	private void removeCurrentActor() {
		long threadId = Thread.currentThread().getId();
		
		if(isCurrentActorAgent()) {
			this.activeAgents.remove(threadId);
		}
		else if(isCurrentActorUser()) {
			this.activeUsers.remove(threadId);
		}
	}
}