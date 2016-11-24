package uk.ac.rhul.cs.dice.vacuumworld.plan;

import java.util.Deque;
import java.util.LinkedList;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public abstract class AbstractPlan implements Plan {
    private Deque<Plan> subPlans;
    private Deque<Class<? extends EnvironmentalAction>> simplePlan;
    
    public AbstractPlan() {
	this.subPlans = new LinkedList<>();
	this.simplePlan = new LinkedList<>();
    }
    
    @Override
    public boolean isSimple() {
	return this.subPlans.isEmpty();
    }
    
    @Override
    public boolean isMadeByASingleAction() {
	return isSimple() && this.simplePlan.size() == 1;
    }
    
    @Override
    public Deque<Plan> getSubPlans() {
	return this.subPlans;
    }
    
    @Override
    public Deque<Class<? extends EnvironmentalAction>> getActions() {
	return this.simplePlan;
    }
    
    @Override
    public Plan pop() {
	return this.subPlans.pop();
    }
    
    @Override
    public Plan peek() {
	return this.subPlans.peek();
    }
    
    @Override
    public void pushSingleAction(Class<? extends EnvironmentalAction> action) {
	this.simplePlan.push(action);
    }
    
    @Override
    public void pushSubPlan(Plan subPlan) {
	if(!this.simplePlan.isEmpty()) {
	    Plan plan = PlanFactory.of(getClass(), this.simplePlan);
	    
	    this.subPlans.push(plan);
	    this.simplePlan.clear();
	}
	
	this.subPlans.push(subPlan);
    }

    @Override
    public Class<? extends EnvironmentalAction> popIfSimple() {
	return this.simplePlan.pop();
    }

    @Override
    public Class<? extends EnvironmentalAction> peekIfSimple() {
	return this.simplePlan.peek();
    }
}