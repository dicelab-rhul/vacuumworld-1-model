package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldServer;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;

public class VacuumWorldMonitorAgent extends AbstractAgent implements
    VacuumWorldAgentInterface {

  public VacuumWorldMonitorAgent(AbstractAgentAppearance appearance,
      List<Sensor> sensors, List<Actuator> actuators, AbstractAgentMind mind,
      AbstractAgentBrain brain) {
    super(appearance, sensors, actuators, mind, brain);
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitorBrain
        && arg instanceof TotalPerceptionAction) {
      MonitoringEvent event = new MonitoringEvent((EnvironmentalAction) arg, (long) VacuumWorldServer.getCycleNumber(), this);
      event.setActuatorRecipient(((VacuumWorldMonitorActuator) this.getActuators().get(
          this.getActionActuatorIndex())).getActuatorId());
      event.setSensorToCallBackId(((VacuumWorldMonitorSensor) this.getSensors().get(
          this.getActionResultSensorIndex())).getSensorId());
      notifyObservers(event, VacuumWorldMonitorActuator.class);
    } else if (o instanceof VacuumWorldMonitorSensor && arg instanceof MonitoringResult) {
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
