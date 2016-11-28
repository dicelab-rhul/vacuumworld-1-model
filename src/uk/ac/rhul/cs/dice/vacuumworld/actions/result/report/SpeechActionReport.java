package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class SpeechActionReport extends AbstractActionReport {
    private Map<String, String> speeches;

    public SpeechActionReport(Class<? extends EnvironmentalAction> action, ActionResult actionResult, ActorFacingDirection actorOldDirection, ActorFacingDirection actorNewDirection, VacuumWorldCoordinates actorOldCoordinates, VacuumWorldCoordinates actorNewCoordinates, Object speeches) {
	super(action, actionResult, actorOldDirection, actorNewDirection, actorOldCoordinates, actorNewCoordinates, null);

	this.speeches = parse(speeches);
    }

    private Map<String, String> parse(Object speeches) {
	if (speeches instanceof Map<?, ?>) {
	    return parseSpeeches((Map<?, ?>) speeches);
	}
	else {
	    return new HashMap<>();
	}
    }

    private Map<String, String> parseSpeeches(Map<?, ?> speeches) {
	Map<String, String> toReturn = new HashMap<>();
	speeches.entrySet().stream().filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof String).forEach(entry -> toReturn.put((String) entry.getKey(), (String) entry.getValue()));

	return toReturn;
    }

    public Map<String, String> getSpeeches() {
	return this.speeches;
    }

    public String getSpeechDirectedToSpecificActor(String recipientId) {
	return this.speeches.getOrDefault(recipientId, null);
    }

    @Override
    public AbstractActionReport duplicate() {
	Set<VacuumWorldCoordinates> perceptionKeysCopy = new HashSet<>();
	getPerceptionKeys().forEach(coordinates -> perceptionKeysCopy.add(coordinates.duplicate()));

	Map<String, String> speechesCopy = new HashMap<>();
	this.speeches.forEach(speechesCopy::put);
	
	
	SpeechActionReport toReturn = new SpeechActionReport(getAction(), getActionResult(), getActorOldDirection(), getActorNewDirection(), getActorOldCoordinates().duplicate(), getActorNewCoordinates().duplicate(), speechesCopy);
	toReturn.setPerceptionKeys(perceptionKeysCopy);

	return toReturn;
    }
}