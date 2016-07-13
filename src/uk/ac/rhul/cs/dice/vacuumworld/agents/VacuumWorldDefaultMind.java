package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;

public class VacuumWorldDefaultMind extends AbstractAgentMind {
	private int perceptionRange;
	private boolean canSeeBehind;

	private Set<Class<? extends AbstractAction>> actions;
	private List<Class<? extends EnvironmentalAction>> availableActions;

	private Random rng;
	private List<DefaultActionResult> lastCyclePerceptions;
	private EnvironmentalAction nextAction;

	public VacuumWorldDefaultMind() {
		this.lastCyclePerceptions = new ArrayList<>();
		this.rng = new Random();
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldDefaultBrain && arg instanceof List<?>) {
			for(Object result : (List<?>) arg) {
				if(result instanceof DefaultActionResult) {
					this.lastCyclePerceptions.add((DefaultActionResult) result);
				}
			}
		}
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		while(this.lastCyclePerceptions.isEmpty()) {
			notifyObservers(null, VacuumWorldDefaultBrain.class);
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		availableActions = new ArrayList<>();
		availableActions.addAll(actions);
		
		if (this.lastCyclePerceptions.isEmpty()) {
			nextAction = new PerceiveAction(this.perceptionRange, this.canSeeBehind);
		}
		else {
			nextAction = decideFromPerceptions();
		}
		
		this.lastCyclePerceptions.clear();
		
		return nextAction;
	}

	@Override
	public void execute(EnvironmentalAction action) {
		notifyObservers(nextAction, VacuumWorldDefaultBrain.class);
	}

	private EnvironmentalAction decideFromPerceptions() {
		//this completely ignores the speeches
		
		for(DefaultActionResult result : this.lastCyclePerceptions) {
			if(result instanceof VacuumWorldActionResult) {
				VacuumWorldPerception p = ((VacuumWorldActionResult) result).getPerception();
				
				if(p != null) {
					return decideAction(p);
				}
			}
		}
		
		return decideAction();
	}

	private EnvironmentalAction decideAction(VacuumWorldPerception perception) {
		if (perception != null) {
			updateAvailableActions(perception);
		}
		return decideAction();
	}

	private EnvironmentalAction decideAction() {
		int size = this.availableActions.size();
		int randomNumber = rng.nextInt(size);
		Class<? extends EnvironmentalAction> actionPrototype = availableActions.get(randomNumber);

		return buildNewAction(actionPrototype);
	}

	private EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction();
		} else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	private EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
		try {
			return actionPrototype.newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	private SpeechAction buildSpeechAction() {
		String senderId = null;
		List<String> recipientsIds = getRecipientsIds();
		VacuumWorldSpeechPayload payload = getPayload();

		try {
			Constructor<SpeechAction> constructor = SpeechAction.class.getConstructor(String.class, List.class,
					Payload.class);
			return constructor.newInstance(senderId, recipientsIds, payload);
		} catch (Exception e) {
			return null;
		}
	}

	private VacuumWorldSpeechPayload getPayload() {
		// TODO implement this
		return null;
	}

	private List<String> getRecipientsIds() {
		// TODO implement this
		return null;
	}

	private void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
		updateCleaningActionIfNecessary(perception);
	}

	private void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

		if (agentLocation.isDirtPresent()) {
			addCleanIfNecessary();
		} else {
			removeCleanIfNecessary();
		}
	}

	private void removeCleanIfNecessary() {
		List<Class<CleanAction>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction> a : this.availableActions) {
			if (a.getClass().isAssignableFrom(CleanAction.class)) {
				toRemove.add(CleanAction.class);
			}
		}
		this.availableActions.removeAll(toRemove);
	}

	private void addCleanIfNecessary() {
		for (Class<? extends EnvironmentalAction> a : this.availableActions) {
			if (a.getClass().isAssignableFrom(CleanAction.class)) {
				return;
			}
		}
		this.availableActions.add(CleanAction.class);
	}

	private void addMoveIfNecessary() {
		for (Class<? extends EnvironmentalAction> a : this.availableActions) {
			if (a.getClass().isAssignableFrom(MoveAction.class)) {
				return;
			}
		}
		this.availableActions.add(MoveAction.class);
	}

	private void removeMoveIfNecessary() {
		List<Class<MoveAction>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction> a : this.availableActions) {
			if (a.getClass().isAssignableFrom(MoveAction.class)) {
				toRemove.add(MoveAction.class);
			}
		}
		this.availableActions.removeAll(toRemove);
	}

	private void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);
		VacuumWorldCleaningAgent agent = agentLocation.getAgent();

		if (agent != null) {
			AgentFacingDirection facingDirection = agent.getFacingDirection();

			if (agentLocation.getNeighborLocation(facingDirection) == VacuumWorldLocationType.WALL) {
				removeMoveIfNecessary();
			} else {
				addMoveIfNecessary();
			}
		}
	}

	public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
		this.actions = actions;
	}

	public void setCanSeeBehind(boolean canSeeBehind) {
		this.canSeeBehind = canSeeBehind;
	}

	public void setPerceptionRange(int preceptionRange) {
		this.perceptionRange = preceptionRange;
	}
}