package uk.ac.rhul.cs.dice.vacuumworld.agents.minds.manhattan;

import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class ManhattanPlan {
	private Queue<Class<? extends EnvironmentalAction>> actionsToPerform;
	private Class<? extends EnvironmentalAction> lastAction;
	private VacuumWorldLocation targetLocation;
	private DirtType targetDirtType;
	private VacuumWorldAgentType currentAgentType;
	private int numberOfConsecutiveFailuresOfTheSameAction;
	private PlanCodes planCodes;
	
	public ManhattanPlan() {
		this.actionsToPerform = new LinkedTransferQueue<>();
		this.numberOfConsecutiveFailuresOfTheSameAction = 0;
		this.lastAction = null;
		this.targetLocation = null;
		this.targetDirtType = null;
		this.currentAgentType = null;
		this.planCodes = PlanCodes.getInstance();
	}
	
	public PlanCodes getPlanCodes() {
		return this.planCodes;
	}

	public Queue<Class<? extends EnvironmentalAction>> getActionsToPerform() {
		return this.actionsToPerform;
	}
	
	public Class<? extends EnvironmentalAction> pullActionToPerform(String agentId) {
		Class<? extends EnvironmentalAction> selected = this.actionsToPerform.poll();
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + agentId + ": selecting " + selected.getSimpleName() + " from plan...");
		
		return selected;
	}

	public void setActionsToPerform(Queue<Class<? extends EnvironmentalAction>> actionsToPerform) {
		this.actionsToPerform = actionsToPerform;
	}
	
	public void clearActionsToPerform() {
		this.actionsToPerform.clear();
	}
	
	public void pushActionToPerform(Class<? extends EnvironmentalAction> actionToPerform, String agentId) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + agentId + ": adding " + actionToPerform.getSimpleName() + " to plan...");
		this.actionsToPerform.add(actionToPerform);
	}

	public Class<? extends EnvironmentalAction> getLastAction() {
		return this.lastAction;
	}

	public void setLastAction(Class<? extends EnvironmentalAction> lastAction) {
		this.lastAction = lastAction;
	}

	public VacuumWorldLocation getTargetLocation() {
		return this.targetLocation;
	}

	public void setTargetLocation(VacuumWorldLocation targetLocation) {
		this.targetLocation = targetLocation;
	}

	public DirtType getTargetDirtType() {
		return this.targetDirtType;
	}

	public void setTargetDirtType(DirtType targetDirtType) {
		this.targetDirtType = targetDirtType;
	}

	public VacuumWorldAgentType getCurrentAgentType() {
		return this.currentAgentType;
	}

	public void setCurrentAgentType(VacuumWorldAgentType currentAgentType) {
		this.currentAgentType = currentAgentType;
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