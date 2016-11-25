package uk.ac.rhul.cs.dice.vacuumworld.actions.result.report;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;

public interface ActionReportInterface<A extends Action, D extends Enum<?>, C extends LocationKey> {
    public abstract Class<? extends A> getAction();
    public abstract void setAction(Class<? extends A> action);
    public abstract ActionResult getActionResult();
    public abstract void setActionResult(ActionResult actionResult);
    public abstract D getActorOldDirection();
    public abstract void setActorOldDirection(D actorOldDirection);
    public abstract D getActorNewDirection();
    public abstract void setActorNewDirection(D actorNewDirection);
    public abstract C getActorOldCoordinates();
    public abstract void setActorOldCoordinates(C actorOldCoordinates);
    public abstract C getActorNewCoordinates();
    public abstract void setActorNewCoordinates(C actorNewCoordinates);
}