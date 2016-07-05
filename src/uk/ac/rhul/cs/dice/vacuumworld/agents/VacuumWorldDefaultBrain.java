package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;

public class VacuumWorldDefaultBrain extends AbstractAgentBrain {

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldDefaultMind) {
      manageMindRequest(arg);
    } else if (o instanceof VacuumWorldCleaningAgent) {
      manageBodyRequest(arg);
    }
  }

  private void manageBodyRequest(Object arg) {
    if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
      notifyObservers(arg, VacuumWorldDefaultMind.class);
    }
  }

  private void manageMindRequest(Object arg) {
    if (AbstractAction.class.isAssignableFrom(arg.getClass())) {
      notifyObservers(arg, VacuumWorldCleaningAgent.class);
    }
  }
}