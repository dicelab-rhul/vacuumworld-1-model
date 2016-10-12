package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public abstract class VacuumWorldDefaultMind extends AbstractAgentMind {
	private Random rng;
	
	private String bodyId;
	private int perceptionRange;
	private boolean canSeeBehind;
	private VacuumWorldActionResult lastAttemptedActionResult;
	private List<VacuumWorldSpeechActionResult> lastCycleIncomingSpeeches;

	private Set<Class<? extends AbstractAction>> actions;
	private List<Class<? extends EnvironmentalAction>> availableActions;
	private EnvironmentalAction nextAction;

	public VacuumWorldDefaultMind() {
		this.rng = new Random();
		this.lastCycleIncomingSpeeches = new ArrayList<>();
	}

	@Override
	public final void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldDefaultBrain && arg instanceof List<?>) {
			manageBrainRequest((List<?>) arg);
		}
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldDefaultBrain.class);
		setAvailableActions(new ArrayList<>(this.getVacuumWorldActions()));
	}
	
	@Override
	public void execute(EnvironmentalAction action) {
		this.lastAttemptedActionResult = null;
		this.lastCycleIncomingSpeeches = null;
		
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
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
	
	protected EnvironmentalAction decideActionRandomly() {		
		int size = this.getAvailableActions().size();
		int randomNumber = this.rng.nextInt(size);
		Class<? extends EnvironmentalAction> actionPrototype = this.getAvailableActions().get(randomNumber);

		return buildNewAction(actionPrototype);
	}

	//subclasses should override this, especially the part where a speech action is build.
	protected EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction(this.bodyId, new ArrayList<>(), new VacuumWorldSpeechPayload("Hello everyone!!!"));
		}
		else if(actionPrototype.equals(PerceiveAction.class)) {
			return buildPerceiveAction();
		}
		else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	protected EnvironmentalAction buildPerceiveAction() {
		try {
			return PerceiveAction.class.getConstructor(Integer.class, Boolean.class).newInstance(this.perceptionRange, this.canSeeBehind);
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
			return constructor.newInstance(senderId, recipientIds, payload);
		}
		catch (Exception e) {
			Utils.log(e);
			
			return null;
		}
	}
	
	protected void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
		updateCleaningActionIfNecessary(perception);
	}

	protected void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

		if (agentLocation.isDirtPresent()) {
			addActionIfNecessary(CleanAction.class);
		}
		else {
			removeActionIfNecessary(CleanAction.class);
		}
	}

	protected void addActionIfNecessary(Class<? extends EnvironmentalAction> name) {
		for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
			if (a.getClass().isAssignableFrom(name)) {
				return;
			}
		}
		
		this.getAvailableActions().add(name);
	}
	
	protected void removeActionIfNecessary(Class<? extends EnvironmentalAction> name) {
		List<Class<? extends EnvironmentalAction>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
			if (a.getClass().isAssignableFrom(name)) {
				toRemove.add(name);
			}
		}
		
		this.getAvailableActions().removeAll(toRemove);
	}

	protected void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);
		VacuumWorldCleaningAgent agent = agentLocation.getAgent();

		if (agent != null) {
			AgentFacingDirection facingDirection = agent.getFacingDirection();

			if (agentLocation.getNeighborLocation(facingDirection) == VacuumWorldLocationType.WALL) {
				removeActionIfNecessary(MoveAction.class);
			}
			else {
				addActionIfNecessary(MoveAction.class);
			}
		}
	}

	public void setVacuumWorldActions(Set<Class<? extends AbstractAction>> actions) {
		this.actions = actions;
	}
	
	public Set<Class<? extends AbstractAction>> getVacuumWorldActions() {
		return this.actions;
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
	
	public void setCanSeeBehind(boolean canSeeBehind) {
		this.canSeeBehind = canSeeBehind;
	}

	public void setPerceptionRange(int preceptionRange) {
		this.perceptionRange = preceptionRange;
	}

	public List<Class<? extends EnvironmentalAction>> getAvailableActions() {
		return this.availableActions;
	}

	public void setAvailableActions(List<Class<? extends EnvironmentalAction>> availableActions) {
		this.availableActions = availableActions;
	}

	public int getPerceptionRange() {
		return this.perceptionRange;
	}

	public boolean isCanSeeBehind() {
		return this.canSeeBehind;
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
	
	public List<VacuumWorldSpeechActionResult> getReceivedSpeeches() {
		return this.lastCycleIncomingSpeeches;
	}
}