package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {
	private List<String> agentsIAlreadySaidHelloTo;
	private List<String> agentsToSayHelloTo;
	private static final String NAME = "RANDOM_SOCIAL";

	public VacuumWorldSmartRandomSocialMind() {
		this.agentsIAlreadySaidHelloTo = new ArrayList<>();
		this.agentsToSayHelloTo = new ArrayList<>();
	}

	public static final String getName() {
		return NAME;
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {		
		VacuumWorldPerception perception = getPerception();
		
		if(perception == null) {
			return decideActionRandomly();
		}
		else {
			updateAvailableActions(perception);
			
			return decideWithPerception(perception);
		}
	}
	
	private EnvironmentalAction decideWithPerception(VacuumWorldPerception perception) {
		updateAgentsToSayHelloTo(perception);
		
		if(this.agentsToSayHelloTo.isEmpty()) {
			return decideActionRandomly();
		}
		else {
			return buildSpeechAction(getBodyId(), this.agentsToSayHelloTo, new VacuumWorldSpeechPayload("Hello, nice to meet you!!!"));
		}
	}

	private void updateAgentsToSayHelloTo(VacuumWorldPerception perception) {
		if(lastActionSucceded()) {
			this.agentsIAlreadySaidHelloTo.addAll(this.agentsToSayHelloTo);
		}
		
		this.agentsToSayHelloTo.clear();
		
		addAgentsToListIfNecessary(perception);
	}

	private void addAgentsToListIfNecessary(VacuumWorldPerception perception) {
		for(VacuumWorldLocation location : perception.getPerceivedMap().values()) {
			if(location.isAnAgentPresent()) {
				addAgentToListIfNecessary(location.getAgent().getId().toString());
			}
		}
	}

	private void addAgentToListIfNecessary(String id) {
		if(!this.agentsIAlreadySaidHelloTo.contains(id)) {
			this.agentsToSayHelloTo.add(id);
		}
	}
}