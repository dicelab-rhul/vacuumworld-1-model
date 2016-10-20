package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.DatabaseResult;
import uk.ac.rhul.cs.dice.monitor.agents.AgentClassModel;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorAgent;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldLegacyMonitoringContainer;

public class VWEvaluatorAgent extends EvaluatorAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> implements VacuumWorldAgentInterface {

	public VWEvaluatorAgent(AbstractAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, VWEvaluatorMind mind, VWEvaluatorBrain brain, AgentClassModel classModel, AbstractMongoBridge database) {
		super(appearance, sensors, actuators, mind, brain, classModel, database);
	}
	

	@Override
	protected void manageDatabaseResult(DatabaseResult result) {
	  //changed results are always relevant
    notifyObservers(result, this.getClassModel().getBrainClass());
	}
	
	/**
	 * Returns the default {@link List} index of the {@link Actuator} that will
	 * be handling {@link EnvironmentalAction}s to the environment.
	 * 
	 * @return the index
	 */
	public int getActionActuatorIndex() {
		return 0;
	}

	/**
	 * Returns the default {@link List} index of the {@link Sensor} that will be
	 * handling {@link MonitoringResult MonitoringResults} from
	 * {@link VacuumWorldLegacyMonitoringContainer}.
	 * 
	 * @return the index
	 */
	public int getActionResultSensorIndex() {
		return 0;
	}

	// ***** NOT USED ***** //
	@Override
	public void updateCon(CustomObservable o, Object arg) {
		//Useless
	}

	@Override
	public int getPerceptionRange() {
		return 0;
	}

	@Override
	public boolean canSeeBehind() {
		return true;
	}

	@Override
	public Object simulate() {
		return null;
	}
}