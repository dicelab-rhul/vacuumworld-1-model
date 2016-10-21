package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class UserPlan {
	private Queue<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> actionsToPerform;
	private Class<? extends EnvironmentalAction<VacuumWorldPerception>> lastAction;
	private int numberOfConsecutiveFailuresOfTheSameAction;
	
	public UserPlan() {
		this.actionsToPerform = new LinkedTransferQueue<>();
		this.numberOfConsecutiveFailuresOfTheSameAction = 0;
		this.lastAction = null;
	}
	
	public UserPlan(List<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> actions, String actorId) {
		for(Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionToPerform : actions) {
			VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + actorId + ": adding " + actionToPerform.getSimpleName() + " to plan...");
		}
		
		this.actionsToPerform = new LinkedTransferQueue<>(actions);
		this.numberOfConsecutiveFailuresOfTheSameAction = 0;
		this.lastAction = null;
	}

	public Queue<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> getActionsToPerform() {
		return this.actionsToPerform;
	}
	
	public Class<? extends EnvironmentalAction<VacuumWorldPerception>> pullActionToPerform(String agentId) {
		Class<? extends EnvironmentalAction<VacuumWorldPerception>> selected = this.actionsToPerform.poll();
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + agentId + ": selecting " + selected.getSimpleName() + " from plan...");
		
		return selected;
	}

	public void setActionsToPerform(Queue<Class<? extends EnvironmentalAction<VacuumWorldPerception>>> actionsToPerform) {
		this.actionsToPerform = actionsToPerform;
	}
	
	public void clearActionsToPerform() {
		this.actionsToPerform.clear();
	}
	
	public void pushActionToPerform(Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionToPerform, String agentId) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + agentId + ": adding " + actionToPerform.getSimpleName() + " to plan...");
		this.actionsToPerform.add(actionToPerform);
	}

	public Class<? extends EnvironmentalAction<VacuumWorldPerception>> getLastAction() {
		return this.lastAction;
	}

	public void setLastAction(Class<? extends EnvironmentalAction<VacuumWorldPerception>> lastAction) {
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