package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPerceptionResultWrapper;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultSensor;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.Obstacle;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.Lockable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldPhysics extends AbstractPhysics implements
    VacuumWorldPhysicsInterface {
  private ConcurrentMap<Long, VacuumWorldCleaningAgent> activeAgents;
  private ConcurrentMap<Long, List<String>> sensorsToNotify;

  public VacuumWorldPhysics() {
    this.activeAgents = new ConcurrentHashMap<>();
    this.sensorsToNotify = new ConcurrentHashMap<>();
  }

  @Override
  public synchronized Result attempt(Event event, Space context) {
    this.activeAgents.put(Thread.currentThread().getId(),
        (VacuumWorldCleaningAgent) event.getActor());
    this.sensorsToNotify.putIfAbsent(Thread.currentThread().getId(),
        new ArrayList<>());
    this.sensorsToNotify.get(Thread.currentThread().getId()).add(
        ((VacuumWorldEvent) event).getSensorToCallBackId());
    
    // we always need to return a perception it is handled here!
    Result r = event.getAction().attempt(this, context);
    VacuumWorldActionResult result = null;
    if(r instanceof VacuumWorldActionResult) {
      result = (VacuumWorldActionResult) r;
    } else if (r instanceof VacuumWorldSpeechPerceptionResultWrapper) {
      doPerceptionAndSensorIds(((VacuumWorldSpeechPerceptionResultWrapper)r).getPerceptionResult(), context);
      return r;
    } else if (r instanceof DefaultActionResult) {
      //this is usually the case when an fail/impossible
      result = new VacuumWorldActionResult((DefaultActionResult)r);
    } else {
      Logger.getGlobal().log(Level.SEVERE, "An unknown Result has been encountered in VacuumWorldPhysics: " + r.getClass());
    }
    doPerceptionAndSensorIds(result, context);
    return result;
    
  }
  
  private void doPerceptionAndSensorIds(VacuumWorldActionResult result, Space context) {
    VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context,
        this.activeAgents.get(Thread.currentThread().getId())
            .getPerceptionRange(),
        this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
    result.setPerception(newPerception);
    // if an action failed or was impossible then we still need the id to send
    // back
    if (!result.getActionResult().equals(ActionResult.ACTION_DONE)) {
      result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  private synchronized VacuumWorldLocation getCurrentActorLocation(
      VacuumWorldSpace context) {
    VacuumWorldCoordinates coordinates = this.activeAgents.get(
        Thread.currentThread().getId()).getCurrentLocation();

    return (VacuumWorldLocation) context.getLocation(coordinates);
  }

  private synchronized void releaseWriteLockIfNecessary(Lockable lockable) {
    lockable.releaseExclusiveWriteLock();
  }

  /**
   * A turning action is always possible.
   */
  @Override
  public synchronized boolean isPossible(TurnLeftAction action, Space context) {
    return true;
  }

  @Override
  public synchronized boolean isNecessary(TurnLeftAction action, Space context) {
    return false;
  }

  @Override
  public synchronized Result perform(TurnLeftAction action, Space context) {
    VacuumWorldLocation agentLocation = null;

    try {
      agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
      agentLocation.getExclusiveWriteLock();
      action.setAgentOldFacingDirection(agentLocation.getAgent()
          .getFacingDirection());
      agentLocation.getAgent().turnLeft();
      agentLocation.releaseExclusiveWriteLock();

      return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));

    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      releaseWriteLockIfNecessary(agentLocation);
      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  @Override
  public synchronized boolean succeeded(TurnLeftAction action, Space context) {
    return this.activeAgents.get(Thread.currentThread().getId())
        .getFacingDirection() == action.getAgentOldFacingDirection()
        .getLeftDirection();
  }

  /**
   * A turning action is always possible.
   */
  @Override
  public synchronized boolean isPossible(TurnRightAction action, Space context) {
    return true;
  }

  @Override
  public synchronized boolean isNecessary(TurnRightAction action, Space context) {
    return false;
  }

  @Override
  public synchronized Result perform(TurnRightAction action, Space context) {
    VacuumWorldLocation agentLocation = null;

    try {
      agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
      agentLocation.getExclusiveWriteLock();
      action.setAgentOldFacingDirection(agentLocation.getAgent()
          .getFacingDirection());
      agentLocation.getAgent().turnRight();
      agentLocation.releaseExclusiveWriteLock();

      return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));

    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      releaseWriteLockIfNecessary(agentLocation);
      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  @Override
  public synchronized boolean succeeded(TurnRightAction action, Space context) {
    return this.activeAgents.get(Thread.currentThread().getId())
        .getFacingDirection() == action.getAgentOldFacingDirection()
        .getRightDirection();
  }

  @Override
  public synchronized boolean isPossible(MoveAction action, Space context) {
    AgentFacingDirection agentFacingDirection = this.activeAgents.get(
        Thread.currentThread().getId()).getFacingDirection();
    VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
    VacuumWorldCoordinates originalCooridinates = agentLocation
        .getCoordinates();
    VacuumWorldLocation targetLocation = ((VacuumWorldSpace) context)
        .getFrontLocation(originalCooridinates, agentFacingDirection);

    return !checkForWall(agentLocation, agentFacingDirection)
        && !checkForObstacle(targetLocation);
  }

  private synchronized boolean checkForWall(VacuumWorldLocation agentLocation,
      AgentFacingDirection agentFacingDirection) {
    return agentLocation.getNeighborLocation(agentFacingDirection) == VacuumWorldLocationType.WALL;
  }

  private synchronized boolean checkForObstacle(
      VacuumWorldLocation targetLocation) {
    if (targetLocation == null) {
      return true;
    }

    return checkForGenericObstacle(targetLocation)
        || checkForAnotherAgent(targetLocation);
  }

  private synchronized boolean checkForAnotherAgent(
      VacuumWorldLocation targetLocation) {
    return targetLocation.getAgent() != null;
  }

  private synchronized boolean checkForGenericObstacle(
      VacuumWorldLocation targetLocation) {
    Obstacle potentialObstacle = targetLocation.getObstacle();

    if (potentialObstacle == null || potentialObstacle instanceof Dirt) {
      return false;
    }

    return true;
  }

  @Override
  public synchronized boolean isNecessary(MoveAction action, Space context) {
    return false;
  }

  @Override
  public synchronized Result perform(MoveAction action, Space context) {
    VacuumWorldLocation agentLocation = null;
    VacuumWorldLocation targetLocation = null;

    try {
      AgentFacingDirection agentFacingDirection = this.activeAgents.get(
          Thread.currentThread().getId()).getFacingDirection();
      agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
      VacuumWorldCoordinates originalCooridinates = agentLocation
          .getCoordinates();
      targetLocation = ((VacuumWorldSpace) context).getFrontLocation(
          originalCooridinates, agentFacingDirection);

      agentLocation.getExclusiveWriteLock();
      targetLocation.getExclusiveWriteLock();

      action.setOldLocationCoordinates(originalCooridinates);

      ((VacuumWorldLocation) ((EnvironmentalSpace) context)
          .getLocation(agentLocation.getCoordinates())).removeAgent();
      targetLocation.addAgent(this.activeAgents.get(Thread.currentThread()
          .getId()));
      targetLocation.getAgent().setCurrentLocation(
          originalCooridinates.getNewCoordinates(agentFacingDirection));
      this.activeAgents.get(Thread.currentThread().getId()).setCurrentLocation(
          originalCooridinates.getNewCoordinates(agentFacingDirection));
      agentLocation.releaseExclusiveWriteLock();
      targetLocation.releaseExclusiveWriteLock();

      return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));

    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      releaseWriteLockIfNecessary(agentLocation);
      releaseWriteLockIfNecessary(targetLocation);

      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  @Override
  public synchronized boolean succeeded(MoveAction action, Space context) {
    VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
    VacuumWorldLocation agentOldLocation = (VacuumWorldLocation) ((EnvironmentalSpace) context)
        .getLocation(action.getOldLocationCoordinates());

    return !(agentOldLocation.isAnAgentPresent())
        && this.activeAgents.get(Thread.currentThread().getId()).equals(
            agentLocation.getAgent());
  }

  @Override
  public synchronized boolean isPossible(CleanAction action, Space context) {
    VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);

    if (agentLocation == null) {
      return false;
    }

    return agentLocation.isDirtPresent();
  }

  @Override
  public synchronized boolean isNecessary(CleanAction action, Space context) {
    return false;
  }

  @Override
  public synchronized Result perform(CleanAction action, Space context) {
    VacuumWorldLocation agentLocation = null;

    try {
      agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
      agentLocation.getExclusiveWriteLock();
      agentLocation.removeDirt();
      agentLocation.releaseExclusiveWriteLock();

      return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      releaseWriteLockIfNecessary(agentLocation);
      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  @Override
  public synchronized boolean succeeded(CleanAction action, Space context) {
    VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
    return !(agentLocation.isDirtPresent());
  }

  @Override
  public synchronized boolean isPossible(PerceiveAction action, Space context) {
    return true;
  }

  @Override
  public synchronized boolean isNecessary(PerceiveAction action, Space context) {
    return false;
  }

  @Override
  public synchronized Result perform(PerceiveAction action, Space context) {
    try {
      /*
       * Perceptions will be added by the attempt methods in this class. This
       * method essentially does nothing.
       */

      return new VacuumWorldActionResult(ActionResult.ACTION_DONE, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context,
      int perceptionRange, boolean canSeeBehind) {
    VacuumWorldCleaningAgent current = this.activeAgents.get(Thread
        .currentThread().getId());
    AgentFacingDirection direction = current.getFacingDirection();

    int northernOverhead = getNorthernOverhead(perceptionRange, canSeeBehind,
        direction);
    int southernOverhead = getSouthernOverhead(perceptionRange, canSeeBehind,
        direction);
    int westernOverhead = getWesternOverhead(perceptionRange, canSeeBehind,
        direction);
    int easternOverhead = getEasternOverhead(perceptionRange, canSeeBehind,
        direction);

    int[] overheads = new int[] { northernOverhead, southernOverhead,
        westernOverhead, easternOverhead };

    return perceive(context, overheads);
  }

  private synchronized int getEasternOverhead(int perceptionRange,
      boolean canSeeBehind, AgentFacingDirection direction) {
    int baseOverhead = perceptionRange - 1;

    if (!canSeeBehind && direction == AgentFacingDirection.WEST) {
      baseOverhead = 0;
    }

    return baseOverhead;
  }

  private synchronized int getWesternOverhead(int perceptionRange,
      boolean canSeeBehind, AgentFacingDirection direction) {
    int baseOverhead = perceptionRange - 1;

    if (!canSeeBehind && direction == AgentFacingDirection.EAST) {
      baseOverhead = 0;
    }

    return baseOverhead;
  }

  private synchronized int getSouthernOverhead(int perceptionRange,
      boolean canSeeBehind, AgentFacingDirection direction) {
    int baseOverhead = perceptionRange - 1;

    if (!canSeeBehind && direction == AgentFacingDirection.NORTH) {
      baseOverhead = 0;
    }

    return baseOverhead;
  }

  private synchronized int getNorthernOverhead(int perceptionRange,
      boolean canSeeBehind, AgentFacingDirection direction) {
    int baseOverhead = perceptionRange - 1;

    if (!canSeeBehind && direction == AgentFacingDirection.SOUTH) {
      baseOverhead = 0;
    }

    return baseOverhead;
  }

  private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context,
      int[] overheads) {
    VacuumWorldCoordinates currentCoordinates = getCurrentActorLocation(context)
        .getCoordinates();
    int currentX = currentCoordinates.getX();
    int currentY = currentCoordinates.getY();

    Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = perceive(
        context, overheads, currentX, currentY);
    return new VacuumWorldPerception(perception, currentCoordinates);
  }

  private synchronized Map<VacuumWorldCoordinates, VacuumWorldLocation> perceive(
      VacuumWorldSpace context, int[] overheads, int currentX, int currentY) {
    Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = new HashMap<>();

    for (int i = currentX - overheads[2]; i <= currentX + overheads[3]; i++) {
      for (int j = currentY - overheads[0]; j <= currentY + overheads[1]; j++) {
        VacuumWorldCoordinates temp = new VacuumWorldCoordinates(i, j);
        VacuumWorldLocation location = (VacuumWorldLocation) context
            .getLocation(temp);

        if (location != null) {
          perception.put(temp, location);
        }
      }
    }

    return perception;
  }

  /**
   * A perceiving action has no post-conditions to check.
   */
  @Override
  public synchronized boolean succeeded(PerceiveAction action, Space context) {
    return true;
  }

  @Override
  public boolean isPossible(SpeechAction action, Space context) {
    return true;
  }

  @Override
  public boolean isNecessary(SpeechAction action, Space context) {
    return false;
  }

  @Override
  public Result perform(SpeechAction action, Space context) {
    try {

      VacuumWorldSpeechActionResult r = new VacuumWorldSpeechActionResult(
          ActionResult.ACTION_DONE, action);
      ArrayList<String> sensorids = new ArrayList<String>();
      VacuumWorldSpace space = (VacuumWorldSpace) context;
      if (r.getRecipientsIds() == null || r.getRecipientsIds().isEmpty()) {
        // send to everyone
        space.getAgents().forEach(new Consumer<VacuumWorldCleaningAgent>() {
          @Override
          public void accept(VacuumWorldCleaningAgent agent) {
            String sensorId = ((VacuumWorldDefaultSensor) agent.getSensors()
                .get(agent.getActionResultSensorIndex())).getSensorId();
            if (!agent.getId().equals(r.getSender())) {
              sensorids.add(sensorId);
            }
          }
        });
        r.setRecipientsIds(sensorids);
      } else {
        // convert the agent ids to their sensors ids that should be notified
        action.getRecipientsIds().forEach(new Consumer<String>() {
          @Override
          public void accept(String t) {
            VacuumWorldCleaningAgent agent = space.getAgentById(t);
            sensorids.add(((VacuumWorldDefaultSensor) agent.getSensors().get(
                agent.getActionResultSensorIndex())).getSensorId());
          }
        });
        r.setRecipientsIds(sensorids);
      }

      VacuumWorldActionResult ar = new VacuumWorldActionResult(
          ActionResult.ACTION_DONE, null, this.sensorsToNotify.get(Thread
              .currentThread().getId()));
      return new VacuumWorldSpeechPerceptionResultWrapper(r, ar);
    } catch (Exception e) {
      Utils.log(e);
      this.activeAgents.remove(Thread.currentThread().getId());
      return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
          this.sensorsToNotify.get(Thread.currentThread().getId()));
    }
  }

  @Override
  public boolean succeeded(SpeechAction action, Space context) {
    return true;
  }

  @Override
  public synchronized void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldSpace && arg instanceof Object[]) {
      manageEnvironmentRequest((Object[]) arg);
    }
  }

  private synchronized void manageEnvironmentRequest(Object[] arg) {
    if (arg.length != 2) {
      return;
    }

    if (arg[0] instanceof VacuumWorldEvent
        && arg[1] instanceof VacuumWorldSpace) {
      attemptEvent((VacuumWorldEvent) arg[0], (VacuumWorldSpace) arg[1]);
    }
  }

  private synchronized void attemptEvent(VacuumWorldEvent event,
      VacuumWorldSpace context) {
    Result result = event.attempt(this, context);
    ActionResult code = ((Result) result).getActionResult();
    MonitoringUpdateEvent ue = new MonitoringUpdateEvent(event.getAction(),
        event.getTimestamp(), event.getActor(), code);
    notifyObservers(ue, VacuumWorldMonitoringContainer.class);

    this.activeAgents.remove(Thread.currentThread().getId());
    this.sensorsToNotify.remove(Thread.currentThread().getId());

    notifyObservers(result, VacuumWorldSpace.class);
  }
}