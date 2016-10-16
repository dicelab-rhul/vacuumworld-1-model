package uk.ac.rhul.cs.dice.vacuumworld.utils.functions;

import java.util.function.Function;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;

public class AgentToId implements Function<VacuumWorldCleaningAgent, String> {

	@Override
	public String apply(VacuumWorldCleaningAgent agent) {
		if(agent != null) {
			return agent.getId();
		}
		else {
			return null;
		}
	}
}