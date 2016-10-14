package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {
	private List<String> agentsIAlreadyGreeted;
	private List<String> agentsWhoAlreadyGreetedMe;
	private List<String> agentsToGreetOnThisCycle;
	
	private static final String NAME = "RANDOM_SOCIAL";

	public VacuumWorldSmartRandomSocialMind() {
		this.agentsIAlreadyGreeted = new ArrayList<>();
		this.agentsWhoAlreadyGreetedMe = new ArrayList<>();
		this.agentsToGreetOnThisCycle = new ArrayList<>();
	}

	public static final String getName() {
		return NAME;
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		VacuumWorldPerception perception = getPerception();
		List<VacuumWorldSpeechActionResult> receivedCommunications = getReceivedCommunications();
		
		if(perception == null) {
			return buildPerceiveAction();
		}
		else {
			updateAvailableActions(perception);
			resetAgentsToGreetOnThisCycle();
			addAgentsIJustGreatedToListIfNecessary();
			
			return decideWithPerception(perception, receivedCommunications);
		}
	}

	private EnvironmentalAction decideWithPerception(VacuumWorldPerception perception, List<VacuumWorldSpeechActionResult> receivedCommunications) {		
		if(someoneJustGreetedMe(receivedCommunications)) {
			List<String> agentsWhoJustGreetedMe = receivedCommunications.stream().filter((VacuumWorldSpeechActionResult result) -> result.getPayload().isGreatingAction()).map((VacuumWorldSpeechActionResult result) -> result.getSenderId()).collect(Collectors.toList());
			agentsWhoJustGreetedMe.stream().filter((String id) -> !this.agentsWhoAlreadyGreetedMe.contains(id)).collect(Collectors.toList()).forEach(this.agentsWhoAlreadyGreetedMe::add);
			
			this.agentsToGreetOnThisCycle = agentsWhoJustGreetedMe.stream().filter((String id) -> !this.agentsIAlreadyGreeted.contains(id)).collect(Collectors.toList());
			
			if(Utils.isCollectionNotNullAndNotEmpty(this.agentsToGreetOnThisCycle)) {
				return greetBack();
			}
		}
		
		return decideWithPerception(perception);
	}

	private EnvironmentalAction decideWithPerception(VacuumWorldPerception perception) {
		List<String> agentsISee = perception.getAgentsInPerception(getBodyId()).stream().map((VacuumWorldCleaningAgent agent) -> agent.getId()).collect(Collectors.toList());
		
		if(Utils.isCollectionNotNullAndNotEmpty(agentsISee)) {
			this.agentsToGreetOnThisCycle = agentsISee.stream().filter((String id) -> !this.agentsIAlreadyGreeted.contains(id)).collect(Collectors.toList());
			
			return decideWithAgentsToGreetOnThisCycle();
		}
		else {
			return decideActionRandomly();
		}
	}

	private EnvironmentalAction decideWithAgentsToGreetOnThisCycle() {
		if(Utils.isCollectionNotNullAndNotEmpty(this.agentsToGreetOnThisCycle)) {
			return greetAgents();
		}
		else {
			return decideActionRandomly();
		}
	}

	private void addAgentsIJustGreatedToListIfNecessary() {
		if(didIGreetAnyoneInThePastCycle()) {			
			List<String> agentsIJustGreeted = getAgentsIJustGreeted();
			agentsIJustGreeted.stream().filter((String id) -> !this.agentsIAlreadyGreeted.contains(id)).collect(Collectors.toList()).forEach(this.agentsIAlreadyGreeted::add);
		}
	}

	private EnvironmentalAction greetAgents() {
		return buildSpeechAction(getBodyId(), this.agentsToGreetOnThisCycle, new VacuumWorldSpeechPayload("Hello, nice to meet you!!!", true));
	}

	private void resetAgentsToGreetOnThisCycle() {
		if(this.agentsToGreetOnThisCycle == null) {
			this.agentsToGreetOnThisCycle = new ArrayList<>();
		}
		
		this.agentsToGreetOnThisCycle.clear();
	}

	private EnvironmentalAction greetBack() {
		return buildSpeechAction(getBodyId(), this.agentsToGreetOnThisCycle, new VacuumWorldSpeechPayload("Nice to meet you too!!!", true));
	}

	private boolean someoneJustGreetedMe(List<VacuumWorldSpeechActionResult> receivedCommunications) {
		return receivedCommunications.stream().anyMatch((VacuumWorldSpeechActionResult result) -> result.getPayload().isGreatingAction());
	}

	private List<String> getAgentsIJustGreeted() {
		EnvironmentalAction lastAction = getNextAction();
		
		if(lastAction instanceof SpeechAction) {
			return ((SpeechAction) lastAction).getRecipientsIds();
		}
		else {
			return new ArrayList<>();
		}
	}

	private boolean didIGreetAnyoneInThePastCycle() {
		EnvironmentalAction lastAction = getNextAction();
		
		if(lastAction instanceof SpeechAction) {
			return ((VacuumWorldSpeechPayload) ((SpeechAction) lastAction).getPayload()).isGreatingAction();
		}
		
		return false;
	}
}