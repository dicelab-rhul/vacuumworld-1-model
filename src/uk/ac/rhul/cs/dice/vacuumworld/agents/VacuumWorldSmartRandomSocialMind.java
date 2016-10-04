package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.AgentAwarenessRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {

	private Set<String> agentsIKnow = new HashSet<>();
	private AgentAwarenessRepresentation me;

	public VacuumWorldSmartRandomSocialMind(AgentAwarenessRepresentation me) {
		this.me = me;
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		while (this.getLastCyclePerceptions().isEmpty()) {
			notifyObservers(null, VacuumWorldDefaultBrain.class);
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		this.setAvailableActions(new ArrayList<>());
		this.getAvailableActions().addAll(this.getActions());
		
		if (this.getLastCyclePerceptions().isEmpty()) {
			this.setNextAction(new PerceiveAction(this.getPerceptionRange(), this.isCanSeeBehind()));
		}
		else {
			this.setNextAction(decideFromPerceptions());
		}
		return this.getNextAction();
	}

	@Override
	public void execute(EnvironmentalAction action) {
		this.getLastCyclePerceptions().clear();
		notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
	}

	private EnvironmentalAction decideFromPerceptions() {
		VacuumWorldActionResult result = null;
		for (DefaultActionResult r : this.getLastCyclePerceptions()) {
			if (r instanceof VacuumWorldSpeechActionResult) {
				//TODO
			}
			else if (r instanceof VacuumWorldActionResult) {
				result = (VacuumWorldActionResult) r;
			}
		}
		if (result != null) {
			return decideFromSpacePerception(result.getPerception());
		}
		else {
			Logger.getGlobal().log(Level.SEVERE, "NO RESULT WAS GIVEN TO: " + this.me.getMyid());
			return new PerceiveAction(this.getPerceptionRange(), this.isCanSeeBehind());
		}
	}

	private EnvironmentalAction decideFromSpacePerception(VacuumWorldPerception perception) {
		// clean if we are on some dirt!
		EnvironmentalAction a = cleanAction(perception);
		if (a != null) {
			return a;
		}
		List<String> newAgents = updateAgentsMet(perception);
		// if there are new agents we want to greet them!
		if (!newAgents.isEmpty()) {
			this.agentsIKnow.addAll(newAgents);
			return this.buildSpeechAction(this.me.getMyid(), newAgents, new VacuumWorldSpeechPayload("Hello!"));
		}
		else {
			return randomAction();
		}
	}

	private EnvironmentalAction cleanAction(VacuumWorldPerception perception) {
		for(Entry<VacuumWorldCoordinates, VacuumWorldLocation> entry : perception.getPerceivedMap().entrySet()) {
			EnvironmentalAction toReturn = clean(entry);
			
			if(toReturn != null) {
				return toReturn;
			}
		}
		
		return null;
	}

	private EnvironmentalAction clean(Entry<VacuumWorldCoordinates, VacuumWorldLocation> entry) {
		if (entry.getValue().isAnAgentPresent()) {
			String id = (String) entry.getValue().getAgent().getId();
			if (this.me.getMyid().equals(id)) {
				return tryCleaning(entry);
			}
		}
		
		return null;
	}

	private EnvironmentalAction tryCleaning(Entry<VacuumWorldCoordinates, VacuumWorldLocation> entry) {
		// check if im on to of some dirt
		if (!entry.getValue().isDirtPresent()) {
			return null;
		}
		
		if (DirtType.agentAndDirtCompatible(((DirtAppearance) entry.getValue().getDirt().getExternalAppearance()).getDirtType(), this.me.getType())) {
			// clean the dirt!
			return new CleanAction();
		}
		
		return null;
	}

	private EnvironmentalAction randomAction() {
		Random rand = new Random();
		int r = rand.nextInt(5);
		
		if (r < 2) {
			return turnAction();
		}
		else if (r < 4) {
			return moveAction();
		}
		else {
			return broadcastAction();
		}

	}

	private EnvironmentalAction broadcastAction() {
		return new SpeechAction(this.me.getMyid(), null, new VacuumWorldSpeechPayload("Hello everyone!"));
	}

	private EnvironmentalAction moveAction() {
		return new MoveAction();
	}

	private EnvironmentalAction turnAction() {
		Random rand = new Random();
		
		if (rand.nextBoolean()) {
			return new TurnRightAction();
		}
		else {
			return new TurnLeftAction();
		}
	}

	private List<String> updateAgentsMet(VacuumWorldPerception perception) {
		List<String> newAgents = new ArrayList<>();
		
		for (Entry<VacuumWorldCoordinates, VacuumWorldLocation> entry : perception.getPerceivedMap().entrySet()) {
			addAgentToList(entry.getKey(), entry.getValue(), newAgents);
		}
		
		return newAgents;
	}
	
	public void addAgentToList(VacuumWorldCoordinates t, VacuumWorldLocation u, List<String> newAgents) {
		if (u.isAnAgentPresent()) {
			String id = (String) u.getAgent().getId();
			if (!this.me.getMyid().equals(id) && !this.agentsIKnow.contains(id)) {
				newAgents.add(id);
			}
		}
	}
}