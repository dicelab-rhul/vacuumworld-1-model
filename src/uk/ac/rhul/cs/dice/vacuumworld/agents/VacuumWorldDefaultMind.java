package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.rhul.cs.dice.cawl.CyclingAutonomousAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.ThreadState;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldMindInterface;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;

public class VacuumWorldDefaultMind extends CyclingAutonomousAgentMind
    implements VacuumWorldMindInterface {
  private int perceptionRange;
  private boolean canSeeBehind;
  private List<Action> availableActions;
  private Random rng;
  private DefaultActionResult previousActionResult;
  private Action nextAction;
  private ThreadState state;
  private boolean canProceed;

  public VacuumWorldDefaultMind() {
    this.previousActionResult = null;
    this.rng = new Random();
    this.canProceed = false;
  }

  public void start(int perceptionRange, boolean canSeeBehind,
      Set<Action> availableActions) {
    //canProceed = false;
    this.state = ThreadState.JUST_STARTED;

    this.perceptionRange = perceptionRange;
    this.canSeeBehind = canSeeBehind;
    this.availableActions = new ArrayList<>(availableActions);
    this.nextAction = decide();
    this.canProceed = false;

    this.state = ThreadState.AFTER_DECIDE;

    waitForServerBeforeExecution();
    execute(this.nextAction);
  }

  public void waitForServerBeforeExecution() {
    while (!this.canProceed)
      ;
  }

  public void resume() {
    this.canProceed = true;
  }

  public ThreadState getState() {
    return this.state;
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldDefaultBrain) {
      manageBrainMessage(arg);
    }
  }

  private void manageBrainMessage(Object arg) {
    if (arg instanceof DefaultActionResult) {
      perceive(arg);
      this.state = ThreadState.AFTER_PERCEIVE;
      return;
    }
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    if (perceptionWrapper instanceof VacuumWorldActionResult) {
      this.previousActionResult = (VacuumWorldActionResult) perceptionWrapper;
    } else if (perceptionWrapper instanceof DefaultActionResult) {
      this.previousActionResult = null;
    }
  }

  @Override
  public Action decide(Object... parameters) {
    if (this.previousActionResult == null) {
      return new PerceiveAction(this.perceptionRange, this.canSeeBehind);
    } else if (this.previousActionResult instanceof VacuumWorldActionResult) {
      return decideFromPerception((VacuumWorldActionResult) this.previousActionResult);
    } else {
      return decideMove();
    }
  }

  private Action decideFromPerception(
      VacuumWorldActionResult previousActionResult) {
    if (previousActionResult.getPerception() == null) {
      return decideMove();
    } else {
      return decideMove(previousActionResult.getPerception());
    }
  }

  @Override
  public void execute(Action action) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    notifyObservers(action, VacuumWorldDefaultBrain.class);
  }

  private Action decideMove(VacuumWorldPerception perception) {
    if (perception != null) {
      updateAvailableActions(perception);
    }

    return decideMove();
  }

  private Action decideMove() {
    int size = this.availableActions.size();
    int randomNumber = rng.nextInt(size);

    return availableActions.get(randomNumber);
  }

  private void updateAvailableActions(VacuumWorldPerception perception) {
    updateMoveActionIfNecessary(perception);
    updateCleaningActionIfNecessary(perception);
  }

  private void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
    VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
    VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(
        agentCoordinates);

    if (agentLocation.isDirtPresent()) {
      addCleanIfNecessary();
    } else {
      removeCleanIfNecessary();
    }
  }

  private void removeCleanIfNecessary() {
    List<CleanAction> toRemove = new ArrayList<>();

    for (Action a : this.availableActions) {
      if (a instanceof CleanAction
          || a.getClass().isAssignableFrom(CleanAction.class)) {
        toRemove.add((CleanAction) a);
      }
    }

    this.availableActions.removeAll(toRemove);
  }

  private void addCleanIfNecessary() {
    for (Action a : this.availableActions) {
      if (a instanceof CleanAction
          || a.getClass().isAssignableFrom(CleanAction.class)) {
        return;
      }
    }

    this.availableActions.add(new CleanAction());
  }

  private void addMoveIfNecessary() {
    for (Action a : this.availableActions) {
      if (a instanceof MoveAction
          || a.getClass().isAssignableFrom(MoveAction.class)) {
        return;
      }
    }

    this.availableActions.add(new MoveAction());
  }

  private void removeMoveIfNecessary() {
    List<MoveAction> toRemove = new ArrayList<>();

    for (Action a : this.availableActions) {
      if (a instanceof MoveAction
          || a.getClass().isAssignableFrom(MoveAction.class)) {
        toRemove.add((MoveAction) a);
      }
    }

    this.availableActions.removeAll(toRemove);
  }

  private void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
    VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
    VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(
        agentCoordinates);
    VacuumWorldCleaningAgent agent = agentLocation.getAgent();

    if (agent != null) {
      AgentFacingDirection facingDirection = agent.getFacingDirection();

      if (agentLocation.getNeighborLocation(facingDirection) == VacuumWorldLocationType.WALL) {
        removeMoveIfNecessary();
      } else {
        addMoveIfNecessary();
      }
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((availableActions == null) ? 0 : availableActions.hashCode());
    result = prime * result + (canSeeBehind ? 1231 : 1237);
    result = prime * result
        + ((nextAction == null) ? 0 : nextAction.hashCode());
    result = prime * result + perceptionRange;
    result = prime
        * result
        + ((previousActionResult == null) ? 0 : previousActionResult.hashCode());
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
    VacuumWorldDefaultMind other = (VacuumWorldDefaultMind) obj;
    if (availableActions == null) {
      if (other.availableActions != null)
        return false;
    } else if (!availableActions.equals(other.availableActions))
      return false;
    if (canSeeBehind != other.canSeeBehind)
      return false;
    if (nextAction == null) {
      if (other.nextAction != null)
        return false;
    } else if (!nextAction.equals(other.nextAction))
      return false;
    if (perceptionRange != other.perceptionRange)
      return false;
    if (previousActionResult == null) {
      if (other.previousActionResult != null)
        return false;
    } else if (!previousActionResult.equals(other.previousActionResult))
      return false;
    return true;
  }
}