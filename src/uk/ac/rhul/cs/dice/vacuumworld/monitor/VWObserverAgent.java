package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;

public class VWObserverAgent extends ObserverAgent implements VacuumWorldAgentInterface {

  public VWObserverAgent(AbstractAgentAppearance appearance,
      List<Sensor> sensors, List<Actuator> actuators, AbstractAgentMind mind,
      AbstractAgentBrain brain, AbstractMongoBridge database,
      CollectionRepresentation collectionRepresentation, Class<?> brainObserver, Class<?> actuatorObserver) {
    super(appearance, sensors, actuators, mind, brain, database,
        collectionRepresentation, brainObserver, actuatorObserver);
  }

  @Override
  public Object simulate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    System.out.println("UPDATE " + this.getClass().getSimpleName() + " FROM "
        + o.getClass().getSimpleName() + " " + arg);
    if(o instanceof VWObserverBrain && arg instanceof Action) {
      //TODO timestamp
      MonitoringEvent event = new MonitoringEvent((Action)arg,(long)1,this);
      event.setActuatorRecipient(((VWObserverActuator)this.getActuators().get(this.getActionActuatorIndex())).getActuatorId());
      event.setSensorToCallBackId(((VWObserverSensor)this.getSensors().get(this.getActionResultSensorIndex())).getSensorId());
      notifyObservers(event, VWObserverActuator.class);
    } else if(o instanceof VWObserverSensor && arg instanceof MonitoringResult) {
      notifyObservers(arg, VWObserverBrain.class);
    }
  }

  /**
   * Returns the default {@link List} index of the {@link Sensor} that will be
   * handling {@link ActionResult}s from the environment.
   * 
   * @return
   */
  public int getActionResultSensorIndex() {
    return 0;
  }

  @Override
  public int getPerceptionRange() {
    return 10; //TODO new action that doesnt rely on perception ranges? total perception?
  }

  @Override
  public boolean canSeeBehind() {
    return true;
  }

  @Override
  public int getActionActuatorIndex() {
    return 0;
  }
  
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
