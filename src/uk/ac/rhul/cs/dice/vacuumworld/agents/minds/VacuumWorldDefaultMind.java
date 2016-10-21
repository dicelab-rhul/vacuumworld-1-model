package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public abstract class VacuumWorldDefaultMind extends VacuumWorldAbstractAgentMind<VacuumWorldPerception> {
	
	public VacuumWorldDefaultMind(String bodyId) {
		super(new Random(System.currentTimeMillis()), bodyId);
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
		loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
	}
	
	@Override
	public void execute(EnvironmentalAction<VacuumWorldPerception> action) {
		setLastActionResult(null);
		clearReceivedCommunications();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
	}
	
	private void manageBrainRequest(List<?> arg) {
		for (Object result : arg) {
			if (result instanceof VacuumWorldActionResult) {
				setLastActionResult((VacuumWorldActionResult) result);
			}
			else if(result instanceof VacuumWorldSpeechActionResult) {
				addReceivedCommunicationToList((VacuumWorldSpeechActionResult) result);
			}
		}
	}
	
	@Override
	public EnvironmentalAction<VacuumWorldPerception> decideActionRandomly() {
		Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionPrototype = super.decideActionPrototypeRandomly();

		return buildNewAction(actionPrototype);
	}

	public EnvironmentalAction<VacuumWorldPerception> buildNewAction(Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction(getBodyId(), new ArrayList<>(), new VacuumWorldSpeechPayload("Hello everyone!!!", false));
		}
		else if(actionPrototype.equals(PerceiveAction.class)) {
			return buildPerceiveAction();
		}
		else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	public EnvironmentalAction<VacuumWorldPerception> buildPerceiveAction() {
		try {
			return PerceiveAction.class.getConstructor(Integer.class, Boolean.class).newInstance(getPerceptionRange(), canSeeBehind());
		}
		catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			VWUtils.fakeLog(e);
			
			return new PerceiveAction();
		}
	}

	public EnvironmentalAction<VacuumWorldPerception> buildPhysicalAction(Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionPrototype) {
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
			return constructor.newInstance(senderId, recipientIds == null ? new ArrayList<>() : new ArrayList<>(recipientIds), payload);
		}
		catch (Exception e) {
			VWUtils.log(e);
			
			return null;
		}
	}
	
	public void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
		updateCleaningActionIfNecessary(perception);
	}

	private void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getActorCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

		if (!agentLocation.isDirtPresent()) {
			removeActionIfNecessary(CleanAction.class);
		}
	}
	
	private void removeActionIfNecessary(Class<? extends EnvironmentalAction<VacuumWorldPerception>> name) {
		List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction<VacuumWorldPerception>> a : this.getAvailableActionsForThisCycle()) {
			if (a.isAssignableFrom(name)) {
				VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": removing " + name.getSimpleName() + " from my available actions for this cycle because it is clearly impossible...");
				toRemove.add(name);
			}
		}
		
		this.getAvailableActionsForThisCycle().removeAll(toRemove);
	}

	private void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
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
}