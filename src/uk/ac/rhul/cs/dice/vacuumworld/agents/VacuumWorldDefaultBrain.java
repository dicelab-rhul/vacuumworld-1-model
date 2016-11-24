package uk.ac.rhul.cs.dice.vacuumworld.agents;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;

public class VacuumWorldDefaultBrain extends AbstractAgentBrain {

    public VacuumWorldDefaultBrain(Class<? extends VacuumWorldDefaultMind> mindClass) {
	super(mindClass);
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldDefaultMind) {
	    manageMindRequest(arg);
	}
	else if (o instanceof VacuumWorldCleaningAgent) {
	    manageBodyRequest(arg);
	}
    }

    protected void manageBodyRequest(Object arg) {
	if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass())) {
	    pushReceivedResultToQueue((VacuumWorldActionResult) arg);
	    setActionResultReturned(true);
	}
	else if(VacuumWorldSpeechActionResult.class.isAssignableFrom(arg.getClass())) {
	    pushReceivedResultToQueue((VacuumWorldSpeechActionResult) arg);
	    setActionResultReturned(true);
	}
    }

    private void manageMindRequest(Object arg) {
	if (arg == null) {
	    manageMindPullRequest();
	}
	else if (EnvironmentalAction.class.isAssignableFrom(arg.getClass())) {
	    setActionResultReturned(false);
	    notifyObservers(arg, VacuumWorldCleaningAgent.class);
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