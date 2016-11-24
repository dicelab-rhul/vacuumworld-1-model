package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;

public class UserBrain extends AbstractAgentBrain {

    public UserBrain() {
	super(UserMind.class);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof UserMind) {
	    manageUserMindRequest(arg);
	}
	else if (o instanceof User) {
	    manageUserBodyRequest(arg);
	}
    }

    private void manageUserBodyRequest(Object arg) {
	if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass())) {
	    pushReceivedResultToQueue((VacuumWorldActionResult) arg);
	    setActionResultReturned(true);
	}
	else if(arg instanceof VacuumWorldSpeechActionResult) {
	    pushReceivedResultToQueue((VacuumWorldSpeechActionResult) arg);
	    setActionResultReturned(true);
	}
    }

    private void manageUserMindRequest(Object arg) {
	if (arg == null) {
	    manageMindPullRequest();
	} 
	else if (EnvironmentalAction.class.isAssignableFrom(arg.getClass()) && !CleanAction.class.isAssignableFrom(arg.getClass())) {
	    setActionResultReturned(false);
	    notifyObservers(arg, User.class);
	}
    }

    @Override
    public void manageMindPullRequest() {
	updateResultsToSend();

	if (isActionResultReturned()) {
	    notifyObservers(getResultsToSend(), UserMind.class);
	    clearResultsToSend();
	}
    }
}