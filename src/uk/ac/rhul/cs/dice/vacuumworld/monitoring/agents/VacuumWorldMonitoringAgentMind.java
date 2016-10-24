package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractActorMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateAgentsHistoriesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringAgentMind extends VacuumWorldAbstractActorMind {
	private int monitoringCounter;
	private List<VacuumWorldMonitoringActionResult> pastPerceptions;
	private boolean monitor;
	
	public VacuumWorldMonitoringAgentMind(String bodyId) {
		super(bodyId);
		
		super.setPerceptionRange(Integer.MAX_VALUE);
		super.setCanSeeBehind(true);
		
		this.monitoringCounter = 0;
		this.pastPerceptions = new ArrayList<>();
		this.monitor = false;
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitoringAgentBrain.class);
		loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		updateMonitoringVariables();
		
		if(this.monitor) {
			EnvironmentalAction action = buildSystemMonitoringAction();
			this.pastPerceptions.clear();
			
			return action;
		}
		else {
			this.pastPerceptions.add(getLastActionResult());
			
			return buildPerceiveAction();
		}
	}
	
	@Override
	public VacuumWorldMonitoringActionResult getLastActionResult() {
		return (VacuumWorldMonitoringActionResult) super.getLastActionResult();
	}
	
	private EnvironmentalAction buildSystemMonitoringAction() {
		return new DatabaseUpdateAgentsHistoriesAction(VacuumWorldDatabaseInteractions.UPDATE_AGENTS_HISTORIES, this.pastPerceptions);
	}

	private void updateMonitoringVariables() {
		this.monitoringCounter++;
		
		if(this.monitoringCounter == 5) {
			this.monitor = true;
			this.monitoringCounter = 0;
		}
		else {
			this.monitor = false;
		}
	}

	@Override
	public void execute(EnvironmentalAction action) {
		setLastActionResult(null);
		clearReceivedCommunications();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldMonitoringAgentBrain.class);
	}
	
	public EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if(TotalPerceptionAction.class.isAssignableFrom(actionPrototype)) {
			return buildPerceiveAction();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	public EnvironmentalAction buildPerceiveAction() {
		try {
			return TotalPerceptionAction.class.newInstance();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return new TotalPerceptionAction();
		}
	}

	@Override
	public EnvironmentalAction decideActionRandomly() {
		return buildPerceiveAction();
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitoringAgentBrain && arg instanceof List<?>) {
			manageBrainRequest((List<?>) arg);
		}
	}
	
	private void manageBrainRequest(List<?> arg) {
		for (Object result : arg) {
			if (result instanceof VacuumWorldMonitoringActionResult) {
				setLastActionResult((VacuumWorldMonitoringActionResult) result);
			}
		}
	}
	
	@Override
	public void setCanSeeBehind(boolean canSeeBehind) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPerceptionRange(int preceptionRange) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public VacuumWorldMonitoringPerception getPerception() {
		return (VacuumWorldMonitoringPerception) super.getPerception();
	}
}