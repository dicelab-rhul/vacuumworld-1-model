package uk.ac.rhul.cs.dice.vacuumworld.plan;

import java.util.Deque;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public interface Plan {
    public abstract boolean isSimple();
    public abstract boolean isMadeByASingleAction();
    public abstract Deque<Plan> getSubPlans();
    public abstract Deque<Class<? extends EnvironmentalAction>> getActions();
    public abstract Plan pop();
    public abstract Plan peek();
    public abstract Class<? extends EnvironmentalAction> popIfSimple();
    public abstract Class<? extends EnvironmentalAction> peekIfSimple();
    public abstract void pushSingleAction(Class<? extends EnvironmentalAction> action);
    public abstract void pushSubPlan(Plan subPlan);
}