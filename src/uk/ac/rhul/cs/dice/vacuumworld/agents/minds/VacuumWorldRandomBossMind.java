package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.util.Arrays;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class VacuumWorldRandomBossMind extends VacuumWorldDefaultMind {
	
	public VacuumWorldRandomBossMind(String bodyId) {
		super(bodyId);
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		EnvironmentalAction next = decideActionRandomly();
		
		if(next instanceof MoveAction) {
			return changeIdeaIfNecessary(next);
		}
		else {
			return next;
		}
	}

	private EnvironmentalAction changeIdeaIfNecessary(EnvironmentalAction selectedAction) {
		VacuumWorldPerception perception = getPerception();
		
		ActorFacingDirection direction = perception.getActorCurrentFacingDirection();
		VacuumWorldCoordinates targetCoordinates = perception.getActorCoordinates().getNewCoordinates(direction);
		
		if(perception.getPerceivedMap().get(targetCoordinates).isAUserPresent()) {
			List<String> recipientsIds = Arrays.asList(perception.getPerceivedMap().get(targetCoordinates).getUser().getId());
			
			return new SpeechAction(getBodyId(), recipientsIds, new VacuumWorldSpeechPayload("move" + direction.compactRepresentation(), false));
		}
		
		return selectedAction;
	}
}