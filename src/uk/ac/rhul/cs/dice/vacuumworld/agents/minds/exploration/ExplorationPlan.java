package uk.ac.rhul.cs.dice.vacuumworld.agents.minds.exploration;

import java.util.LinkedList;
import java.util.Queue;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public class ExplorationPlan {
    private Queue<Class<? extends EnvironmentalAction>> actionsToPerform;
    
    public ExplorationPlan() {
	this.actionsToPerform = new LinkedList<>();
    }
    
    public boolean enqueueActionPrototype(Class<? extends EnvironmentalAction> actionPrototype) {
	return this.actionsToPerform.add(actionPrototype);
    }
    
    public Class<? extends EnvironmentalAction> peek() {
	return this.actionsToPerform.peek();
    }
    
    public Class<? extends EnvironmentalAction> retrieveActioToPerformPrototype() {
	return this.actionsToPerform.poll();
    }
    
    public boolean isEmpty() {
	return this.actionsToPerform.isEmpty();
    }
}