package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class UserBrain extends AbstractAgentBrain<VacuumWorldPerception> {
	private ConcurrentLinkedQueue<DefaultActionResult<VacuumWorldPerception>> receivedResults;
	private List<DefaultActionResult<VacuumWorldPerception>> resultsToSend;
	private boolean actionResultReturned;
	
	public UserBrain() {
		super(UserMind.class);
		
		this.receivedResults = new ConcurrentLinkedQueue<>();
		this.resultsToSend = new ArrayList<>();
		this.actionResultReturned = false;
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

	@SuppressWarnings("unchecked")
	private void manageUserBodyRequest(Object arg) {
		if (DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
			this.receivedResults.add((DefaultActionResult<VacuumWorldPerception>) arg);
			this.actionResultReturned = true;
		}
	}

	private void manageUserMindRequest(Object arg) {
		if (arg == null) {
			manageMindPullRequest();
		}
		else if (AbstractAction.class.isAssignableFrom(arg.getClass()) && !CleanAction.class.isAssignableFrom(arg.getClass())){
			this.actionResultReturned = false;
			notifyObservers(arg, User.class);
		}
	}
	
	@Override
	public void updateResultsToSend() {
		while (!this.receivedResults.isEmpty()) {
			this.resultsToSend.add(this.receivedResults.poll());
		}
	}

	@Override
	public void manageMindPullRequest() {
		updateResultsToSend();
		
		if (this.actionResultReturned) {
			notifyObservers(this.resultsToSend, UserMind.class);
			this.resultsToSend.clear();
		}
	}
}