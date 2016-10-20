package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;

public class VacuumWorldMonitoringAgentBrain extends VacuumWorldDefaultBrain {

	public VacuumWorldMonitoringAgentBrain(Class<? extends VacuumWorldDefaultMind> mindClass) {
		super(mindClass);
	}
	
	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringAgentMind) {
			manageMonitoringMindRequest(arg);
		}
		else if(o instanceof VacuumWorldMonitoringAgent && arg instanceof DefaultActionResult) {
			manageBodyRequest((DefaultActionResult) arg);
		}
	}

	private void manageMonitoringMindRequest(Object arg) {
		if(arg == null) {
			manageMindRequest();
		}
		else if(AbstractAction.class.isAssignableFrom(arg.getClass())) {
			executeAction((AbstractAction) arg);
		}
	}

	private void executeAction(AbstractAction action) {
		this.actionResultReturned = false;
		notifyObservers(action, VacuumWorldMonitoringAgent.class);
	}
}