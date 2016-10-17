package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class UserPlan {
	private Queue<Class<? extends EnvironmentalAction>> actionsToPerform;
	private Class<? extends EnvironmentalAction> lastAction;
	private int numberOfConsecutiveFailuresOfTheSameAction;
	
	public UserPlan() {
		this.actionsToPerform = new LinkedTransferQueue<>();
		this.numberOfConsecutiveFailuresOfTheSameAction = 0;
		this.lastAction = null;
	}
	
	public UserPlan(List<Class<? extends EnvironmentalAction>> actions, String actorId) {
		for(Class<? extends EnvironmentalAction> actionToPerform : actions) {
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.ACTOR + actorId + ": adding " + actionToPerform.getSimpleName() + " to plan...");
		}
		
		this.actionsToPerform = new LinkedTransferQueue<>(actions);
		this.numberOfConsecutiveFailuresOfTheSameAction = 0;
		this.lastAction = null;
	}

	public Queue<Class<? extends EnvironmentalAction>> getActionsToPerform() {
		return this.actionsToPerform;
	}
	
	public Class<? extends EnvironmentalAction> pullActionToPerform(String agentId) {
		Class<? extends EnvironmentalAction> selected = this.actionsToPerform.poll();
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.ACTOR + agentId + ": selecting " + selected.getSimpleName() + " from plan...");
		
		return selected;
	}

	public void setActionsToPerform(Queue<Class<? extends EnvironmentalAction>> actionsToPerform) {
		this.actionsToPerform = actionsToPerform;
	}
	
	public void clearActionsToPerform() {
		this.actionsToPerform.clear();
	}
	
	public void pushActionToPerform(Class<? extends EnvironmentalAction> actionToPerform, String agentId) {
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.ACTOR + agentId + ": adding " + actionToPerform.getSimpleName() + " to plan...");
		this.actionsToPerform.add(actionToPerform);
	}

	public Class<? extends EnvironmentalAction> getLastAction() {
		return this.lastAction;
	}

	public void setLastAction(Class<? extends EnvironmentalAction> lastAction) {
		this.lastAction = lastAction;
	}

	public int getNumberOfConsecutiveFailuresOfTheSameAction() {
		return this.numberOfConsecutiveFailuresOfTheSameAction;
	}

	public void setNumberOfConsecutiveFailuresOfTheSameAction(int numberOfConsecutiveFailuresOfTheSameAction) {
		this.numberOfConsecutiveFailuresOfTheSameAction = numberOfConsecutiveFailuresOfTheSameAction;
	}
	
	public void incrementNumberOfConsecutiveFailuresOfTheSameAction() {
		this.numberOfConsecutiveFailuresOfTheSameAction++;
	}
}