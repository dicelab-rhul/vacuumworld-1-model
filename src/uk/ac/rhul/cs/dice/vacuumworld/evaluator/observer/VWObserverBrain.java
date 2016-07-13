package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Brain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;

/**
 * The Vacuum World implementation of {@link ObserverBrain}. This {@link Brain}
 * is set up to handle {@link TotalPerceptionAction TotalPerceptionActions} from
 * the {@link VWObserverMind} and {@link MonitoringResult MonitoringResults}
 * from the {@link VWObserverAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverBrain extends ObserverBrain {
	private MonitoringResult currentPerception;

	@Override
	public void updateCon(CustomObservable o, Object arg) {
		if (o instanceof VWObserverMind) {
			manageMindMessage(arg);
		}
		else if (o instanceof VWObserverAgent && arg instanceof MonitoringResult) {
			this.currentPerception = (MonitoringResult) arg;
		}
	}

	private void manageMindMessage(Object arg) {
		if (arg == null) {
			notifyObservers(this.currentPerception, VWObserverMind.class);
		}
		else if (arg instanceof TotalPerceptionAction) {
			notifyObservers(arg, VWObserverAgent.class);
		}
	}
}