package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.StateRepresentationBuilder;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractActorMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringAgentMind extends VacuumWorldAbstractActorMind {
	private int cycleCounter;
	private boolean updateDatabase;
	
	private List<JsonObject> states;
	
	public VacuumWorldMonitoringAgentMind(String bodyId, JsonObject initialStateRepresentation) {
		super(bodyId);
		
		super.setPerceptionRange(Integer.MAX_VALUE);
		super.setCanSeeBehind(true);
		
		this.cycleCounter = 0;
		this.updateDatabase = false;
		this.states = new ArrayList<>();
		this.states.add(initialStateRepresentation);
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitoringAgentBrain.class);
		storeUpdateForDatabaseInMemory();
		loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
	}
	
	private void storeUpdateForDatabaseInMemory() {
		if(this.cycleCounter == 0) {
			return;
		}
		else {
			this.states.add(StateRepresentationBuilder.buildStateRepresentation(getPerception() == null ? new HashMap<>() : getPerception().getPerceivedMap()));
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		updateMonitoringVariables();
		
		return buildSystemMonitoringAction();
	}
	
	private EnvironmentalAction buildSystemMonitoringAction() {
		if(this.updateDatabase) {
			return buildNewAction(DatabaseUpdateStatesAction.class);
		}
		else {
			return buildNewAction(TotalPerceptionAction.class);
		}
	}

	@Override
	public VacuumWorldMonitoringActionResult getLastActionResult() {
		return (VacuumWorldMonitoringActionResult) super.getLastActionResult();
	}

	private void updateMonitoringVariables() {
		this.cycleCounter++;
		this.updateDatabase = this.cycleCounter % 5 == 0;
	}

	@Override
	public void execute(EnvironmentalAction action) {
		setLastActionResult(null);
		clearReceivedCommunications();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldMonitoringAgentBrain.class);
	}
	
	private EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if(TotalPerceptionAction.class.isAssignableFrom(actionPrototype)) {
			return buildPerceiveAction();
		}
		else if(DatabaseUpdateStatesAction.class.isAssignableFrom(actionPrototype)) {
			return new DatabaseUpdateStatesAction(VacuumWorldDatabaseInteractions.UPDATE_STATES, this.states);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	private EnvironmentalAction buildPerceiveAction() {
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