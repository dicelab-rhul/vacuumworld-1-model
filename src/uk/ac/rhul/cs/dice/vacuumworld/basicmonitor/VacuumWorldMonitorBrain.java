package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;

public class VacuumWorldMonitorBrain extends AbstractAgentBrain {

  @Override
  public void update(CustomObservable o, Object arg) {
    System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
        + o.getClass().getSimpleName() + " " + arg);

    if (o instanceof VacuumWorldMonitorMind
        && arg instanceof TotalPerceptionAction) {
      notifyObservers(arg, VacuumWorldMonitorAgent.class);
    } else if (o instanceof VacuumWorldMonitorAgent
        && arg instanceof MonitoringResult) {
      notifyObservers(arg, VacuumWorldMonitorMind.class);
    }
  }
}
