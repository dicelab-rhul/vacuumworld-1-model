package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

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
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {
	private Set<String> agentsIKnow = new HashSet<>();
	private AgentAwarenessRepresentation me;
	private static final String NAME = "RANDOM_SOCIAL";

	public VacuumWorldSmartRandomSocialMind(AgentAwarenessRepresentation me) {
		this.me = me;
	}

	public static final String getName() {
		return NAME;
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		super.decide(parameters);
		
		if (this.getLastCyclePerceptions().isEmpty()) {
			this.setNextAction(new PerceiveAction(this.getPerceptionRange(), this.isCanSeeBehind()));
		}
		else {
			this.setNextAction(decideFromPerceptions());
		}
		return this.getNextAction();
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
			Utils.logWithClass(this.getClass().getSimpleName(), "No result was given to " + this.me.getMyid() + ".");
			
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
		
		for (VacuumWorldLocation location : perception.getPerceivedMap().values()) {
			addAgentToList(location, newAgents);
		}
		
		return newAgents;
	}
	
	private void addAgentToList(VacuumWorldLocation location, List<String> newAgents) {
		if (location.isAnAgentPresent()) {
			String id = (String) location.getAgent().getId();
			if (!this.me.getMyid().equals(id) && !this.agentsIKnow.contains(id)) {
				newAgents.add(id);
			}
		}
	}
}