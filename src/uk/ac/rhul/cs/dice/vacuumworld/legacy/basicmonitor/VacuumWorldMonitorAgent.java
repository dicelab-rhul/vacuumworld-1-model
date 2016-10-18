package uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldMonitorAgent extends AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> implements VacuumWorldAgentInterface {

	public VacuumWorldMonitorAgent(AbstractAgentAppearance appearance, List<Sensor<VacuumWorldSensorRole>> sensors, List<Actuator<VacuumWorldActuatorRole>> actuators, AbstractAgentMind mind, AbstractAgentBrain brain) {
		super(appearance, sensors, actuators, mind, brain);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldMonitorBrain && arg instanceof TotalPerceptionAction) {
			MonitoringEvent event = new MonitoringEvent((EnvironmentalAction) arg, (long) Utils.getCycleNumber(), this);
			event.setActuatorRecipient(((VacuumWorldMonitorActuator) this.getActuators().get(this.getActionActuatorIndex())).getActuatorId());
			event.setSensorToCallBackId(((VacuumWorldMonitorSensor) this.getSensors().get(this.getActionResultSensorIndex())).getSensorId());
			notifyObservers(event, VacuumWorldMonitorActuator.class);
		}
		else if (o instanceof VacuumWorldMonitorSensor && arg instanceof MonitoringResult) {
			notifyObservers(arg, VacuumWorldMonitorBrain.class);
		}
	}

	@Override
	public int getActionActuatorIndex() {
		return 0;
	}

	@Override
	public int getActionResultSensorIndex() {
		return 0;
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