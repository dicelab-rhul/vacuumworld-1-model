package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class PureSocialMind extends VacuumWorldDefaultMind {

    public PureSocialMind(String bodyId) {
	super(bodyId);
    }

    @Override
    public EnvironmentalAction decide(Object... parameters) {
	if(getLastActionResult() != null) {
	    List<String> recipients = getPerception().getActorsIdsInPerception();
	    List<Result> communications = getReceivedCommunications();
	    communications.stream().map(result -> (VacuumWorldSpeechActionResult) result).map(result -> result.getPayload().getPayload()).forEach(m -> VWUtils.logWithClass(getClass().getSimpleName(),  m));
	
	    return buildSpeechAction(getBodyId(), recipients, new VacuumWorldSpeechPayload("Prova", false));
	}
	else {
	    return buildPerceiveAction();
	}
    }

}
