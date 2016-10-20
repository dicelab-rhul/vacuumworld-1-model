package uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionActionOld;

public class VacuumWorldMonitorBrain extends AbstractAgentBrain {
	private MonitoringResult currentPerception;

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitorMind) {
			manageMindRequest(arg);
		}
		else if (o instanceof VacuumWorldMonitorAgent && arg instanceof MonitoringResult) {
			this.currentPerception = (MonitoringResult) arg;
		}
	}

	private void manageMindRequest(Object arg) {
		if (arg == null) {
			notifyObservers(this.currentPerception, VacuumWorldMonitorMind.class);
		}
		else if (arg instanceof TotalPerceptionActionOld) {
			notifyObservers(arg, VacuumWorldMonitorAgent.class);
		}
	}
}