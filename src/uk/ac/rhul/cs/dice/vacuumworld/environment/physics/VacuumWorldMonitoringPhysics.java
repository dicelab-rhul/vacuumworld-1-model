package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObserver;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.MonitoringResult;

public class VacuumWorldMonitoringPhysics extends AbstractPhysics implements
    VacuumWorldMonitoringPhysicsInterface, CustomObserver {

  private Physics monitoredContainerPhysics;
  private ConcurrentMap<Long, AbstractAgent> activeAgents;
  private ConcurrentMap<Long, String> sensorsToNotify;

  public VacuumWorldMonitoringPhysics() {
    this.activeAgents = new ConcurrentHashMap<>();
    this.sensorsToNotify = new ConcurrentHashMap<>();
  }

  public Physics getMonitoredContainerPhysics() {
    return this.monitoredContainerPhysics;

  }

  public void setMonitoredContainerPhysics(Physics monitoredContainerPysics) {
    this.monitoredContainerPhysics = monitoredContainerPysics;
  }

  @Override
  public Result attempt(Event event, Space context) {
    this.activeAgents.put(Thread.currentThread().getId(),
        (AbstractAgent)event.getActor());
    this.sensorsToNotify.put(Thread.currentThread().getId(),
        ((MonitoringEvent) event).getSensorToCallBackId());

    return event.getAction().attempt(this, context);
  }

  @Override
  public boolean isPossible(TotalPerceptionAction action, Space context) {
    return true;
  }

  @Override
  public boolean isNecessary(TotalPerceptionAction action, Space context) {
    return false;
  }

  @Override
  public Result perform(TotalPerceptionAction action, Space context) {
    return new MonitoringResult(ActionResult.ACTION_DONE, null, null, ((VacuumWorldMonitoringContainer)context).getVacuumWorldSpaceRepresentation());
  }

  @Override
  public boolean succeeded(TotalPerceptionAction action, Space context) {
    return true;
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldMonitoringContainer && arg instanceof Object[]) {
      manageEnvironmentRequest((Object[]) arg);
    }
  }

  private void manageEnvironmentRequest(Object[] arg) {
    if (arg.length != 2) {
      return;
    }

    if (arg[0] instanceof MonitoringEvent
        && arg[1] instanceof VacuumWorldMonitoringContainer) {
      attemptEvent((MonitoringEvent) arg[0], (VacuumWorldMonitoringContainer) arg[1]);
    }
  }

  private void attemptEvent(MonitoringEvent event, VacuumWorldMonitoringContainer context) {
    Result result = event.attempt(this, context);

    if (result.getRecipientId() == null) {
      result.setRecipientId(this.sensorsToNotify.get(Thread.currentThread()
          .getId()));
    }

    this.activeAgents.remove(Thread.currentThread().getId());
    this.sensorsToNotify.remove(Thread.currentThread().getId());

    notifyObservers(result, VacuumWorldMonitoringContainer.class);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime
        * result
        + ((monitoredContainerPhysics == null) ? 0 : monitoredContainerPhysics
            .hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    VacuumWorldMonitoringPhysics other = (VacuumWorldMonitoringPhysics) obj;
    if (monitoredContainerPhysics == null) {
      if (other.monitoredContainerPhysics != null)
        return false;
    } else if (!monitoredContainerPhysics
        .equals(other.monitoredContainerPhysics))
      return false;
    return true;
  }
}