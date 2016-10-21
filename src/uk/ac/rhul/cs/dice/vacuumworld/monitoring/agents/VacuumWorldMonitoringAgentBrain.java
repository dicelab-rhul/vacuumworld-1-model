package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;

public class VacuumWorldMonitoringAgentBrain extends AbstractAgentBrain<VacuumWorldMonitoringPerception> {
	
	public VacuumWorldMonitoringAgentBrain() {
		super(VacuumWorldMonitoringAgentMind.class);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if(o instanceof VacuumWorldMonitoringAgentMind) {
			manageMonitoringMindRequest(arg);
		}
		else if(o instanceof VacuumWorldMonitoringAgent && arg instanceof DefaultActionResult) {
			manageBodyRequest(arg);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void manageBodyRequest(Object arg) {
		if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			pushReceivedResultToQueue((DefaultActionResult<VacuumWorldMonitoringPerception>) arg);
			setActionResultReturned(true);
		}
	}

	private void manageMonitoringMindRequest(Object arg) {
		if(arg == null) {
			manageMindPullRequest();
		}
		else if(EnvironmentalAction.class.isAssignableFrom(arg.getClass())) {
			setActionResultReturned(false);
			notifyObservers(arg, VacuumWorldMonitoringAgent.class);
		}
	}

	@Override
	public void manageMindPullRequest() {
		updateResultsToSend();
		
		if (isActionResultReturned()) {
			notifyObservers(getResultsToSend(), getPairedMindClass());
			clearResultsToSend();
		}
	}
}