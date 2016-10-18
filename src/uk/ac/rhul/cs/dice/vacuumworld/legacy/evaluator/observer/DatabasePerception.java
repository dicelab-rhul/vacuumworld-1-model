package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import java.util.Set;

import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database.AgentDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database.DirtDatabaseRepresentation;

public class DatabasePerception implements RefinedPerception {

	private Set<AgentDatabaseRepresentation> agents;
	private Set<DirtDatabaseRepresentation> dirts;
	private Integer lastCycleRead = null;

	public DatabasePerception(Set<AgentDatabaseRepresentation> agents, Set<DirtDatabaseRepresentation> dirts) {
		this.agents = agents;
		this.dirts = dirts;
	}

	public Set<AgentDatabaseRepresentation> getAgents() {
		return this.agents;
	}

	public Set<DirtDatabaseRepresentation> getDirts() {
		return this.dirts;
	}

	public Integer getLastCycleRead() {
		return this.lastCycleRead;
	}

	public void setLastCycleRead(Integer lastCycleRead) {
		this.lastCycleRead = lastCycleRead;
	}
}