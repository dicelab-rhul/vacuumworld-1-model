package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.DropDirtAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class UserMind extends AbstractAgentMind {
	private DirtType lastDroppedDirt;
	private Random rng;
	private String bodyId;
	private VacuumWorldActionResult lastAttemptedActionResult;
	private List<VacuumWorldSpeechActionResult> lastCycleIncomingSpeeches;
	private Set<Class<? extends AbstractAction>> actions;
	private List<Class<? extends EnvironmentalAction>> availableActions;
	private EnvironmentalAction nextAction;
	private UserPlan plan;
	
	public UserMind() {
		this.rng = new Random();
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		this.lastDroppedDirt = DirtType.GREEN;
		this.plan = null;
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, UserBrain.class);
		setAvailableActions(new ArrayList<>(getVacuumWorldActions()));
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		if(this.plan != null) {
			return followPlan(false);
		}
		
		VacuumWorldPerception perception = getPerception();
		
		if(perception == null) {
			return buildPerceiveAction();
		}
		else {
			updateAvailableActions(perception);
			
			return decideWithPerceptionAndMessages(perception);
		}
	}
	
	private EnvironmentalAction followPlan(boolean specialFlag) {
		if(lastActionSucceded() || specialFlag) {
			this.plan.setNumberOfConsecutiveFailuresOfTheSameAction(0);
			this.plan.setLastAction(this.plan.pullActionToPerform(getBodyId()));
			
			
			return buildNewAction(this.plan.getLastAction());
		}
		else {
			this.plan.incrementNumberOfConsecutiveFailuresOfTheSameAction();
			
			return retryIfPossible();
		}
	}

	private EnvironmentalAction retryIfPossible() {
		if(this.plan.getNumberOfConsecutiveFailuresOfTheSameAction() <= 10) {
			return buildPhysicalAction(this.plan.getLastAction());
		}
		else {
			this.plan = null;
			
			return decideActionRandomly();
		}
	}

	private EnvironmentalAction decideWithPerceptionAndMessages(VacuumWorldPerception perception) {
		List<VacuumWorldSpeechActionResult> messages = getReceivedCommunications();
		
		EnvironmentalAction toReturn;
		
		for(VacuumWorldSpeechActionResult result : messages) {
			toReturn = getNextActionFromMessage(result.getPayload().getPayload(), perception);
			
			if(toReturn == null) {
				continue;
			}
			else {
				return toReturn;
			}
		}
		
		return decideActionRandomly();
	}

	private EnvironmentalAction getNextActionFromMessage(String payload, VacuumWorldPerception perception) {
		if(payload.matches("^move[NSWE]$")) {
			this.plan = buildPlan(payload, perception);
			
			if(this.plan == null) {
				return null;
			}
			
			return followPlan(true);
		}
		else {
			return null;
		}
	}

	private UserPlan buildPlan(String payload, VacuumWorldPerception perception) {
		String temp = payload.replaceAll("move", "");
		
		return buildPlanHelper(ActorFacingDirection.fromCompactRepresentation(temp), perception.getAgentCurrentFacingDirection());
	}

	private UserPlan buildPlanHelper(ActorFacingDirection target, ActorFacingDirection direction) {
		return new UserPlan(direction.getBestStrategyForMoving(target), getBodyId());
	}

	protected EnvironmentalAction decideActionRandomly() {		
		int size = this.getAvailableActions().size();
		int randomNumber = this.rng.nextInt(size);
		Class<? extends EnvironmentalAction> actionPrototype = this.getAvailableActions().get(randomNumber);

		return buildNewAction(actionPrototype);
	}
	
	protected EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction(this.bodyId, new ArrayList<>(), new VacuumWorldSpeechPayload("Hello everyone!!!", false));
		}
		else if(actionPrototype.equals(PerceiveAction.class)) {
			return buildPerceiveAction();
		}
		else if(actionPrototype.equals(DropDirtAction.class)) {
			return buildDropDirtAction();
		}
		else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	private EnvironmentalAction buildDropDirtAction() {
		try {
			DirtType dirtType = DirtType.GREEN.equals(this.lastDroppedDirt) && lastActionSucceded() ? DirtType.ORANGE : DirtType.GREEN;
			this.lastDroppedDirt = dirtType;
			
			return DropDirtAction.class.getConstructor(DirtType.class).newInstance(dirtType);
		}
		catch (Exception e) {
			Utils.log(e);
			
			return null;
		}
	}

	protected EnvironmentalAction buildPerceiveAction() {
		try {
			return PerceiveAction.class.getConstructor(Integer.class, Boolean.class).newInstance(Integer.MAX_VALUE, true);
		}
		catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			Utils.fakeLog(e);
			
			return new PerceiveAction();
		}
	}

	protected final EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
		try {
			return actionPrototype.newInstance();
		}
		catch (Exception e) {
			Utils.log(e);
			
			return null;
		}
	}
	
	protected SpeechAction buildSpeechAction(String senderId, List<String> recipientIds, VacuumWorldSpeechPayload payload) {
		try {
			Constructor<SpeechAction> constructor = SpeechAction.class.getConstructor(String.class, List.class, Payload.class);
			return constructor.newInstance(senderId, new ArrayList<>(recipientIds), payload);
		}
		catch (Exception e) {
			Utils.log(e);
			
			return null;
		}
	}
	
	protected void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
	}
	
	protected void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);
		VacuumWorldCleaningAgent agent = agentLocation.getAgent();

		if (agent != null) {
			ActorFacingDirection facingDirection = agent.getFacingDirection();

			if (agentLocation.getNeighborLocationType(facingDirection) == VacuumWorldLocationType.WALL) {
				removeActionIfNecessary(MoveAction.class);
			}
		}
	}
	
	protected void removeActionIfNecessary(Class<? extends EnvironmentalAction> name) {
		List<Class<? extends EnvironmentalAction>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
			if (a.isAssignableFrom(name)) {
				Utils.logWithClass(this.getClass().getSimpleName(), Utils.ACTOR + getBodyId() + ": removing " + name.getSimpleName() + " from my available actions for this cycle because it is clearly impossible...");
				toRemove.add(name);
			}
		}
		
		this.getAvailableActions().removeAll(toRemove);
	}

	@Override
	public void execute(EnvironmentalAction action) {
		this.lastAttemptedActionResult = null;
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), UserBrain.class);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof UserBrain && arg instanceof List<?>) {
			manageBrainRequest((List<?>) arg);
		}
	}
	
	private void manageBrainRequest(List<?> arg) {
		for (Object result : arg) {
			if (result instanceof VacuumWorldActionResult) {
				this.lastAttemptedActionResult = (VacuumWorldActionResult) result;
			}
			else if(result instanceof VacuumWorldSpeechActionResult) {
				this.lastCycleIncomingSpeeches.add((VacuumWorldSpeechActionResult) result);
			}
		}
	}

	public void setVacuumWorldActions(Set<Class<? extends AbstractAction>> actions) {
		List<Class<? extends AbstractAction>> temp = new ArrayList<>(actions);
		temp.remove(CleanAction.class);
		
		this.actions = new HashSet<>(temp);
	}
	
	public Set<Class<? extends AbstractAction>> getVacuumWorldActions() {
		return this.actions;
	}
	
	public EnvironmentalAction getNextAction() {
		return this.nextAction;
	}

	public void setNextActionForExecution(EnvironmentalAction nextAction) {
		this.nextAction = nextAction;
	}

	public VacuumWorldActionResult getLastActionResult() {
		return this.lastAttemptedActionResult;
	}
	
	public VacuumWorldPerception getPerception() {
		if(this.lastAttemptedActionResult == null) {
			return null;
		}
		else {
			return this.lastAttemptedActionResult.getPerception();
		}
	}
	
	public List<VacuumWorldSpeechActionResult> getReceivedCommunications() {
		return this.lastCycleIncomingSpeeches;
	}
	

	public List<Class<? extends EnvironmentalAction>> getAvailableActions() {
		return this.availableActions;
	}

	public void setAvailableActions(List<Class<? extends EnvironmentalAction>> availableActions) {
		this.availableActions = availableActions;
	}
	
	public boolean lastActionSucceded() {
		return ActionResult.ACTION_DONE.equals(this.lastAttemptedActionResult.getActionResult());
	}
	
	public boolean wasLastActionImpossible() {
		return ActionResult.ACTION_IMPOSSIBLE.equals(this.lastAttemptedActionResult.getActionResult());
	}
	
	public boolean lastActionFailed() {
		return ActionResult.ACTION_FAILED.equals(this.lastAttemptedActionResult.getActionResult());
	}
	
	public String getBodyId() {
		return this.bodyId;
	}
	
	public void setBodyId(String id) {
		this.bodyId = id;
	}
}