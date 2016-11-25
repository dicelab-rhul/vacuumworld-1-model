package uk.ac.rhul.cs.dice.vacuumworld.utils.predicates;

import java.util.List;
import java.util.function.Predicate;

import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class Contained implements Predicate<String> {
    private List<String> container;

    public Contained(List<String> container) {
	this.container = container;
    }

    @Override
    public boolean test(String candidate) {
	try {
	    return this.container.contains(candidate);
	}
	catch (Exception e) {
	    VWUtils.fakeLog(e);

	    return false;
	}
    }
}