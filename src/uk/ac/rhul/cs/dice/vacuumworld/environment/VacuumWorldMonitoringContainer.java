package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurningAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.DirtRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VacuumWorldSpaceRepresentation;

public class VacuumWorldMonitoringContainer extends EnvironmentalSpace {
  private List<VWObserverAgent> observerAgents;
  private List<VWEvaluatorAgent> evaluatorAgents;
  private VacuumWorldMonitoringPhysics physics;
  private VacuumWorldSpace subContainerSpace;
  private VacuumWorldSpaceRepresentation vacuumWorldSpaceRepresentation;

  public VacuumWorldMonitoringContainer(VacuumWorldMonitoringPhysics physics,
      VacuumWorldSpace space) {
    this.physics = physics;
    this.subContainerSpace = space;
    this.observerAgents = new ArrayList<>();
    this.evaluatorAgents = new ArrayList<>();
    vacuumWorldSpaceRepresentation = new VacuumWorldSpaceRepresentation();
  }

  public List<VWObserverAgent> getObserverAgents() {
    return this.observerAgents;
  }

  public List<VWEvaluatorAgent> getEvaluatorAgents() {
    return this.evaluatorAgents;
  }

  public VacuumWorldMonitoringPhysics getPhysics() {
    return this.physics;
  }

  public VacuumWorldSpace getSubContainerSpace() {
    return this.subContainerSpace;
  }

  public void addObserverAgent(VWObserverAgent agent) {
    this.observerAgents.add(agent);
    this.addObserver(agent.getSensors().get(agent.getActionResultSensorIndex()));
    ((VWObserverActuator) agent.getActuators().get(
        agent.getActionActuatorIndex())).addObserver(this);
  }

  public void addEvaluatorAgent(VWEvaluatorAgent agent) {
    System.out.println("Adding evaluator");
    this.evaluatorAgents.add(agent);
    ((VWEvaluatorActuator) agent.getActuators().get(
        agent.getActionActuatorIndex())).addObserver(this);
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    // Manage sub container message
    if (o instanceof VacuumWorldPhysics && arg instanceof MonitoringUpdateEvent) {
      manageSubContainerMessage((MonitoringUpdateEvent) arg);
      return;
    }
    // Manage Actuator message
    if (o instanceof VWObserverActuator && arg instanceof MonitoringEvent) {
      manageActuatorRequest((MonitoringEvent) arg);
    }
    // Manage Physics message
    else if (o instanceof VacuumWorldMonitoringPhysics
        && DefaultActionResult.class.isAssignableFrom(arg.getClass())) {
      managePhysicsRequest((DefaultActionResult) arg);
    }
  }

  private void manageSubContainerMessage(MonitoringUpdateEvent event) {
    System.out.println("MESSAGE FROM SUBCONTAINER! : " + event.represent());
    // if the action was not done then it is not relevant
    if (event.getResult().equals(ActionResult.ACTION_DONE)) {
      AgentRepresentation agent = vacuumWorldSpaceRepresentation
          .getAgent((String) ((AbstractAgent) event.getActor()).getId());
      Action a = event.getAction();
      agent.setClean(false);
      agent.setSuccessfulClean(false);
      
      if (a instanceof CleanAction) {
        agent.setClean(true);
        // if the agent was on top of some dirt then remove it from the map
        VacuumWorldCoordinates coord = new VacuumWorldCoordinates(agent.getX(),
            agent.getY());
        if (vacuumWorldSpaceRepresentation.getDirts().get(coord) != null) {
          // remove the dirt as some was found on the location of an agent who
          // is cleaning
          vacuumWorldSpaceRepresentation.dirtCleaned(coord);
          agent.setSuccessfulClean(true);
        }
        // cleaning failed!
      } else if (a instanceof MoveAction) {
        VacuumWorldCoordinates coord = new VacuumWorldCoordinates(agent.getX(),
            agent.getY());
        coord = coord.getNewCoordinates(agent.getDirection());
        agent.setX(coord.getX());
        agent.setY(coord.getY());
      } else if (a instanceof TurningAction) {
        if (a instanceof TurnLeftAction) {
          agent.setDirection(agent.getDirection().getLeftDirection());
        }
        if (a instanceof TurnRightAction) {
          agent.setDirection(agent.getDirection().getRightDirection());
        }
      }
    }
  }

  private void managePhysicsRequest(DefaultActionResult result) {
    notifyAgentSensor(result, result.getRecipientId());
  }

  private void manageActuatorRequest(MonitoringEvent event) {
    notifyObservers(new Object[] { event, this },
        VacuumWorldMonitoringPhysics.class);
  }

  private void notifyAgentSensor(Object arg, String sensorId) {
    List<CustomObserver> recipients = this.getObservers();

    for (CustomObserver recipient : recipients) {
      notifyIfNeeded(recipient, arg, sensorId);
    }
  }

  private void notifyIfNeeded(CustomObserver recipient, Object arg,
      String sensorId) {
    if (recipient instanceof VWObserverSensor) {
      VWObserverSensor s = (VWObserverSensor) recipient;

      if (s.getSensorId().equals(sensorId)) {
        s.update(this, arg);
      }
    }
  }

  public VacuumWorldSpaceRepresentation getVacuumWorldSpaceRepresentation() {
    return vacuumWorldSpaceRepresentation;
  }

  public void setVacuumWorldSpaceRepresentation(
      VacuumWorldSpaceRepresentation vacuumWorldSpaceRepresentation) {
    this.vacuumWorldSpaceRepresentation = vacuumWorldSpaceRepresentation;
  }

  /**
   * Creates the representation of VacuumWorldSpace. Should only be called when
   * the real VacuumWorldSpace is fully set up; contains all agents and dirt
   * etc.
   */
  public void createVacuumWorldSpaceRepresentation() {
    Collection<Location> locations = subContainerSpace.getLocations();
    Iterator<Location> iter = locations.iterator();
    Random rand = new Random();
    while (iter.hasNext()) {
      VacuumWorldLocation loc = (VacuumWorldLocation) iter.next();
      if (loc.isAnAgentPresent()) {
        // create the agent representation
        String agentId = (String) loc.getAgent().getId();
        AgentRepresentation rep = new AgentRepresentation(agentId,
            ((VacuumWorldAgentAppearance) loc.getAgent()
                .getExternalAppearance()).getType(), loc.getAgent()
                .getSensors().size(), loc.getAgent().getActuators().size(), loc
                .getAgent().getFacingDirection(), loc.getAgent()
                .getCurrentLocation().getX(), loc.getAgent()
                .getCurrentLocation().getY());
        vacuumWorldSpaceRepresentation.getAgents().put(agentId, rep);
      }
      if (loc.isAnObstaclePresent()) {
        // create the obstacle representation
        Dirt dirt = loc.getDirt();
        if (dirt != null) {
          vacuumWorldSpaceRepresentation.getDirts()
              .put(
                  new VacuumWorldCoordinates(loc.getCoordinates().getX(), loc
                      .getCoordinates().getY()),
                  new DirtRepresentation(String.valueOf(rand.nextLong()),
                      ((DirtAppearance) dirt.getExternalAppearance())
                          .getDirtType()));
        } else {
          System.err.println("CANNOT REPRESENT: " + loc.getObstacle() + "IN "
              + VacuumWorldSpaceRepresentation.class.getSimpleName());
        }
      }
    }
  }
}