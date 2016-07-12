package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;

public class VacuumWorldDefaultBrain extends AbstractAgentBrain {

  //TODO change to include speech
  private VacuumWorldActionResult currentResult;
  
  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldDefaultMind) {
      manageMindRequest(arg);
    } else if (o instanceof VacuumWorldCleaningAgent) {
      manageBodyRequest(arg);
    }
  }

  private void manageBodyRequest(Object arg) {
    //TODO emanuele change this to include speech
    if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
      currentResult = (VacuumWorldActionResult)arg;
    }
  }

  private void manageMindRequest(Object arg) {
    //TODO potentially change to some getPercepts related action
    if(arg == null) { 
      //notify the mind with the current perception
      notifyObservers(currentResult, VacuumWorldDefaultMind.class);
    } else if (AbstractAction.class.isAssignableFrom(arg.getClass())) {
      notifyObservers(arg, VacuumWorldCleaningAgent.class);
    } 
  }
}