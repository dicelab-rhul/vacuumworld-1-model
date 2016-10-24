package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;

public class UserBrain extends AbstractAgentBrain {
	private ConcurrentLinkedQueue<VacuumWorldActionResult> receivedResults;
	private List<VacuumWorldActionResult> resultsToSend;
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

	private void manageUserBodyRequest(Object arg) {
		if (VacuumWorldActionResult.class.isAssignableFrom(arg.getClass())) {
			this.receivedResults.add((VacuumWorldActionResult) arg);
			this.actionResultReturned = true;
		}
	}

	private void manageUserMindRequest(Object arg) {
		if (arg == null) {
			manageMindPullRequest();
		}
		else if (EnvironmentalAction.class.isAssignableFrom(arg.getClass()) && !CleanAction.class.isAssignableFrom(arg.getClass())){
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