package uk.ac.rhul.cs.dice.vacuumworld.utils.functions;

import java.util.function.Function;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;

public class AgentToId implements Function<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>, String> {

	@Override
	public String apply(AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> agent) {
		if(agent != null) {
			return agent.getId().toString();
		}
		else {
			return null;
		}
	}
}