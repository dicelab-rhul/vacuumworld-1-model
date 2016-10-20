package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringAgentMind extends VacuumWorldDefaultMind {
	public VacuumWorldMonitoringAgentMind() {
		super.setPerceptionRange(Integer.MAX_VALUE);
		super.setCanSeeBehind(true);
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitoringAgentBrain.class);
		setAvailableActions(new ArrayList<>(getVacuumWorldActions()));
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		return buildPerceiveAction();
	}
	
	@Override
	public void execute(EnvironmentalAction action) {
		this.lastAttemptedActionResult = null;
		this.lastCycleIncomingSpeeches = new ArrayList<>();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldMonitoringAgentBrain.class);
	}
	
	@Override
	public void setVacuumWorldMonitoringAgentActions() {
		super.setVacuumWorldMonitoringAgentActions();
	}
	
	@Override
	protected EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
		if(TotalPerceptionAction.class.isAssignableFrom(actionPrototype)) {
			return buildPerceiveAction();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	protected EnvironmentalAction buildPerceiveAction() {
		try {
			return TotalPerceptionAction.class.newInstance();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return new TotalPerceptionAction();
		}
	}
	
	@Override
	protected SpeechAction buildSpeechAction(String senderId, List<String> recipientIds, VacuumWorldSpeechPayload payload) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void updateAvailableActions(VacuumWorldPerception perception) {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	protected void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
		throw new UnsupportedOperationException();
	}
}