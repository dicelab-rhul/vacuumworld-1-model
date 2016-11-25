package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VWPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.utils.functions.SpeechResultToSenderId;
import uk.ac.rhul.cs.dice.vacuumworld.utils.predicates.Contained;
import uk.ac.rhul.cs.dice.vacuumworld.utils.predicates.IsGreeting;
import uk.ac.rhul.cs.dice.vacuumworld.utils.predicates.IsGreetingAction;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {
    private List<String> agentsIAlreadyGreeted; // agents IDs of the agents this
						// agent already greeted in the
						// past
    private List<String> agentsWhoAlreadyGreetedMe; // agents IDs of the agents
						    // who already greeted this
						    // agent in the past
    private List<String> agentsToGreetOnThisCycle; // temporary list of agents
						   // this agent is going to
						   // greet on this cycle / has
						   // greeted in the past cycle.

    public VacuumWorldSmartRandomSocialMind(String bodyId) {
	super(bodyId);

	this.agentsIAlreadyGreeted = new ArrayList<>();
	this.agentsWhoAlreadyGreetedMe = new ArrayList<>();
	this.agentsToGreetOnThisCycle = new ArrayList<>();
    }

    /**
     * General idea of the decision process (the [END] label means that the
     * action is selected and the decision process ends):
     * 
     * get the perception get the received communications if no perception -->
     * return a PerceiveAction [END] filter out the clearly impossible actions
     * (e.g., CleanAction if no Dirt). if someone greeted this agent in the past
     * cycle: if at least one of them was not already greeted by this agent -->
     * greet back all the ones that were not already greeted by this agent [END]
     * if this agent can spots other agents within its perception: if at least
     * one of them was not already greeted by this agent --> greet all the ones
     * that were not already greeted by this agent [END] select a random action
     * [END]
     */
    @Override
    public EnvironmentalAction decide(Object... parameters) {
	VWPerception perception = getPerception();
	List<Result> receivedCommunications = getReceivedCommunications();

	if (perception == null) {
	    return buildPerceiveAction();
	}
	else {
	    updateAvailableActions(perception);
	    resetAgentsToGreetOnThisCycle();
	    addAgentsIJustGreatedToListIfNecessary();

	    return decideWithPerception(perception, receivedCommunications);
	}
    }

    private EnvironmentalAction decideWithPerception(VWPerception perception, List<Result> receivedCommunications) {
	if (someoneJustGreetedMe(receivedCommunications)) {

	    /*
	     * From receivedCommunications I create a Stream, then I filter out
	     * the results which do not respect the IsGreeting() Predicate, then
	     * I map on the leftovers a Function which gets the sender IDs from
	     * the results and finally I pack the IDs into a List.
	     *
	     * The IsGreeting() Predicate returns true if and only if the Result
	     * in input has a Payload and that Payload has the isGreeting
	     * attribute set to true.
	     * 
	     * The SpeechResultToSenderId() Function returns the sender ID of
	     * the Payload wrapped by a SpeechResult.
	     * 
	     * The collect(...) method transforms a Stream intto a Collection
	     * thanks to its Collector parameter.
	     */
	    List<String> agentsWhoJustGreetedMe = receivedCommunications.stream().filter(new IsGreeting()).map(new SpeechResultToSenderId()).collect(Collectors.toList());

	    /*
	     * From agentsWhoJustGreetedMe I create a Stream, then I filter out
	     * the results which respect the Contained(...) Predicate w.r.t.
	     * this.agentsWhoAlreadyGreetedMe, then I pack the leftovers into a
	     * List and finally I add each member of that List to
	     * this.agentsWhoAlreadyGreetedMe.
	     * 
	     * The Contained(...) Predicate requires a List<String> and checks
	     * whether the String in input is contained into that list. The
	     * negate() method represents the logical negation of the predicate.
	     * 
	     * The collect(...) method transforms a Stream into a Collection
	     * thanks to its Collector parameter.
	     * 
	     * The forEach(...) method accepts a named function and applies that
	     * to every elements of the List its applied to.
	     */
	    agentsWhoJustGreetedMe.stream().filter(new Contained(this.agentsWhoAlreadyGreetedMe).negate()).collect(Collectors.toList()).forEach(this.agentsWhoAlreadyGreetedMe::add);

	    /*
	     * From agentsWhoJustGreetedMe I create a Stream, then I filter out
	     * the results which respect the Contained(...) Predicate w.r.t.
	     * this.agentsWhoAlreadyGreetedMe, then I pack the leftovers into a
	     * List and finally I assign that List to
	     * this.agentsToGreetOnThisCycle.
	     * 
	     * The Contained(...) Predicate requires a List<String> and checks
	     * whether the String in input is contained into that list. The
	     * negate() method represents the logical negation of the predicate.
	     * 
	     * The collect(...) method transforms a Stream into a Collection
	     * thanks to its Collector parameter.
	     */
	    this.agentsToGreetOnThisCycle = agentsWhoJustGreetedMe.stream().filter(new Contained(this.agentsIAlreadyGreeted).negate()).collect(Collectors.toList());

	    if (VWUtils.isCollectionNotNullAndNotEmpty(this.agentsToGreetOnThisCycle)) {
		return greetBack();
	    }
	}

	return decideWithPerception(perception);
    }

    private EnvironmentalAction decideWithPerception(VWPerception perception) {
	/*
	 * From perception I get all the agents within it whose ID is different
	 * from this agent's ID, then I create a Stream from those, then I map
	 * on the Stream elements a Function which gets the IDs from the agents,
	 * then I pack the IDs into a List and finally I add each member of that
	 * List to agentsISee.
	 * 
	 * The AgentToId() Function returns the Agent ID of an Agent.
	 * 
	 * The collect(...) method transforms a Stream into a Collection thanks
	 * to its Collector parameter.
	 */
	List<String> agentsISee = perception.getActorsIdsInPerception();

	if (VWUtils.isCollectionNotNullAndNotEmpty(agentsISee)) {

	    /*
	     * From agentsISee I create a Stream, then I filter out the IDs
	     * which respect the Contained(...) Predicate w.r.t.
	     * this.agentsIAlreadyGreeted, then I pack the leftovers into a List
	     * and finally I add each member of that List to
	     * this.agentsToGreetOnThisCycle.
	     * 
	     * The Contained(...) Predicate requires a List<String> and checks
	     * whether the String in input is contained into that list. The
	     * negate() method represents the logical negation of the predicate.
	     * 
	     * The collect(...) method transforms a Stream into a Collection
	     * thanks to its Collector parameter.
	     */
	    this.agentsToGreetOnThisCycle = agentsISee.stream() .filter(new Contained(this.agentsIAlreadyGreeted).negate()).collect(Collectors.toList());

	    return decideWithAgentsToGreetOnThisCycle();
	}
	else {
	    return decideActionRandomly();
	}
    }

    private EnvironmentalAction decideWithAgentsToGreetOnThisCycle() {
	if (VWUtils.isCollectionNotNullAndNotEmpty(this.agentsToGreetOnThisCycle)) {
	    return greetAgents();
	}
	else {
	    return decideActionRandomly();
	}
    }

    private void addAgentsIJustGreatedToListIfNecessary() {
	if (didIGreetAnyoneInThePastCycle()) {
	    List<String> agentsIJustGreeted = getAgentsIJustGreeted();

	    /*
	     * From agentsIJustGreeted I create a Stream, then I filter out the
	     * IDs which respect the Contained(...) Predicate w.r.t.
	     * this.agentsIAlreadyGreeted, then I pack the leftovers into a List
	     * and finally I add each member of that List to
	     * this.agentsIAlreadyGreeted.
	     * 
	     * The Contained(...) Predicate requires a List<String> and checks
	     * whether the String in input is contained into that list. The
	     * negate() method represents the logical negation of the predicate.
	     * 
	     * The collect(...) method transforms a Stream into a Collection
	     * thanks to its Collector parameter.
	     * 
	     * The forEach(...) method accepts a named function and applies that
	     * to every elements of the List its applied to.
	     */
	    agentsIJustGreeted.stream().filter(new Contained(this.agentsIAlreadyGreeted).negate()).collect(Collectors.toList()).forEach(this.agentsIAlreadyGreeted::add);
	}
    }

    private EnvironmentalAction greetAgents() {
	return buildSpeechAction(getBodyId(), this.agentsToGreetOnThisCycle, new VacuumWorldSpeechPayload("Hello, nice to meet you!!!", true));
    }

    private void resetAgentsToGreetOnThisCycle() {
	if (this.agentsToGreetOnThisCycle == null) {
	    this.agentsToGreetOnThisCycle = new ArrayList<>();
	}

	this.agentsToGreetOnThisCycle.clear();
    }

    private EnvironmentalAction greetBack() {
	return buildSpeechAction(getBodyId(), this.agentsToGreetOnThisCycle, new VacuumWorldSpeechPayload("Nice to meet you too!!!", true));
    }

    private boolean someoneJustGreetedMe(List<Result> receivedCommunications) {

	/*
	 * From receivedCommunications I create a Stream, then I return true if
	 * and only if at least one element respects the IsGreeting() Predicate.
	 * 
	 * The IsGreeting() Predicate returns true if and only if the Result in
	 * input has a Payload and that Payload has the isGreeting attribute set
	 * to true.
	 */
	return receivedCommunications.stream().anyMatch(new IsGreeting());
    }

    private List<String> getAgentsIJustGreeted() {
	EnvironmentalAction lastAction = getNextAction();

	if (lastAction instanceof SpeechAction) {
	    return ((SpeechAction) lastAction).getRecipientsIds();
	}
	else {
	    return new ArrayList<>();
	}
    }

    private boolean didIGreetAnyoneInThePastCycle() {
	EnvironmentalAction lastAction = getNextAction();

	if (lastAction instanceof SpeechAction) {

	    /*
	     * I return true if and only if last action respects the
	     * IsGreatingAction() Predicate.
	     * 
	     * The IsGreatingAction Predicate returns true if and only if the
	     * Action in input has a Payload and that Payload has the isGreeting
	     * attribute set to true.
	     */
	    return new IsGreetingAction().test((SpeechAction) lastAction);
	}

	return false;
    }
}