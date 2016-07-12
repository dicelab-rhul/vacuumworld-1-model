package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;

public class VacuumWorldMonitorBrain extends AbstractAgentBrain {

  private MonitoringResult currentPerception;

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorMind) {
      if (arg == null) {
        notifyObservers(currentPerception, VacuumWorldMonitorMind.class);
      } else if (arg instanceof TotalPerceptionAction) {
        notifyObservers(arg, VacuumWorldMonitorAgent.class);
      }
    } else if (o instanceof VacuumWorldMonitorAgent
        && arg instanceof MonitoringResult) {
      currentPerception = (MonitoringResult) arg;
    }
  }
}
