package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.*;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.*;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.*;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.*;
import uk.ac.rhul.cs.dice.vacuumworld.environment.Lockable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringBridge;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.TurningDirection;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWPair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldPhysics extends AbstractPhysics implements VacuumWorldPhysicsInterface {
	private Map<String, List<AbstractActionReport>> attemptedActions;
	
	public VacuumWorldPhysics() {
		this.attemptedActions = new HashMap<>();
	}
	
	public Map<String, List<AbstractActionReport>> getAttemptedActions() {
		return this.attemptedActions;
	}
	
	public List<AbstractActionReport> getActionsAttemptedBySpecificActor(String actorId) {
		return this.attemptedActions.getOrDefault(actorId, null);
	}
	
	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldSpace && arg instanceof VWPair<?, ?>) {
			manageEnvironmentRequest((VWPair<?, ?>) arg);
		}
		else if(o instanceof VacuumWorldMonitoringBridge && arg instanceof VWPair<?, ?>) {
			manageSuperEnvironmentRequest((VWPair<?, ?>) arg);
		}
	}

	@Override
	public synchronized boolean isPossible(TurnLeftAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnLeftAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnLeftAction action, VacuumWorldSpace context) {
		VacuumWorldLocation actorLocation = getCurrentActorLocation(context, action.getActor());
		
		return doTurn(actorLocation, action, TurningDirection.LEFT);
	}

	@Override
	public synchronized boolean succeeded(TurnLeftAction action, VacuumWorldSpace context) {
		try {
			return checkTurningActionSuccess(action, context, TurningDirection.LEFT);
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isPossible(TurnRightAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnRightAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnRightAction action, VacuumWorldSpace context) {
		VacuumWorldLocation actorLocation = getCurrentActorLocation(context, action.getActor());
		
		return doTurn(actorLocation, action, TurningDirection.RIGHT);
	}

	@Override
	public synchronized boolean succeeded(TurnRightAction action, VacuumWorldSpace context) {
		try {
			return checkTurningActionSuccess(action, context, TurningDirection.RIGHT);
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isPossible(MoveAction action, VacuumWorldSpace context) {
		try {
			Actor actor = action.getActor();
			VacuumWorldLocation actorLocation = getCurrentActorLocation(context, actor);
			ActorFacingDirection actorFacingDirection = getActorFacingDirection(actorLocation, actor);
			VacuumWorldCoordinates originalCooridinates = actorLocation.getCoordinates();
			VacuumWorldLocation targetLocation = context.getFrontLocation(originalCooridinates, actorFacingDirection);

			return !checkForWall(actorLocation, actorFacingDirection) && !checkForObstacle(targetLocation);
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isNecessary(MoveAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(MoveAction action, VacuumWorldSpace context) {
		Actor actor = action.getActor();
		VacuumWorldLocation actorLocation = getCurrentActorLocation(context, actor);
		ActorFacingDirection actorFacingDirection = getActorFacingDirection(actorLocation, actor);
		VacuumWorldCoordinates originalCooridinates = actorLocation.getCoordinates();
		VacuumWorldLocation targetLocation = context.getFrontLocation(originalCooridinates, actorFacingDirection);
		
		return doMove(action, actor, actorLocation, targetLocation, originalCooridinates, actorFacingDirection);
	}	

	@Override
	public synchronized boolean succeeded(MoveAction action, VacuumWorldSpace context) {
		try {
			Actor actor = action.getActor();
			VacuumWorldLocation actorLocation = getCurrentActorLocation(context, actor);
			VacuumWorldLocation actorOldLocation = context.getLocation(action.getOldLocationCoordinates());

			return checkOldLocation(actorOldLocation, actor) && checkNewLocation(actorLocation, actor);
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isPossible(CleanAction action, VacuumWorldSpace context) {
		try {
			VacuumWorldLocation agentLocation = getAgentLocation(context, action.getActor());
			
			if (agentLocation == null) {
				return false;
			}
			
			if(!agentLocation.isDirtPresent()) {
				return false;
			}
			
			return checkCompatibility(agentLocation);
		}
		
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isNecessary(CleanAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(CleanAction action, VacuumWorldSpace context) {
		VacuumWorldLocation agentLocation = getAgentLocation(context, action.getActor());
		
		return doClean(agentLocation, action);
	}

	@Override
	public synchronized boolean succeeded(CleanAction action, VacuumWorldSpace context) {
		try {
			VacuumWorldLocation agentLocation = getAgentLocation(context, action.getActor());
			
			return !(agentLocation.isDirtPresent());
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isPossible(PerceiveAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(PerceiveAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(PerceiveAction action, VacuumWorldSpace context) {
		try {
			if(isCurrentActorAgent(action.getActor())) {
				logPerceive(action, getAgentLocation(context, (VacuumWorldCleaningAgent) action.getActor()), ((VacuumWorldCleaningAgent) action.getActor()).getFacingDirection());
			}
			else if(isCurrentActorUser(action.getActor())) {
				logPerceive(action, getUserLocation(context, (User) action.getActor()), ((User) action.getActor()).getFacingDirection());
			}
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		}
		catch (Exception e) {
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), e, null);
		}
	}

	@Override
	public synchronized boolean succeeded(PerceiveAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isPossible(SpeechAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(SpeechAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(SpeechAction action, VacuumWorldSpace context) {
		try {
			return doSpeech(action, context);
		} 
		catch (Exception e) {
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), e, null);
		}
	}

	@Override
	public synchronized boolean succeeded(SpeechAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isPossible(DropDirtAction action, VacuumWorldSpace context) {
		try {
			VacuumWorldLocation actorLocation = getCurrentActorLocation(context, action.getActor());
			
			return isCurrentActorUser(action.getActor()) && !actorLocation.isDirtPresent();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}

	@Override
	public synchronized boolean isNecessary(DropDirtAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public synchronized Result perform(DropDirtAction action, VacuumWorldSpace context) {
		try {
			VacuumWorldLocation currentActorLocation = getCurrentActorLocation(context, action.getActor());
			
			currentActorLocation.getExclusiveWriteLock();
			dropDirt(currentActorLocation, action);
			currentActorLocation.releaseExclusiveWriteLock();
			
			logDropDirt(action, currentActorLocation);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		}
		catch(Exception e) {
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), e, null);
		}
	}

	@Override
	public synchronized boolean succeeded(DropDirtAction action, VacuumWorldSpace context) {
		try {
			VacuumWorldLocation currentActorLocation = getCurrentActorLocation(context, action.getActor());
			
			if(!currentActorLocation.isDirtPresent()) {
				return false;
			}
			
			return currentActorLocation.getDirt().getExternalAppearance().getDirtType().equals(action.getDirtToDropType());
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}
	
	@Override
	public boolean isPossible(TotalPerceptionAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public boolean isNecessary(TotalPerceptionAction action, VacuumWorldSpace context) {
		return false;
	}

	@Override
	public Result perform(TotalPerceptionAction action, VacuumWorldSpace context) {
		VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + action.getActor().getId() + " requested a perception of the whole grid" + ".");
		
		return new VacuumWorldMonitoringActionResult(ActionResult.ACTION_DONE, action.getActor().getId(), new ArrayList<>(), null);
	}

	@Override
	public boolean succeeded(TotalPerceptionAction action, VacuumWorldSpace context) {
		return true;
	}

	@Override
	public synchronized boolean isPossible(VacuumWorldEvent event, VacuumWorldSpace context) {
		return event.getAction().isPossible(this, context);
	}

	@Override
	public synchronized boolean isNecessary(VacuumWorldEvent event, VacuumWorldSpace context) {
		return event.getAction().isNecessary(this, context);
	}

	@Override
	public synchronized Result attempt(VacuumWorldEvent event, VacuumWorldSpace context) {
		if(event.isPossible(this, context)) {
			Result result = event.perform(this, context);
			result.setRecipientsIds(Arrays.asList(event.getSensorToCallBackId()));
			
			if(!ActionResult.ACTION_DONE.equals(result.getActionResult())) {
				this.attemptedActions.get(event.getActor().getId().toString()).clear();
				storeActionFailedOutcome(event.getAction(), event.getActor(), getCurrentActorLocation(context, event.getActor()));
				
				return result;
			}
			
			if(!event.succeeded(this, context)) {
				this.attemptedActions.get(event.getActor().getId().toString()).clear();
				storeActionFailedOutcome(event.getAction(), event.getActor(), getCurrentActorLocation(context, event.getActor()));
				
				return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, event.getActor().getId().toString(), null, Arrays.asList(event.getSensorToCallBackId()));
			}
			else {
				return result;
			}
		}
		else {
			storeActionImpossibleOutcome(event.getAction(), event.getActor(), getCurrentActorLocation(context, event.getActor()));
			
			return new VacuumWorldActionResult(ActionResult.ACTION_IMPOSSIBLE, event.getActor().getId().toString(), null, Arrays.asList(event.getSensorToCallBackId()));
		}
	}

	@Override
	public synchronized Result perform(VacuumWorldEvent event, VacuumWorldSpace context) {
		return event.getAction().perform(this, context);
	}

	@Override
	public synchronized boolean succeeded(VacuumWorldEvent event, VacuumWorldSpace context) {
		return event.getAction().succeeded(this, context);
	}
	
	private void manageSuperEnvironmentRequest(VWPair<?, ?> arg) {
		manageEnvironmentRequest(arg);
	}
	
	private void manageEnvironmentRequest(VWPair<?, ?> pair) {
		if (pair.checkClasses(VacuumWorldEvent.class, VacuumWorldSpace.class)) {
			manageEnvironmentRequest((VacuumWorldEvent) pair.getFirst(), (VacuumWorldSpace) pair.getSecond());
		}
	}

	private void manageEnvironmentRequest(VacuumWorldEvent event, VacuumWorldSpace context) {
		if(!this.attemptedActions.containsKey(event.getActor().getId().toString())) {
			this.attemptedActions.put(event.getActor().getId().toString(), new ArrayList<>());
		}
		
		this.attemptedActions.get(event.getActor().getId().toString()).clear();
		
		Result result = event.attempt(this, context);
		result.setRecipientsIds(Arrays.asList(event.getSensorToCallBackId()));
		
		addPerceptionToResult(event.getActor(), result, context);
		notifyEnvironment(result);
	}

	private void notifyEnvironment(Result result) {
		if(result instanceof VacuumWorldActionResult) {
			notifyObservers(result, VacuumWorldSpace.class);
		}
		else if(result instanceof VacuumWorldMonitoringActionResult) {
			notifyObservers(result, VacuumWorldMonitoringBridge.class);
		}
	}

	private void addPerceptionToResult(Actor actor, Result result, VacuumWorldSpace context) {
		if(result instanceof VacuumWorldSpeechPerceptionResultWrapper) {
			addPerceptionToResultHelper(actor, ((VacuumWorldSpeechPerceptionResultWrapper) result).getPerceptionResult(), context);
		}
		else {
			addPerceptionToResultHelper(actor, result, context);
		}
	}
	
	private void addPerceptionToResultHelper(Actor actor, Result result, VacuumWorldSpace context) {
		if(actor instanceof VacuumWorldMonitoringAgent) {
			result.setPerception(new VacuumWorldMonitoringPerception(context.getFullGrid(), null));
		}
		else if(isCurrentActorAgent(actor)) {
			result.setPerception(perceiveByAgent((VacuumWorldCleaningAgent) actor, context));
		}
		else if(isCurrentActorUser(actor)) {
			result.setPerception(new VacuumWorldPerception(context.getFullGrid(), getCurrentActorLocation(context, actor).getCoordinates()));
		}
	}
	
	private VacuumWorldPerception perceiveByAgent(VacuumWorldCleaningAgent current, VacuumWorldSpace context) {
		int perceptionRange = current.getPerceptionRange();
		boolean canSeeBehind = current.canSeeBehind();
		ActorFacingDirection direction = current.getFacingDirection();
		int[] overheads = getOverheads(direction, perceptionRange, canSeeBehind);

		return perceive(current, context, overheads);
	}

	private int[] getOverheads(ActorFacingDirection facingDirection, int perceptionRange, boolean canSeeBehind) {
		int[] overheads = new int[4];
		int counter = 0;
		
		for(ActorFacingDirection direction : ActorFacingDirection.values()) {
			overheads[counter++] = !canSeeBehind && direction.isOpposite(facingDirection) ? 0 : perceptionRange - 1;
		}
		
		return overheads;
	}
	
	private VacuumWorldPerception perceive(VacuumWorldCleaningAgent current, VacuumWorldSpace context, int[] overheads) {
		VacuumWorldCoordinates currentCoordinates = getCurrentActorLocation(context, current).getCoordinates();
		int currentX = currentCoordinates.getX();
		int currentY = currentCoordinates.getY();

		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = perceive(context, overheads, currentX, currentY);
		return new VacuumWorldPerception(perception, currentCoordinates);
	}

	private Map<VacuumWorldCoordinates, VacuumWorldLocation> perceive(VacuumWorldSpace context, int[] overheads, int currentX, int currentY) {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = new HashMap<>();

		for (int i = currentX - overheads[2]; i <= currentX + overheads[3]; i++) {
			for (int j = currentY - overheads[0]; j <= currentY + overheads[1]; j++) {
				updatePerceptionIfNecessary(perception, i, j, context);
			}
		}

		return perception;
	}

	private void updatePerceptionIfNecessary(Map<VacuumWorldCoordinates, VacuumWorldLocation> perception, int i, int j, VacuumWorldSpace context) {
		VacuumWorldCoordinates coordinates = new VacuumWorldCoordinates(i, j);
		VacuumWorldLocation location = context.getLocation(coordinates);

		if (location != null) {
			perception.put(coordinates, location);
		}
	}
	
	private void unlockLocationsIfNecessary(VWPair<List<Lockable>, List<Lockable>> locationsToUnlock) {
		locationsToUnlock.getFirst().stream().filter((Lockable location) -> location != null).forEach(Lockable::releaseSharedReadLock);
		locationsToUnlock.getSecond().stream().filter((Lockable location) -> location != null).forEach(Lockable::releaseExclusiveWriteLock);
	}
	
	private VacuumWorldLocation getCurrentActorLocation(VacuumWorldSpace context, Actor actor) {
		if(isCurrentActorAgent(actor)) {
			return getAgentLocation(context, (VacuumWorldCleaningAgent) actor);
		}
		else if(isCurrentActorUser(actor)) {
			return getUserLocation(context, (User) actor);
		}
		else {
			return null;
		}
	}

	private VacuumWorldLocation getAgentLocation(VacuumWorldSpace context, VacuumWorldCleaningAgent agent) {
		VacuumWorldCoordinates coordinates = agent.getCurrentLocation();
		String agentId = agent.getId();
		
		if(!context.containsKey(coordinates)) {
			return null;
		}
		else {
			return getAgentLocation(context.getLocation(coordinates), agentId);
		}
	}

	private VacuumWorldLocation getAgentLocation(VacuumWorldLocation location, String agentId) {
		if(!location.isAnAgentPresent()) {
			return null;
		}
		
		return agentId.equals(location.getAgent().getId()) ? location : null;
	}

	private VacuumWorldLocation getUserLocation(VacuumWorldSpace context, User user) {
		VacuumWorldCoordinates coordinates = user.getCurrentLocation();
		String userId = user.getId();
		
		if(!context.containsKey(coordinates)) {
			return null;
		}
		else {
			return getUserLocation(context.getLocation(coordinates), userId);
		}
	}

	private VacuumWorldLocation getUserLocation(VacuumWorldLocation location, String userId) {
		if(!location.isAUserPresent()) {
			return null;
		}
		
		return userId.equals(location.getUser().getId()) ? location : null;
	}

	private boolean isCurrentActorAgent(Actor actor) {
		return VacuumWorldCleaningAgent.class.isAssignableFrom(actor.getClass());
	}
	
	private boolean isCurrentActorUser(Actor actor) {
		return User.class.isAssignableFrom(actor.getClass());
	}
	
	private Result doTurn(VacuumWorldLocation actorLocation, TurningAction action, TurningDirection turningDirection) {
		try {			
			actorLocation.getExclusiveWriteLock();
			performTurn(action, actorLocation, turningDirection);
			actorLocation.releaseExclusiveWriteLock();
			
			String actorId = action.getActor().getId().toString();
			logTurn(action, actorLocation, actorId);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		}
		catch (Exception e) {
			VWPair<List<Lockable>, List<Lockable>> locationsToUnlock = new VWPair<>(new ArrayList<>(), Arrays.asList(actorLocation));
			unlockLocationsIfNecessary(locationsToUnlock);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId().toString(), e, null);
		}
	}
	
	private void logTurn(TurningAction action, VacuumWorldLocation actorLocation, String actorId) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = null;
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		if(isCurrentActorAgent(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(action.getActorOldFacingDirection(), actorLocation.getAgent().getFacingDirection());
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + " old facing direction: " + action.getActorOldFacingDirection() + ", new facing direction: " + actorLocation.getAgent().getFacingDirection() + ".");
		}
		else if(isCurrentActorUser(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(action.getActorOldFacingDirection(), actorLocation.getAgent().getFacingDirection());
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + " old facing direction: " + action.getActorOldFacingDirection() + ", new facing direction: " + actorLocation.getUser().getFacingDirection() + ".");
		}
		
		createAndStoreActionReport(actorId, action.getClass(), ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter);
	}

	private void createAndStoreActionReport(String actorId, Class<? extends EnvironmentalAction> actionPrototype, ActionResult actionResult, Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter, Pair<VacuumWorldCoordinates> locationsBeforeAndAfter, Object... additional) {
		AbstractActionReport report = null;
		
		if(TurningAction.class.isAssignableFrom(actionPrototype)) {
			report = new TurnActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond(), TurningDirection.fromFacingDirections(facingDirectionsBeforeAndAfter));
		}
		else if(MoveAction.class.isAssignableFrom(actionPrototype)) {
			report = new MoveActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond());
		}
		else if(CleanAction.class.isAssignableFrom(actionPrototype)) {
			report = new CleanActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond(), (DirtType) additional[0]);
		}
		else if(PerceiveAction.class.isAssignableFrom(actionPrototype)) {
			report = new PerceiveActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond());
		}
		else if(DropDirtAction.class.isAssignableFrom(actionPrototype)) {
			report = new DropDirtActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond(), (DirtType) additional[0]);
		}
		else if(SpeechAction.class.isAssignableFrom(actionPrototype)) {
			report = new SpeechActionReport(actionPrototype, actionResult, facingDirectionsBeforeAndAfter.getFirst(), facingDirectionsBeforeAndAfter.getSecond(), locationsBeforeAndAfter.getFirst(), locationsBeforeAndAfter.getSecond());
		}
		
		storeActionReport(actorId, report);
	}

	private void storeActionReport(String actorId, AbstractActionReport report) {
		if(!this.attemptedActions.containsKey(actorId)) {
			this.attemptedActions.put(actorId, new ArrayList<>());
		}
		
		this.attemptedActions.get(actorId).add(report);
	}
	
	private void storeActionImpossibleOutcome(EnvironmentalAction action, Actor actor, VacuumWorldLocation actorLocation) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = null;
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		if(isCurrentActorAgent(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getAgent().getFacingDirection(), actorLocation.getAgent().getFacingDirection());
		}
		else if(isCurrentActorUser(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getUser().getFacingDirection(), actorLocation.getUser().getFacingDirection());
		}
		
		createAndStoreActionReport(actor.getId().toString(), action.getClass(), ActionResult.ACTION_IMPOSSIBLE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter, new Object[]{null});
	}

	private void storeActionFailedOutcome(EnvironmentalAction action, Actor actor, VacuumWorldLocation actorLocation) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = null;
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		if(isCurrentActorAgent(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getAgent().getFacingDirection(), actorLocation.getAgent().getFacingDirection());
		}
		else if(isCurrentActorUser(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getUser().getFacingDirection(), actorLocation.getUser().getFacingDirection());
		}
		
		createAndStoreActionReport(actor.getId().toString(), action.getClass(), ActionResult.ACTION_FAILED, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter, new Object[]{null});
	}

	private void performTurn(TurningAction action, VacuumWorldLocation actorLocation, TurningDirection turningDirection) {
		if(isCurrentActorAgent(action.getActor())) {
			action.setActorOldFacingDirection(actorLocation.getAgent().getFacingDirection());
			actorLocation.getAgent().turn(turningDirection);
		}
		else if(isCurrentActorUser(action.getActor())) {
			action.setActorOldFacingDirection(actorLocation.getUser().getFacingDirection());
			actorLocation.getUser().turn(turningDirection);
		}
	}
	
	private ActorFacingDirection getActorFacingDirection(VacuumWorldLocation currentActorLocation, Actor actor) {		
		if(isCurrentActorAgent(actor)) {
			return getAgentFacingDirection(currentActorLocation, (VacuumWorldCleaningAgent) actor);
		}
		else if(isCurrentActorUser(actor)) {
			return getUserFacingDirection(currentActorLocation, (User) actor);
		}
		else {
			return null;
		}
	}

	private ActorFacingDirection getAgentFacingDirection(VacuumWorldLocation currentActorLocation, VacuumWorldCleaningAgent agent) {
		if(currentActorLocation == null) {
			return null;
		}
		
		ActorFacingDirection facingDirection = agent.getFacingDirection();
		
		return facingDirection.equals(currentActorLocation.getAgent().getFacingDirection()) ? facingDirection : null;
	}

	private ActorFacingDirection getUserFacingDirection(VacuumWorldLocation currentActorLocation, User user) {
		if(currentActorLocation == null) {
			return null;
		}
		
		ActorFacingDirection facingDirection = user.getFacingDirection();
		
		return facingDirection.equals(currentActorLocation.getUser().getFacingDirection()) ? facingDirection : null;
	}
	
	private boolean checkForWall(VacuumWorldLocation actorLocation, ActorFacingDirection actorFacingDirection) {
		return actorLocation.getNeighborLocationType(actorFacingDirection) == VacuumWorldLocationType.WALL;
	}

	private boolean checkForObstacle(VacuumWorldLocation targetLocation) {
		if (targetLocation == null) {
			return true;
		}

		return checkForGenericObstacle(targetLocation) || checkForAgent(targetLocation) || checkForUser(targetLocation);
	}

	private boolean checkForUser(VacuumWorldLocation targetLocation) {
		return targetLocation.getUser() != null;
	}

	private boolean checkForAgent(VacuumWorldLocation targetLocation) {
		return targetLocation.getAgent() != null;
	}

	private boolean checkForGenericObstacle(VacuumWorldLocation targetLocation) {
		Obstacle potentialObstacle = targetLocation.getObstacle();

		if (potentialObstacle == null || potentialObstacle instanceof Dirt) {
			return false;
		}

		return true;
	}
	
	private boolean checkTurningActionSuccess(TurningAction action, VacuumWorldSpace context, TurningDirection turningDirection) {
		Actor actor = action.getActor();
		ActorFacingDirection candidateFromAction = action.getActorOldFacingDirection().getSideDirection(turningDirection);
		
		if(candidateFromAction == null) {
			return false;
		}
		else {
			return checkTurningActionSuccess(actor, context, candidateFromAction);
		}
	}

	private boolean checkTurningActionSuccess(Actor actor, VacuumWorldSpace context, ActorFacingDirection candidateFromAction) {
		if(isCurrentActorAgent(actor)) {
			ActorFacingDirection candidateFromContext = getAgentFacingDirection(getCurrentActorLocation(context, actor), (VacuumWorldCleaningAgent) actor);
			
			return candidateFromAction.equals(candidateFromContext);
		}
		else if(isCurrentActorUser(actor)) {
			ActorFacingDirection candidateFromContext = getUserFacingDirection(getCurrentActorLocation(context, actor), (User) actor);
			
			return candidateFromAction.equals(candidateFromContext);
		}
		else {
			return false;
		}
	}
	
	private Result doMove(MoveAction action, Actor actor, VacuumWorldLocation actorLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection actorFacingDirection) {
		try {
			actorLocation.getExclusiveWriteLock();
			targetLocation.getExclusiveWriteLock();

			action.setOldLocationCoordinates(originalCooridinates);

			return doMove(actor, actorLocation, targetLocation, originalCooridinates, actorFacingDirection);
		}
		catch (Exception e) {
			VWPair<List<Lockable>, List<Lockable>> locationsToUnlock = new VWPair<>(new ArrayList<>(), Arrays.asList(actorLocation, targetLocation));
			unlockLocationsIfNecessary(locationsToUnlock);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, actor.getId().toString(), e, null);
		}
	}

	private Result doMove(Actor actor, VacuumWorldLocation actorLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection actorFacingDirection) {
		if(isCurrentActorAgent(actor)) {
			moveAgent((VacuumWorldCleaningAgent) actor, actorLocation, targetLocation, originalCooridinates, actorFacingDirection);
		}
		else if(isCurrentActorUser(actor)) {
			moveUser((User) actor, actorLocation, targetLocation, originalCooridinates, actorFacingDirection);
		}
		
		return finalizeMove(actor, actorLocation, targetLocation, originalCooridinates, actorFacingDirection);
	}

	private Result finalizeMove(Actor actor, VacuumWorldLocation actorLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection actorFacingDirection) {
		VWPair<List<Lockable>, List<Lockable>> locationsToUnlock = new VWPair<>(new ArrayList<>(), Arrays.asList(actorLocation, targetLocation));
		unlockLocationsIfNecessary(locationsToUnlock);
		logMove(actor.getId().toString(), actor, actorLocation, originalCooridinates, actorFacingDirection);
		
		return new VacuumWorldActionResult(ActionResult.ACTION_DONE, actor.getId().toString(), new ArrayList<>(), null);
	}

	private void logMove(String actorId, Actor actor, VacuumWorldLocation actorLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection actorFacingDirection) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = new Pair<>(actorFacingDirection, actorFacingDirection);
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(originalCooridinates, actorLocation.getCoordinates());

		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actor.getId().toString() + " old position: " + originalCooridinates + ", new position: " + actorLocation.getCoordinates() + ", facing direction: " + actorFacingDirection + ".");
		createAndStoreActionReport(actorId, MoveAction.class, ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter);
	}

	private void moveUser(User user, VacuumWorldLocation userLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection userFacingDirection) {
		userLocation.removeUser();
		targetLocation.addUser(user);
		user.setCurrentLocation(originalCooridinates.getNewCoordinates(userFacingDirection));
	}

	private void moveAgent(VacuumWorldCleaningAgent agent, VacuumWorldLocation agentLocation, VacuumWorldLocation targetLocation, VacuumWorldCoordinates originalCooridinates, ActorFacingDirection agentFacingDirection) {
		agentLocation.removeAgent();
		targetLocation.addAgent(agent);
		agent.setCurrentLocation(originalCooridinates.getNewCoordinates(agentFacingDirection));
	}
	
	private boolean checkOldLocation(VacuumWorldLocation actorOldLocation, Actor actor) {
		if(isCurrentActorAgent(actor)) {
			return !actorOldLocation.isAnAgentPresent();
		}
		else if(isCurrentActorUser(actor)) {
			return !actorOldLocation.isAUserPresent();
		}
		else {
			return false;
		}
	}

	private boolean checkNewLocation(VacuumWorldLocation actorLocation, Actor actor) {
		if(isCurrentActorAgent(actor)) {
			return actorLocation.isAnAgentPresent() && actor.getId().toString().equals(actorLocation.getAgent().getId());
		}
		else if(isCurrentActorUser(actor)) {
			return actorLocation.isAUserPresent() && actor.getId().toString().equals(actorLocation.getUser().getId());
		}
		else {
			return false;
		}
	}
	
	private boolean checkCompatibility(VacuumWorldLocation agentLocation) {
		VacuumWorldCleaningAgent agent = agentLocation.getAgent();
		VacuumWorldAgentType agentType = agent.getExternalAppearance().getType();
		DirtType dirtType = agentLocation.getDirt().getExternalAppearance().getDirtType();
		
		return DirtType.agentAndDirtCompatible(dirtType, agentType);
	}

	private Result doClean(VacuumWorldLocation agentLocation, CleanAction action) {
		try {
			agentLocation.getExclusiveWriteLock();
			DirtType dirtType = agentLocation.getDirt().getExternalAppearance().getDirtType();
			agentLocation.removeDirt();
			agentLocation.releaseExclusiveWriteLock();
			
			logClean(agentLocation, action, dirtType);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, action.getActor().getId(), new ArrayList<>(), null);
		}
		catch (Exception e) {
			VWPair<List<Lockable>, List<Lockable>> locationsToUnlock = new VWPair<>(new ArrayList<>(), Arrays.asList(agentLocation));
			unlockLocationsIfNecessary(locationsToUnlock);
			
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, action.getActor().getId(), e, null);
		}
	}
	
	private void logClean(VacuumWorldLocation agentLocation, CleanAction action, DirtType dirtType) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = null;
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(agentLocation.getCoordinates(), agentLocation.getCoordinates());
		
		if(isCurrentActorAgent(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(agentLocation.getAgent().getFacingDirection(), agentLocation.getAgent().getFacingDirection());
		}
		else if(isCurrentActorUser(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(agentLocation.getUser().getFacingDirection(), agentLocation.getUser().getFacingDirection());
		}
		
		VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + action.getActor().getId() + " cleaned a " + dirtType.toString() + " piece of dirt on location " + agentLocation.getCoordinates().toString() + ".");
		createAndStoreActionReport(action.getActor().getId(), action.getClass(), ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter, dirtType);
	}
	
	private void logPerceive(PerceiveAction action, VacuumWorldLocation actorLocation, ActorFacingDirection facingDirection) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = new Pair<>(facingDirection, facingDirection);
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		createAndStoreActionReport(actorLocation.getUser().getId(), action.getClass(), ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter);
	}
	
	private Result doSpeech(SpeechAction action, VacuumWorldSpace context) {
		VacuumWorldSpeechActionResult result = createSpeechActionResult(action, context);
		String actorId = action.getSenderId();
		VacuumWorldActionResult actionResult = new VacuumWorldActionResult(ActionResult.ACTION_DONE, action.getActor().getId().toString(), new ArrayList<>(), null);
		String message = result.getPayload().getPayload();
		String logMessage = buildSpeechActionLogMessage(actorId, message, result.getRecipientsIds());
		
		logSpeech(action, actorId, getCurrentActorLocation(context, action.getActor()), logMessage);
		
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
		addBodyToRecipientsListIfNecessary(candidateId, result, recipientsIds);
	}

	private void addAgentIdToRecipientsList(VacuumWorldCleaningAgent agent, List<String> recipientsIds, VacuumWorldSpeechActionResult result) {
		String candidateId = agent.getId();
		addBodyToRecipientsListIfNecessary(candidateId, result, recipientsIds);
	}
	
	private void addBodyToRecipientsListIfNecessary(String candidateId, VacuumWorldSpeechActionResult result, List<String> recipientsIds) {
		if(!candidateId.equals(result.getSenderId())) {
			recipientsIds.add(candidateId);
		}
	}
	
	private void logSpeech(SpeechAction action, String actorId, VacuumWorldLocation actorLocation, String logMessage) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = null;
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		if(isCurrentActorAgent(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getAgent().getFacingDirection(), actorLocation.getAgent().getFacingDirection());
		}
		else if(isCurrentActorUser(action.getActor())) {
			facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getUser().getFacingDirection(), actorLocation.getUser().getFacingDirection());
		}
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), logMessage);
		createAndStoreActionReport(actorId, action.getClass(), ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter);
	}
	
	private void dropDirt(VacuumWorldLocation currentActorLocation, DropDirtAction action) {
		Double[] dimensions = new Double[] { (double) 1, (double) 1 };
		String name = "Dirt";
		currentActorLocation.setDirt(new Dirt(new DirtAppearance(name, dimensions, action.getDirtToDropType()), false, action.getDropCycle()));
	}
	
	private void logDropDirt(DropDirtAction action, VacuumWorldLocation actorLocation) {
		Pair<ActorFacingDirection> facingDirectionsBeforeAndAfter = new Pair<>(actorLocation.getUser().getFacingDirection(), actorLocation.getUser().getFacingDirection());
		Pair<VacuumWorldCoordinates> locationsBeforeAndAfter = new Pair<>(actorLocation.getCoordinates(), actorLocation.getCoordinates());
		
		VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + action.getActor().getId().toString() + " dropped a " + action.getDirtToDropType().toString() + " piece of dirt on location " + actorLocation.getCoordinates().toString() + ".");
		createAndStoreActionReport(actorLocation.getUser().getId(), action.getClass(), ActionResult.ACTION_DONE, facingDirectionsBeforeAndAfter, locationsBeforeAndAfter, action.getDirtToDropType());
	}
}