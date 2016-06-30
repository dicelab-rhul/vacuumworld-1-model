package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentBrain;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorAgent;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;

public class VWEvaluatorAgent extends EvaluatorAgent implements
    VacuumWorldAgentInterface {

  public VWEvaluatorAgent(AbstractAgentAppearance appearance,
      List<Sensor> sensors, List<Actuator> actuators, AbstractAgentMind mind,
      AbstractAgentBrain brain, AbstractMongoBridge database,
      CollectionRepresentation collectionRepresentation) {
    super(appearance, sensors, actuators, mind, brain, database,
        collectionRepresentation);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Object simulate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateCon(CustomObservable o, Object arg) {
    // TODO Auto-generated method stub

  }

  /**
   * Returns the default {@link List} index of the {@link Actuator} that will be
   * handling {@link Action}s to the environment.
   * 
   * @return
   */
  @Override
  public int getActionActuatorIndex() {
    return 0;
  }

  @Override
  public int getPerceptionRange() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean canSeeBehind() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getActionResultSensorIndex() {
    // TODO Auto-generated method stub
    return 0;
  }
}
