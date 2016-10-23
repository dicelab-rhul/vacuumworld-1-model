package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.DropDirtAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class UserMind extends VacuumWorldAbstractAgentMind {
	private DirtType lastDroppedDirt;
	private List<VacuumWorldSpeechActionResult> lastCycleIncomingSpeeches;
	private UserPlan plan;
	
	public UserMind(Random rng, String bodyId) {
		super(rng, bodyId);
		
		super.setPerceptionRange(Integer.MAX_VALUE);
		super.setCanSeeBehind(true);
		
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		this.lastDroppedDirt = DirtType.GREEN;
		this.plan = null;
	}
	
	public UserMind(String bodyId) {
		super(bodyId);
		
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		this.lastDroppedDirt = DirtType.GREEN;
		this.plan = null;
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, UserBrain.class);
		loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		if(this.plan != null) {
			if(!this.plan.getActionsToPerform().isEmpty()) {
				return followPlan(false);
			}
			else {
				VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": Plan is empty: I will follow it no more!");
				this.plan = null;
			}
		}
		
		VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": No plan found! I'm free to decide what to do!");
		
		return decideWithPerception();
	}
	
	private EnvironmentalAction decideWithPerception() {
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
		if(lastActionSucceeded() || specialFlag) {
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
		List<Result> messages = getReceivedCommunications();
		EnvironmentalAction toReturn;
		
		for(Result result : messages) {
			toReturn = getNextActionFromMessage(((VacuumWorldSpeechActionResult) result).getPayload().getPayload(), perception);
			
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
			this.plan = buildPlanMaybe(payload, perception);
			
			if(this.plan == null) {
				return null;
			}
			
			return followPlan(true);
		}
		else {
			return null;
		}
	}

	private UserPlan buildPlanMaybe(String payload, VacuumWorldPerception perception) {
		if(getRNG().nextBoolean()) {
			VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": I agree to build a plan...");
			
			return buildPlan(payload, perception);
		}
		else {
			VWUtils.logWithClass(getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": I don't want to build a plan...");
			
			return null;
		}
	}

	private UserPlan buildPlan(String payload, VacuumWorldPerception perception) {
		String temp = payload.replaceAll("move", "");
		
		return buildPlanHelper(ActorFacingDirection.fromCompactRepresentation(temp), perception.getActorCurrentFacingDirection());
	}

	private UserPlan buildPlanHelper(ActorFacingDirection target, ActorFacingDirection direction) {
		return new UserPlan(direction.getBestStrategyForMoving(target), getBodyId());
	}

	@Override
	public EnvironmentalAction decideActionRandomly() {
		Class<? extends EnvironmentalAction> actionPrototype = decideActionPrototypeRandomly();

		return buildNewAction(actionPrototype);
	}
	
	public EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction(getBodyId(), new ArrayList<>(), new VacuumWorldSpeechPayload("Hello everyone!!!", false));
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
			DirtType dirtType = DirtType.GREEN.equals(this.lastDroppedDirt) && lastActionSucceeded() ? DirtType.ORANGE : DirtType.GREEN;
			this.lastDroppedDirt = dirtType;
			
			return DropDirtAction.class.getConstructor(DirtType.class).newInstance(dirtType);
		}
		catch (Exception e) {
			VWUtils.log(e);
			
			return null;
		}
	}

	protected EnvironmentalAction buildPerceiveAction() {
		try {
			return PerceiveAction.class.getConstructor(Integer.class, Boolean.class).newInstance(Integer.MAX_VALUE, true);
		}
		catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			VWUtils.fakeLog(e);
			
			return new PerceiveAction();
		}
	}

	public EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
		try {
			return actionPrototype.newInstance();
		}
		catch (Exception e) {
			VWUtils.log(e);
			
			return null;
		}
	}
	
	public SpeechAction buildSpeechAction(String senderId, List<String> recipientIds, VacuumWorldSpeechPayload payload) {
		try {
			Constructor<SpeechAction> constructor = SpeechAction.class.getConstructor(String.class, List.class, Payload.class);
			return constructor.newInstance(senderId, new ArrayList<>(recipientIds), payload);
		}
		catch (Exception e) {
			VWUtils.log(e);
			
			return null;
		}
	}
	
	protected void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
	}
	
	protected void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getActorCoordinates();
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

		for (Class<? extends EnvironmentalAction> a : this.getAvailableActionsForThisCycle()) {
			if (a.isAssignableFrom(name)) {
				VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": removing " + name.getSimpleName() + " from my available actions for this cycle because it is clearly impossible...");
				toRemove.add(name);
			}
		}
		
		this.getAvailableActionsForThisCycle().removeAll(toRemove);
	}

	@Override
	public void execute(EnvironmentalAction action) {
		setLastActionResult(null);
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
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
				setLastActionResult((VacuumWorldActionResult) result);
			}
			else if(result instanceof VacuumWorldSpeechActionResult) {
				this.lastCycleIncomingSpeeches.add((VacuumWorldSpeechActionResult) result);
			}
		}
	}
	
	@Override
	public void setCanSeeBehind(boolean canSeeBehind) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPerceptionRange(int preceptionRange) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPerceptionRange() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean canSeeBehind() {
		return true;
	}
	
	@Override
	public VacuumWorldPerception getPerception() {
		return (VacuumWorldPerception) super.getPerception();
	}
}