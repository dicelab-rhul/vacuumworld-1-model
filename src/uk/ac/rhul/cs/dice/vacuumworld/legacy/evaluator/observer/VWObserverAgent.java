package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.Agent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Brain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.AgentClassModel;
import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgent;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

/**
 * The Vacuum World implementation of {@link ObserverAgent}. This {@link Agent}
 * is set up to handle {@link TotalPerceptionAction TotalPerceptionActions} from
 * {@link VWObserverBrain} and {@link MonitoringResult MonitoringResults} from
 * {@link VWObserverActuator VWObserverActuators}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverAgent extends ObserverAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> implements VacuumWorldAgentInterface {

	/**
	 * Constructor.
	 * 
	 * @param appearance
	 *            of the agent
	 * @param sensors
	 *            that the agent will use
	 * @param actuators
	 *            that the agent will use
	 * @param mind
	 *            that the agent will use
	 * @param brain
	 *            that the agent will use @ classModel the class model of this
	 *            agent
	 * @param database
	 *            that will be used in the {@link DatabaseAgent}
	 * @param brainObserver
	 *            the class of the {@link Brain} that will interact with this
	 *            agent
	 * @param actuatorObserver
	 *            the class of the {@link Actuator Actuators} that will interact
	 *            with this agent
	 */
	public VWObserverAgent(AbstractAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, VWObserverMind mind, VWObserverBrain brain, AgentClassModel classModel, AbstractMongoBridge database) {
		super(appearance, sensors, actuators, mind, brain, classModel, database);
	}

	@Override
	public void updateCon(CustomObservable o, Object arg) {
		if (o instanceof VWObserverBrain && arg instanceof TotalPerceptionAction) {
			MonitoringEvent event = new MonitoringEvent((EnvironmentalAction) arg, (long) Utils.getCycleNumber(), this);
			event.setActuatorRecipient(((VWObserverActuator) this.getActuators().get(this.getActionActuatorIndex())).getActuatorId());
			event.setSensorToCallBackId(((VWObserverSensor) this.getSensors().get(this.getActionResultSensorIndex())).getSensorId());
			notifyObservers(event, VWObserverActuator.class);
		}
		else if (o instanceof VWObserverSensor && arg instanceof MonitoringResult) {
			notifyObservers(arg, VWObserverBrain.class);
		}
	}

	/**
	 * Returns the default {@link List} index of the {@link Sensor} that will be
	 * handling {@link MonitoringResult MonitoringResults} from
	 * {@link VacuumWorldMonitoringContainer}.
	 * 
	 * @return the index
	 */
	@Override
	public int getActionResultSensorIndex() {
		return 0;
	}

	/**
	 * Returns the default {@link List} index of the {@link Actuator} that will
	 * be sending {@link MonitoringEvent MonitoringEvents}.
	 * 
	 * @return the index
	 */
	@Override
	public int getActionActuatorIndex() {
		return 0;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	// ***** NOT USED ***** //
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