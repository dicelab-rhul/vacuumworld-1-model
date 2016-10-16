package uk.ac.rhul.cs.dice.vacuumworld.agents.user;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;

public class UserAppearance extends AbstractAgentAppearance {

	public UserAppearance(String name, Double[] dimensions) {
		super(name, dimensions);
	}

	@Override
	public String represent() {
		return "U";
	}
}