package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;

public abstract class TurningAction extends PhysicalAction {
    private ActorFacingDirection actorOldFacingDirection;

    public ActorFacingDirection getActorOldFacingDirection() {
	return this.actorOldFacingDirection;
    }

    public void setActorOldFacingDirection(ActorFacingDirection actorOldFacingDirection) {
	this.actorOldFacingDirection = ActorFacingDirection.fromString(actorOldFacingDirection.toString().toLowerCase());
    }
}