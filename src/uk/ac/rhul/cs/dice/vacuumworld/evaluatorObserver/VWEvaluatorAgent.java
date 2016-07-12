package uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.AbstractAgentAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.agents.AgentClassModel;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorAgent;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldAgentInterface;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;

public class VWEvaluatorAgent extends EvaluatorAgent implements
    VacuumWorldAgentInterface {

  public VWEvaluatorAgent(AbstractAgentAppearance appearance,
      List<Sensor> sensors, List<Actuator> actuators, VWEvaluatorMind mind,
      VWEvaluatorBrain brain, AgentClassModel classModel,
      AbstractMongoBridge database,
      CollectionRepresentation collectionRepresentation) {
    super(appearance, sensors, actuators, mind, brain, classModel, database,
        collectionRepresentation);
  }

  /**
   * Returns the default {@link List} index of the {@link Actuator} that will be
   * handling {@link EnvironmentalAction}s to the environment.
   * 
   * @return the index
   */
  @Override
  public int getActionActuatorIndex() {
    return 0;
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

  // ***** NOT USED ***** //
  
  @Override
  public void updateCon(CustomObservable o, Object arg) {
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
