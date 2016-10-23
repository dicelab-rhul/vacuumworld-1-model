package uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AbstractActorRunnable;

public class VacuumWorldMonitoringActorRunnable extends AbstractActorRunnable {

	public VacuumWorldMonitoringActorRunnable(Mind agentMind) {
		super(agentMind);
	}

	@Override
	public void run() {
		super.getState().run(this);
	}
}
