package uk.ac.rhul.cs.dice.vacuumworld.plan;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;

public class PlanFactory {

    private PlanFactory() {}

    public static <T extends Plan> T of(Class<? extends T> prototype, Iterable<Class<? extends EnvironmentalAction>> actions) {
	try {
	    T plan = prototype.newInstance();
	    actions.forEach(plan::pushSingleAction);

	    return plan;
	}
	catch (Exception e) {
	    throw new IllegalArgumentException(e);
	}
    }
}