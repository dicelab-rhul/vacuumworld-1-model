package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractEvent;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Actor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;

public class MonitoringUpdateEvent extends AbstractEvent {
	private ActionResult result;

	public MonitoringUpdateEvent(EnvironmentalAction action, Long timestamp, Actor actor, ActionResult result) {
		super(action, timestamp, actor);
		this.result = result;
	}

	public ActionResult getResult() {
		return this.result;
	}

	@Override
	public String represent() {
		return this.getActor() + " : " + this.getAction().getClass().getSimpleName() + " : " + this.result.toString() + " : " + this.getTimestamp();
	}

	@Override
	public String toString() {
		return this.represent();
	}

	@Override
	public boolean isPossible(Physics physics, Space context) {
		return true;
	}

	@Override
	public boolean isNecessary(Physics physics, Space context) {
		return false;
	}

	@Override
	public Result perform(Physics physics, Space context) {
		return null;
	}

	@Override
	public boolean succeeded(Physics physics, Space context) {
		return true;
	}
}