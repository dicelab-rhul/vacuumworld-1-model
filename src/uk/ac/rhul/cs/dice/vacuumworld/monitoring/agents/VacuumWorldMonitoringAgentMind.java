package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractAgentMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldMonitoringAgentMind extends VacuumWorldAbstractAgentMind<VacuumWorldMonitoringPerception> {
	public VacuumWorldMonitoringAgentMind(String bodyId) {
		super(bodyId);
		
		super.setPerceptionRange(Integer.MAX_VALUE);
		super.setCanSeeBehind(true);
	}
	
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VacuumWorldMonitoringAgentBrain.class);
		loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
	}
	
	@Override
	public EnvironmentalAction<VacuumWorldMonitoringPerception> decide(Object... parameters) {
		return buildPerceiveAction();
	}
	
	@Override
	public void execute(EnvironmentalAction<VacuumWorldMonitoringPerception> action) {
		setLastActionResult(null);
		clearReceivedCommunications();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldMonitoringAgentBrain.class);
	}
	
	public EnvironmentalAction<VacuumWorldMonitoringPerception> buildNewAction(Class<? extends EnvironmentalAction<VacuumWorldPerception>> actionPrototype) {
		if(TotalPerceptionAction.class.isAssignableFrom(actionPrototype)) {
			return buildPerceiveAction();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	public EnvironmentalAction<VacuumWorldMonitoringPerception> buildPerceiveAction() {
		try {
			return TotalPerceptionAction.class.newInstance();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return new TotalPerceptionAction();
		}
	}

	@Override
	public EnvironmentalAction<VacuumWorldMonitoringPerception> decideActionRandomly() {
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
}