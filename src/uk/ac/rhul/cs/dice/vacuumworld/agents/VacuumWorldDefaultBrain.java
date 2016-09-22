package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;

public class VacuumWorldDefaultBrain extends AbstractAgentBrain {
  private ConcurrentLinkedQueue<DefaultActionResult> receivedResults;
  private List<DefaultActionResult> resultsToSend;
  private boolean actionResultReturned;
  private Class<? extends VacuumWorldDefaultMind> mindClass;

  public VacuumWorldDefaultBrain(
      Class<? extends VacuumWorldDefaultMind> mindClass) {
    this.mindClass = mindClass;
    this.receivedResults = new ConcurrentLinkedQueue<>();
    this.resultsToSend = new ArrayList<>();
    this.actionResultReturned = false;
  }

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
      this.receivedResults.add((DefaultActionResult) arg);
      this.actionResultReturned = true;
    }
  }

  private void manageMindRequest(Object arg) {
    if (arg == null) {
      updateResultsToSend();
      if (this.actionResultReturned) {
        notifyObservers(this.resultsToSend, mindClass);
        resultsToSend.clear();
      }
    } else if (AbstractAction.class.isAssignableFrom(arg.getClass())) {
      this.actionResultReturned = false;
      notifyObservers(arg, VacuumWorldCleaningAgent.class);
    }
  }

  private void updateResultsToSend() {
    while (!this.receivedResults.isEmpty()) {
      this.resultsToSend.add(this.receivedResults.poll());
    }
  }
}