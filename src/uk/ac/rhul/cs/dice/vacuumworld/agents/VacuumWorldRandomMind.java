package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
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

public class VacuumWorldRandomMind extends VacuumWorldDefaultMind {
	private Random rng;
	private static final String NAME = "RANDOM";

	public VacuumWorldRandomMind() {
		this.rng = new Random();
	}
	
	public static final String getName() {
		return NAME;
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		super.decide(parameters);
		
		return decideHelper();
	}

	private EnvironmentalAction decideHelper() {
		if (this.getPerceptions().isEmpty()) {
			setNextAction(new PerceiveAction(this.getPerceptionRange(), this.isCanSeeBehind()));
		}
		else {
			setNextAction(decideFromPerceptions());
		}
		
		return this.getNextAction();
	}

	private EnvironmentalAction decideFromPerceptions() {
		// this completely ignores the speeches

		for (DefaultActionResult result : this.getPerceptions()) {
			if (result instanceof VacuumWorldActionResult) {
				VacuumWorldPerception p = ((VacuumWorldActionResult) result).getPerception();

				if (p != null) {
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
		int size = this.getAvailableActions().size();
		int randomNumber = this.rng.nextInt(size);
		Class<? extends EnvironmentalAction> actionPrototype = this.getAvailableActions().get(randomNumber);

		return buildNewAction(actionPrototype);
	}

	private EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if (actionPrototype.equals(SpeechAction.class)) {
			return buildSpeechAction(null, new ArrayList<>(0), getPayload());
		}
		else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	private VacuumWorldSpeechPayload getPayload() {
		return new VacuumWorldSpeechPayload("Hello World");
	}

	private void updateAvailableActions(VacuumWorldPerception perception) {
		updateMoveActionIfNecessary(perception);
		updateCleaningActionIfNecessary(perception);
	}

	private void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
		VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
		VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

		if (agentLocation.isDirtPresent()) {
			addActionIfNecessary(CleanAction.class);
		}
		else {
			removeActionIfNecessary(CleanAction.class);
		}
	}

	private void addActionIfNecessary(Class<? extends EnvironmentalAction> name) {
		for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
			if (a.getClass().isAssignableFrom(name)) {
				return;
			}
		}
		
		this.getAvailableActions().add(name);
	}
	
	private void removeActionIfNecessary(Class<? extends EnvironmentalAction> name) {
		List<Class<? extends EnvironmentalAction>> toRemove = new ArrayList<>();

		for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
			if (a.getClass().isAssignableFrom(name)) {
				toRemove.add(name);
			}
		}
		
		this.getAvailableActions().removeAll(toRemove);
	}

	private void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
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
}